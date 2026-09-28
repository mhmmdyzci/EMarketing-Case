package com.example.emarketing_case.presentation.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.components.AppButton
import com.example.emarketing_case.presentation.components.AppTopBar
import com.example.emarketing_case.presentation.error.toMessageRes
import com.example.emarketing_case.presentation.products.components.ProductCard
import com.example.emarketing_case.presentation.theme.AppProductPrice
import com.example.emarketing_case.presentation.theme.appBackground

@Composable
fun ProductsScreen(
    products: LazyPagingItems<Product>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .appBackground()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.products_title),
            backContentDescription = stringResource(R.string.products_back),
            onBackClick = onBackClick,
        )

        when (val refreshState = products.loadState.refresh) {
            LoadState.Loading -> ProductsLoading(modifier = Modifier.fillMaxSize())
            is LoadState.Error -> ProductsError(
                message = stringResource(refreshState.error.toAppError().toMessageRes()),
                onRetryClick = products::retry,
                modifier = Modifier.fillMaxSize(),
            )
            is LoadState.NotLoading -> {
                if (products.itemCount == 0) {
                    EmptyProducts(modifier = Modifier.fillMaxSize())
                } else {
                    ProductsList(
                        products = products,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductsList(
    products: LazyPagingItems<Product>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(
            count = products.itemCount,
            key = products.itemKey(Product::id),
            contentType = products.itemContentType { Product::class },
        ) { index ->
            products[index]?.let { product ->
                ProductCard(
                    product = product,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        when (val appendState = products.loadState.append) {
            LoadState.Loading -> item(key = "append_loading") {
                ProductsLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
            }
            is LoadState.Error -> item(key = "append_error") {
                AppendError(
                    message = stringResource(appendState.error.toAppError().toMessageRes()),
                    onRetryClick = products::retry,
                )
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun ProductsLoading(
    modifier: Modifier = Modifier,
) {
    val loadingDescription = stringResource(R.string.products_loading)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics {
                contentDescription = loadingDescription
            },
            color = AppProductPrice,
        )
    }
}

@Composable
private fun ProductsError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        AppButton(
            text = stringResource(R.string.products_retry),
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )
    }
}

@Composable
private fun EmptyProducts(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.products_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun AppendError(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetryClick) {
            Text(stringResource(R.string.products_retry))
        }
    }
}
