package com.snapshop.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.BottomNavBar
import com.snapshop.app.ui.details.ProductDetailsScreen
import com.snapshop.app.ui.history.HistoryScreen
import com.snapshop.app.ui.home.HomeScreen
import com.snapshop.app.ui.imagesearch.ImageSearchScreen
import com.snapshop.app.ui.recentlyviewed.RecentlyViewedScreen
import com.snapshop.app.ui.search.ProductSearchScreen
import com.snapshop.app.ui.wishlist.WishlistScreen
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var activeProduct by remember { mutableStateOf<Product?>(null) }

    // Bottom Navigation Bar should be visible on primary tabs
    val primaryBottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.SearchResults.route,
        Screen.Wishlist.route,
        Screen.SearchHistory.route,
        Screen.RecentlyViewed.route
    )

    val showBottomBar = primaryBottomNavRoutes.any { route ->
        currentRoute?.startsWith(route.split("?")[0]) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val cleanRoute = currentRoute?.split("?")?.get(0) ?: Screen.Home.route
                BottomNavBar(
                    currentRoute = cleanRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onSearchQuerySubmit = { query ->
                        navController.navigate(Screen.SearchResults.createRoute(query))
                    },
                    onCameraClick = {
                        navController.navigate(Screen.ImageSearch.createRoute("CAMERA"))
                    },
                    onGalleryClick = {
                        navController.navigate(Screen.ImageSearch.createRoute("GALLERY"))
                    },
                    onHistoryItemClick = { query ->
                        navController.navigate(Screen.SearchResults.createRoute(query))
                    }
                )
            }

            // Search Results Screen
            composable(
                route = Screen.SearchResults.route,
                arguments = listOf(
                    navArgument("query") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val encodedQuery = backStackEntry.arguments?.getString("query")
                val query = encodedQuery?.let {
                    try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it }
                }

                ProductSearchScreen(
                    initialQuery = query,
                    onProductClick = { product ->
                        activeProduct = product
                        navController.navigate(Screen.ProductDetails.route)
                    },
                    onCameraClick = {
                        navController.navigate(Screen.ImageSearch.createRoute("CAMERA"))
                    },
                    onGalleryClick = {
                        navController.navigate(Screen.ImageSearch.createRoute("GALLERY"))
                    }
                )
            }

            // Product Details Screen
            composable(Screen.ProductDetails.route) {
                val currentSelectedProduct = activeProduct
                if (currentSelectedProduct != null) {
                    ProductDetailsScreen(
                        product = currentSelectedProduct,
                        onBackClick = { navController.popBackStack() },
                        onRelatedProductClick = { related ->
                            activeProduct = related
                        }
                    )
                } else {
                    navController.popBackStack()
                }
            }

            // Image Search Screen (Camera / Gallery Gemini AI)
            composable(
                route = Screen.ImageSearch.route,
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                        defaultValue = "CAMERA"
                    }
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "CAMERA"
                ImageSearchScreen(
                    initialMode = mode,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { product ->
                        activeProduct = product
                        navController.navigate(Screen.ProductDetails.route)
                    }
                )
            }

            // Saved Wishlist Screen
            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    onProductClick = { product ->
                        activeProduct = product
                        navController.navigate(Screen.ProductDetails.route)
                    }
                )
            }

            // Search History Screen
            composable(Screen.SearchHistory.route) {
                HistoryScreen(
                    onSearchQueryClick = { query ->
                        navController.navigate(Screen.SearchResults.createRoute(query))
                    }
                )
            }

            // Recently Viewed Screen
            composable(Screen.RecentlyViewed.route) {
                RecentlyViewedScreen(
                    onProductClick = { product ->
                        activeProduct = product
                        navController.navigate(Screen.ProductDetails.route)
                    }
                )
            }
        }
    }
}