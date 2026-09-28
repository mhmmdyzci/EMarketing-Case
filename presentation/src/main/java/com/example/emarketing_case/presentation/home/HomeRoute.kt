package com.example.emarketing_case.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeRoute(
    onProductsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        onProductsClick = onProductsClick,
        onLogoutClick = onLogoutClick,
        modifier = modifier,
    )
}
