package com.example.emarketing_case.presentation.products.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.theme.AppProductBorder
import com.example.emarketing_case.presentation.theme.AppProductPrice
import com.example.emarketing_case.presentation.theme.AppProductRating
import com.example.emarketing_case.presentation.theme.AppProductSeparator
import com.example.emarketing_case.presentation.theme.AppProductSurface
import com.example.emarketing_case.presentation.theme.AppProductThumbnailBorder
import com.example.emarketing_case.presentation.theme.AppProductThumbnailSurface
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val price = remember(product.price, locale) {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(USD_CURRENCY_CODE)
        }.format(product.price)
    }
    val category = remember(product.category, locale) {
        product.category
            .replace('-', ' ')
            .replaceFirstChar { character -> character.titlecase(locale) }
    }
    val shape = MaterialTheme.shapes.large

    Row(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = shape)
            .clip(shape)
            .background(AppProductSurface)
            .border(width = 1.dp, color = AppProductBorder, shape = shape)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductThumbnail(
            imageUrl = product.thumbnailUrl,
            contentDescription = stringResource(
                R.string.products_image_description,
                product.title,
            ),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProductMetadata(
                    rating = product.rating,
                    stock = product.stock,
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppProductPrice,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ProductThumbnail(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(112.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(AppProductThumbnailSurface)
            .border(
                width = 1.dp,
                color = AppProductThumbnailBorder,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(11.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ProductMetadata(
    rating: Double,
    stock: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_product_rating),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = AppProductRating,
            )
            Text(
                text = stringResource(R.string.products_rating, rating),
                style = MaterialTheme.typography.labelSmall,
                color = AppProductRating,
                maxLines = 1,
            )
        }
        Text(
            text = stringResource(R.string.products_metadata_separator),
            style = MaterialTheme.typography.bodySmall,
            color = AppProductSeparator,
        )
        Text(
            text = stringResource(R.string.products_stock, stock),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val USD_CURRENCY_CODE = "USD"
