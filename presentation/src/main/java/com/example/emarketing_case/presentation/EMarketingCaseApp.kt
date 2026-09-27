package com.example.emarketing_case.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.emarketing_case.domain.model.SessionState
import com.example.emarketing_case.presentation.navigation.AppNavHost
import com.example.emarketing_case.presentation.navigation.ScreenRoutes
import com.example.emarketing_case.presentation.session.SessionLoadingScreen
import com.example.emarketing_case.presentation.session.SessionViewModel

@Composable
fun EMarketingCaseApp(
    modifier: Modifier = Modifier,
    onProductsClick: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { contentPadding ->
        when (sessionState) {
            SessionState.Unknown -> SessionLoadingScreen(
                modifier = Modifier.padding(contentPadding),
            )
            SessionState.Authenticated,
            SessionState.Unauthenticated,
            -> SessionNavigation(
                sessionState = sessionState,
                onProductsClick = onProductsClick,
                onLogoutClick = sessionViewModel::logout,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun SessionNavigation(
    sessionState: SessionState,
    onProductsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    val navController = rememberNavController()
    val startDestination = remember { sessionState.toRoute() }

    LaunchedEffect(sessionState) {
        navController.navigateToSession(sessionState)
    }

    AppNavHost(
        navController = navController,
        startDestination = startDestination,
        contentPadding = contentPadding,
        onProductsClick = onProductsClick,
        onLogoutClick = onLogoutClick,
    )
}

private fun NavHostController.navigateToSession(sessionState: SessionState) {
    val destination = sessionState.toRoute()
    val currentRoute = currentBackStackEntry?.destination?.route ?: return
    if (currentRoute == destination) return

    navigate(destination) {
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

private fun SessionState.toRoute(): String = when (this) {
    SessionState.Authenticated -> ScreenRoutes.HOME
    SessionState.Unauthenticated -> ScreenRoutes.LOGIN
    SessionState.Unknown -> error("Unknown session state has no navigation destination")
}
