package com.snapshop.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Intelligent Merchant URL Resolver.
 * Unwraps intermediate Google redirect links (e.g. google.com/url, google.com/aclk),
 * removes search engine tracking parameters, and routes directly to native store
 * deep-links (Flipkart, Amazon, Croma, etc.) so users land directly in the merchant's
 * app or store page instead of being stuck on Google.
 */
object MerchantUrlResolver {

    private val GOOGLE_TRACKING_PARAMS = setOf(
        "rct", "esrc", "usg", "ved", "ei", "opi", "cd", "biw", "bih", "sa", "sig"
    )

    private val REDIRECT_PARAM_KEYS = listOf(
        "adurl",
        "url",
        "q",
        "dest",
        "destination",
        "target",
        "redirect_url",
        "r"
    )

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(1500, TimeUnit.MILLISECONDS)
            .readTimeout(1500, TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * Checks if the given URL points to a Google domain or Google service.
     */
    fun isGoogleUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val clean = url.trim().lowercase()
        return try {
            val uri = URI(clean)
            val host = uri.host ?: ""
            host.contains("google.") || host.endsWith("google") || host == "goo.gl"
        } catch (_: Exception) {
            clean.contains("google.com") ||
                clean.contains("google.co.") ||
                clean.contains("google.to")
        }
    }

    /**
     * Extracts destination merchant URL from a Google redirect wrapper.
     * Handles single or double URL-encoded query parameters (adurl, url, q, etc.).
     */
    fun unwrapGoogleUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        var currentUrl = url.trim()

        for (iteration in 0 until 3) {
            if (!isGoogleUrl(currentUrl)) {
                return cleanMerchantUrl(currentUrl)
            }

            val extracted = extractDestinationFromGoogle(currentUrl)
            if (!extracted.isNullOrBlank() && extracted != currentUrl) {
                currentUrl = extracted
            } else {
                break
            }
        }

