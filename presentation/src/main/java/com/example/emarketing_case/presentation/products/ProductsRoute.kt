package com.example.emarketing_case.presentation.products

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun ProductsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductsViewModel = hiltViewModel(),
) {
    val products = viewModel.products.collectAsLazyPagingItems()

    ProductsScreen(
        products = products,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
