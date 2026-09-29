package com.snapshop.app.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.snapshop.app.domain.Product
import com.snapshop.app.ui.components.BottomNavBar
import com.snapshop.app.ui.components.LocalSnapShopSnackbarHostState
import com.snapshop.app.ui.components.openMerchantLink
import com.snapshop.app.ui.details.ProductDetailsScreen
import com.snapshop.app.ui.history.HistoryScreen
import com.snapshop.app.ui.home.HomeScreen
import com.snapshop.app.ui.imagesearch.ImageSearchScreen
import com.snapshop.app.ui.recentlyviewed.RecentlyViewedScreen
import com.snapshop.app.ui.search.ProductSearchScreen
import com.snapshop.app.ui.search.ProductSearchViewModel
import com.snapshop.app.ui.settings.SettingsScreen
import com.snapshop.app.ui.wishlist.WishlistScreen
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activity = LocalContext.current as ComponentActivity
    val searchViewModel: ProductSearchViewModel = viewModel(activity)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeProduct by remember { mutableStateOf<Product?>(null) }

    val primaryBottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.SearchResults.route,
        Screen.Wishlist.route,
        Screen.SearchHistory.route
    )

    val showBottomBar = primaryBottomNavRoutes.any { route ->
        currentRoute?.startsWith(route.split("?")[0]) == true
    }

    CompositionLocalProvider(LocalSnapShopSnackbarHostState provides snackbarHostState) {
        Scaffold(
            containerColor = com.snapshop.app.ui.theme.BackgroundLight,
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        },
                        onWishlistClick = {
                            navController.navigate(Screen.Wishlist.route)
                        },
                        onRecentlyViewedClick = {
                            navController.navigate(Screen.RecentlyViewed.route)
                        },
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

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
                        try {
                            URLDecoder.decode(it, "UTF-8")
                        } catch (e: Exception) {
                            it
                        }
                    }

                    ProductSearchScreen(
                        initialQuery = query,
                        viewModel = searchViewModel,
                        onProductClick = { product ->
                            activeProduct = product
                            navController.navigate(Screen.ProductDetails.route)
                        },
                        onCameraClick = {
                            navController.navigate(Screen.ImageSearch.createRoute("CAMERA"))
                        },
                        onGalleryClick = {
                            navController.navigate(Screen.ImageSearch.createRoute("GALLERY"))
                        },
                        onOpenMerchantLink = { buyUrl ->
                            openMerchantLink(
                                context = activity,
                                buyUrl = buyUrl,
                                snackbarHostState = snackbarHostState,
                                scope = scope
                            )
                        }
                    )
                }

                composable(Screen.ProductDetails.route) {
                    val currentSelectedProduct = activeProduct
                    if (currentSelectedProduct != null) {
                        ProductDetailsScreen(
                            product = currentSelectedProduct,
                            onBackClick = { navController.popBackStack() },
                            onRelatedProductClick = { related ->
                                activeProduct = related
                            },
                            onOpenMerchantLink = { buyUrl ->
                                openMerchantLink(
                                    context = activity,
                                    buyUrl = buyUrl,
                                    snackbarHostState = snackbarHostState,
                                    scope = scope
                                )
                            }
                        )
                    } else {
                        navController.popBackStack()
                    }
                }

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
                        onVisualSearchComplete = { query, result ->
                            searchViewModel.setVisualSearchResults(query, result.products)
                            navController.navigate(Screen.SearchResults.createRoute(query)) {
                                popUpTo(Screen.ImageSearch.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Wishlist.route) {
                    WishlistScreen(
                        onProductClick = { product ->
                            activeProduct = product
                            navController.navigate(Screen.ProductDetails.route)
                        },
                        onOpenMerchantLink = { buyUrl ->
                            openMerchantLink(
                                context = activity,
                                buyUrl = buyUrl,
                                snackbarHostState = snackbarHostState,
                                scope = scope
                            )
                        }
                    )
                }

                composable(Screen.SearchHistory.route) {
                    HistoryScreen(
                        onSearchQueryClick = { query ->
                            navController.navigate(Screen.SearchResults.createRoute(query))
                        }
                    )
                }

                composable(Screen.RecentlyViewed.route) {
                    RecentlyViewedScreen(
                        onProductClick = { product ->
                            activeProduct = product
                            navController.navigate(Screen.ProductDetails.route)
                        },
                        onOpenMerchantLink = { buyUrl ->
                            openMerchantLink(
                                context = activity,
                                buyUrl = buyUrl,
                                snackbarHostState = snackbarHostState,
                                scope = scope
                            )
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}
