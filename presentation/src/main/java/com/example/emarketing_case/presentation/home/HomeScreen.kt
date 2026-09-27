package com.example.emarketing_case.presentation.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.FaceRetouchingNatural
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.components.AppButton
import com.example.emarketing_case.presentation.home.components.CampaignBanner
import com.example.emarketing_case.presentation.home.components.CategoryItem


private data class HomeCategory(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val homeCategories = listOf(
    HomeCategory(R.string.home_category_beauty, Icons.Outlined.FaceRetouchingNatural),
    HomeCategory(R.string.home_category_furniture, Icons.Outlined.Chair),
    HomeCategory(R.string.home_category_smartphones, Icons.Outlined.PhoneAndroid),
    HomeCategory(R.string.home_category_clothing, Icons.Outlined.Checkroom),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            actions = {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = stringResource(R.string.home_logout),
                    )
                }
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                CampaignBanner(
                    title = stringResource(R.string.home_campaign_title),
                    description = stringResource(R.string.home_campaign_description),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.home_categories_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = homeCategories,
                            key = { category -> category.labelRes },
                        ) { category ->
                            CategoryItem(
                                label = stringResource(category.labelRes),
                                icon = category.icon,
                            )
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_products_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.home_products_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AppButton(
                        text = stringResource(R.string.home_products_action),
                        onClick = onProductsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

