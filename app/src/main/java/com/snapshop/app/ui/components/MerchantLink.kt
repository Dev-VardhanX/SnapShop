package com.snapshop.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

fun openMerchantLink(
    context: Context,
    buyUrl: String?,
    snackbarHostState: SnackbarHostState? = null,
    scope: CoroutineScope? = null
) {
    if (!isValidMerchantUrl(buyUrl)) {
        if (snackbarHostState != null && scope != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Deal link unavailable.")
            }
        } else {
            Toast.makeText(context, "Deal link unavailable.", Toast.LENGTH_SHORT).show()
        }
        return
    }
    val cleanUrl = buyUrl!!.trim()
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w("MerchantLink", "No app to open URL: ${e.message}")
        if (snackbarHostState != null && scope != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Couldn't open this link.")
            }
        } else {
            Toast.makeText(context, "Couldn't open this link.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("MerchantLink", "Failed to open merchant URL: ${e.message}")
        if (snackbarHostState != null && scope != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Couldn't open this link.")
            }
        } else {
            Toast.makeText(context, "Couldn't open this link.", Toast.LENGTH_SHORT).show()
        }
    }
}
