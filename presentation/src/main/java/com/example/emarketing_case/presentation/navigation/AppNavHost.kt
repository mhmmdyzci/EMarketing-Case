package com.example.emarketing_case.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.emarketing_case.presentation.auth.login.LoginRoute
import com.example.emarketing_case.presentation.home.HomeRoute
import com.example.emarketing_case.presentation.products.ProductsRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    contentPadding: PaddingValues,
    onNavigateToProducts: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(ScreenRoutes.LOGIN) {
            LoginRoute(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
        composable(ScreenRoutes.HOME) {
            HomeRoute(
                onProductsClick = onNavigateToProducts,
                onLogoutClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
        composable(ScreenRoutes.PRODUCTS) {
            ProductsRoute(
                onBackClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
    }
}
