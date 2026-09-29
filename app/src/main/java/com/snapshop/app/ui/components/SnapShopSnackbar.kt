package com.snapshop.app.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnapShopSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("SnapShopSnackbarHostState not provided")
}
