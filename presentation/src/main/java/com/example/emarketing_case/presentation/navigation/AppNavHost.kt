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

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    contentPadding: PaddingValues,
    onProductsClick: () -> Unit,
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
                onProductsClick = onProductsClick,
                onLogoutClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
    }
}
