package com.snapshop.app.navigation

sealed class Screen(val route: String) {

    data object Splash : Screen("splash")
    data object Home : Screen("home")

    data object SearchResults : Screen("search_results?query={query}") {
        fun createRoute(query: String? = null): String {
            return if (!query.isNullOrBlank()) {
                "search_results?query=${java.net.URLEncoder.encode(query, "UTF-8")}"
            } else {
                "search_results"
            }
        }
    }

    data object ProductDetails : Screen("product_details")

    data object Wishlist : Screen("wishlist")

    data object SearchHistory : Screen("search_history")

    data object RecentlyViewed : Screen("recently_viewed")

    data object Settings : Screen("settings")

    data object ImageSearch : Screen("image_search?mode={mode}") {
        fun createRoute(mode: String = "CAMERA"): String {
            return "image_search?mode=$mode"
        }
    }
}