        return if (!isGoogleUrl(currentUrl)) cleanMerchantUrl(currentUrl) else currentUrl
    }

    private fun extractDestinationFromGoogle(googleUrl: String): String? {
        val queryIndex = googleUrl.indexOf('?')
        if (queryIndex == -1 || queryIndex >= googleUrl.length - 1) return null

        val query = googleUrl.substring(queryIndex + 1).substringBefore('#')

        // 1. Try finding explicit markers (e.g. &adurl=, ?adurl=, &url=, ?url=)
        for (key in REDIRECT_PARAM_KEYS) {
            val marker = "$key="
            var searchIndex = 0
            while (searchIndex < query.length) {
                val found = query.indexOf(marker, searchIndex)
                if (found == -1) break
                val isAtStart = found == 0 || query[found - 1] == '&'
                if (isAtStart) {
                    val rawValue = query.substring(found + marker.length)
                    val candidate = parseCandidateValue(rawValue)
                    if (candidate != null) {
                        return candidate
                    }
                }
                searchIndex = found + marker.length
            }
        }

        return null
    }

    private fun parseCandidateValue(rawValue: String): String? {
        // Find if any google tracking parameters follow (if the value was unencoded)
        var cleanValue = rawValue
        for (param in GOOGLE_TRACKING_PARAMS) {
            val trackerIndex = cleanValue.indexOf("&$param=")
            if (trackerIndex != -1) {
                cleanValue = cleanValue.substring(0, trackerIndex)
            }
        }

        // Also check if cut by standard & if not part of inner URL
        var candidate = cleanValue
        for (d in 0 until 3) {
            val trimmed = candidate.trim()
            if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
            ) {
                if (!isGoogleUrl(trimmed)) {
                    return trimmed
                }
            }

            val decoded = try {
                URLDecoder.decode(candidate, StandardCharsets.UTF_8.name())
            } catch (_: Exception) {
                candidate
            }

            if (decoded == candidate) break
            candidate = decoded
        }

        val finalCheck = candidate.trim()
        return if ((finalCheck.startsWith("http://", ignoreCase = true) ||
                finalCheck.startsWith("https://", ignoreCase = true)) &&
            !isGoogleUrl(finalCheck)
        ) {
            finalCheck
        } else {
            null
        }
    }

    /**
     * Cleans up Google tracking parameters from the query string of a merchant URL.
     */
    fun cleanMerchantUrl(url: String): String {
        if (isGoogleUrl(url)) return url
        val queryIndex = url.indexOf('?')
        if (queryIndex == -1) return url

        val base = url.substring(0, queryIndex)
        val query = url.substring(queryIndex + 1).substringBefore('#')
        val fragment = if (url.contains('#')) "#" + url.substringAfter('#') else ""

        val keptParams = query.split('&').filter { param ->
            val key = param.substringBefore('=').lowercase()
            key !in GOOGLE_TRACKING_PARAMS
        }

        return if (keptParams.isEmpty()) {
            base + fragment
        } else {
            "$base?${keptParams.joinToString("&")}$fragment"
        }
    }

    /**
     * Builds a direct store search or product deep-link when only merchant name
     * and product title are known, bypassing intermediate Google Shopping pages.
     */
    fun buildMerchantSearchUrl(source: String?, title: String?): String? {
        if (source.isNullOrBlank() || title.isNullOrBlank()) return null
        val cleanSource = source.trim()
        val cleanTitle = sanitizeSearchQuery(title)
        if (cleanTitle.isBlank()) return null

        val encodedTitle = try {
            URLEncoder.encode(cleanTitle, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            cleanTitle.replace(" ", "+")
        }

        val lowerSource = cleanSource.lowercase()

        return when {
            lowerSource.contains("flipkart") -> {
                "https://www.flipkart.com/search?q=$encodedTitle"
            }
            lowerSource.contains("amazon") -> {
                "https://www.amazon.in/s?k=$encodedTitle"
            }
            lowerSource.contains("myntra") -> {
                "https://www.myntra.com/$encodedTitle"
            }
            lowerSource.contains("croma") -> {
                "https://www.croma.com/searchB?q=$encodedTitle"
            }
            lowerSource.contains("reliance") -> {
                "https://www.reliancedigital.in/search?q=$encodedTitle"
            }
            lowerSource.contains("tatacliq") || lowerSource.contains("tata cliq") -> {
                "https://www.tatacliq.com/search/?searchCategory=all&text=$encodedTitle"
            }
            lowerSource.contains("meesho") -> {
                "https://www.meesho.com/search?q=$encodedTitle"
            }
            lowerSource.contains("nykaa") -> {
                "https://www.nykaa.com/search/result/?q=$encodedTitle"
            }
            lowerSource.contains("ajio") -> {
                "https://www.ajio.com/search/?text=$encodedTitle"
            }
            lowerSource.contains("jiomart") -> {
                "https://www.jiomart.com/search/$encodedTitle"
            }
            // Domain-like store source (e.g. samsung.com, apple.com, boat-lifestyle.com)
            lowerSource.contains(".") && !lowerSource.contains("google") -> {
                val domain = lowerSource.removePrefix("http://")
                    .removePrefix("https://")
                    .removePrefix("www.")
                    .substringBefore('/')
                if (domain.isNotBlank()) {
                    "https://www.$domain/search?q=$encodedTitle"
                } else null
            }
            else -> null
        }
    }

    /**
     * Resolves the best direct merchant URL from raw URL, source merchant, and product title.
     * Prioritizes:
     * 1. Unwrapping direct merchant link from Google redirect URLs (0ms)
     * 2. Direct merchant link if already non-Google (0ms)
     * 3. Direct store search deep-link if URL is a Google comparison/search page and merchant is known
     * 4. Original URL as safe fallback
     */
    fun resolveDirectMerchantUrl(
        rawUrl: String?,
        source: String? = null,
        title: String? = null
    ): String? {
        val cleanRaw = rawUrl?.trim()

        if (cleanRaw.isNullOrBlank()) {
            return buildMerchantSearchUrl(source, title)
        }

        val unwrapped = unwrapGoogleUrl(cleanRaw)
        if (!unwrapped.isNullOrBlank() && !isGoogleUrl(unwrapped)) {
            return unwrapped
        }

        // If the URL is still a Google URL (e.g. Google Shopping product page or search)
        // and we have a recognized merchant source (like Flipkart), route directly to the merchant!
        if (isGoogleUrl(unwrapped ?: cleanRaw)) {
            val directMerchantSearch = buildMerchantSearchUrl(source, title)
            if (directMerchantSearch != null) {
                return directMerchantSearch
            }
        }

        return unwrapped ?: cleanRaw
    }

    /**
     * Follows HTTP 3xx redirects on IO dispatcher for opaque Google redirect links
     * (e.g. Google ad click tokens without explicit query parameters) to discover
     * the actual destination URL.
     */
    suspend fun resolveHttpRedirect(url: String): String? = withContext(Dispatchers.IO) {
        if (!isGoogleUrl(url)) return@withContext url

        try {
            var currentUrl = url
            for (step in 0 until 3) {
                val request = Request.Builder()
                    .url(currentUrl)
                    .head()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                    .build()

                val call = httpClient.newCall(request)
                val response = call.execute()
                response.use { resp ->
                    val code = resp.code
                    val location = resp.header("Location")
                    if (code in 300..399 && !location.isNullOrBlank()) {
                        currentUrl = location
                        if (!isGoogleUrl(currentUrl)) {
                            return@withContext cleanMerchantUrl(currentUrl)
                        }
                    } else {
                        return@withContext if (!isGoogleUrl(currentUrl)) cleanMerchantUrl(currentUrl) else null
                    }
                }
            }
            if (!isGoogleUrl(currentUrl)) cleanMerchantUrl(currentUrl) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun sanitizeSearchQuery(title: String): String {
        return title.replace(Regex("[\"\'(){}\\[\\]+:*#&/\\-_]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(" ")
            .take(8)
            .joinToString(" ")
    }
}
