package com.snapshop.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.core.net.toUri
import com.snapshop.app.util.MerchantUrlResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val HTTP_URL_PATTERN = Regex("^https?://.+", RegexOption.IGNORE_CASE)

fun isValidMerchantUrl(buyUrl: String?): Boolean {
    val clean = buyUrl?.trim().orEmpty()
    if (clean.isEmpty()) return false
    if (!HTTP_URL_PATTERN.matches(clean)) return false
    return try {
        val uri = clean.toUri()
        uri.scheme?.equals("http", ignoreCase = true) == true ||
            uri.scheme?.equals("https", ignoreCase = true) == true
    } catch (_: Exception) {
        false
    }
}

/**
 * Resolves direct store link and opens the merchant app (e.g. Flipkart, Amazon)
 * or mobile store page directly, avoiding intermediate Google pages.
 */
fun openMerchantLink(
    context: Context,
    buyUrl: String?,
    merchantName: String? = null,
    productTitle: String? = null,
    snackbarHostState: SnackbarHostState? = null,
    scope: CoroutineScope? = null
) {
    val initialResolved = MerchantUrlResolver.resolveDirectMerchantUrl(
        rawUrl = buyUrl,
        source = merchantName,
        title = productTitle
    )

    if (!isValidMerchantUrl(initialResolved)) {
        showError(context, "Deal link unavailable.", snackbarHostState, scope)
        return
    }

    val targetUrl = initialResolved!!.trim()

    // If targetUrl is already a direct merchant link (not Google), open immediately!
    if (!MerchantUrlResolver.isGoogleUrl(targetUrl)) {
        launchViewIntent(context, targetUrl, snackbarHostState, scope)
        return
    }

    // If it's still a Google URL (e.g., an opaque Google ad redirect /aclk) and we have a CoroutineScope,
    // attempt a quick background HTTP redirect resolution to see if it redirects to Flipkart/store.
    if (scope != null) {
        scope.launch {
            val redirectedUrl = try {
                MerchantUrlResolver.resolveHttpRedirect(targetUrl)
            } catch (_: Exception) {
                null
            }

            val finalUrl = when {
                !redirectedUrl.isNullOrBlank() && !MerchantUrlResolver.isGoogleUrl(redirectedUrl) -> {
                    redirectedUrl
                }
                !merchantName.isNullOrBlank() && !productTitle.isNullOrBlank() -> {
                    MerchantUrlResolver.buildMerchantSearchUrl(merchantName, productTitle) ?: targetUrl
                }
                else -> targetUrl
            }

            withContext(Dispatchers.Main) {
                launchViewIntent(context, finalUrl, snackbarHostState, scope)
            }
        }
    } else {
        launchViewIntent(context, targetUrl, snackbarHostState, scope)
    }
}

private fun launchViewIntent(
    context: Context,
    url: String,
    snackbarHostState: SnackbarHostState?,
    scope: CoroutineScope?
) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w("MerchantLink", "No app to open URL: ${e.message}")
        showError(context, "Couldn't open this link.", snackbarHostState, scope)
    } catch (e: Exception) {
        Log.e("MerchantLink", "Failed to open merchant URL: ${e.message}")
        showError(context, "Couldn't open this link.", snackbarHostState, scope)
    }
}

private fun showError(
    context: Context,
    message: String,
    snackbarHostState: SnackbarHostState?,
    scope: CoroutineScope?
) {
    if (snackbarHostState != null && scope != null) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    } else {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
