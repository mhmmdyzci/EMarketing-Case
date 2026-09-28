package com.example.emarketing_case.presentation.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.components.AppButton
import com.example.emarketing_case.presentation.components.AppButtonStyle
import com.example.emarketing_case.presentation.home.components.CampaignBanner
import com.example.emarketing_case.presentation.home.components.CategoryItem
import com.example.emarketing_case.presentation.theme.AppCategoryBorder
import com.example.emarketing_case.presentation.theme.AppCategorySurface
import com.example.emarketing_case.presentation.theme.appBackground

private data class HomeCategory(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
)

private val homeCategories = listOf(
    HomeCategory(R.string.home_category_beauty, R.drawable.ic_home_beauty),
    HomeCategory(R.string.home_category_furniture, R.drawable.ic_home_furniture),
    HomeCategory(R.string.home_category_smartphones, R.drawable.ic_home_smartphone),
    HomeCategory(R.string.home_category_clothing, R.drawable.ic_home_clothing),
)

@Composable
fun HomeScreen(
    onProductsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .appBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        HomeHeader(
            onLogoutClick = onLogoutClick,
            modifier = Modifier.padding(top = 10.dp, bottom = 34.dp),
        )

        CampaignBanner(
            title = stringResource(R.string.home_campaign_title),
            description = stringResource(R.string.home_campaign_description),
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.home_categories_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            homeCategories.forEach { category ->
                CategoryItem(
                    label = stringResource(category.labelRes),
                    iconRes = category.iconRes,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = stringResource(R.string.home_products_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.home_products_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 7.dp),
        )
        AppButton(
            text = stringResource(R.string.home_products_action),
            onClick = onProductsClick,
            style = AppButtonStyle.Soft,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 17.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HomeHeader(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        val logoutLabel = stringResource(R.string.home_logout)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(AppCategorySurface.copy(alpha = 0.60f))
                .border(
                    width = 1.dp,
                    color = AppCategoryBorder.copy(alpha = 0.50f),
                    shape = MaterialTheme.shapes.extraLarge,
                )
                .clickable(
                    role = Role.Button,
                    onClickLabel = logoutLabel,
                    onClick = onLogoutClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_logout),
                contentDescription = logoutLabel,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
