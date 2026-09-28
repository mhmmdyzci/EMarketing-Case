package com.example.emarketing_case.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.theme.AppHomeAccent
import com.example.emarketing_case.presentation.theme.AppHomeCardBorder
import com.example.emarketing_case.presentation.theme.AppHomeCardGlow
import com.example.emarketing_case.presentation.theme.AppHomeCardGradient

@Composable
internal fun CampaignBanner(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, shape)
            .clip(shape)
            .background(AppHomeCardGradient)
            .drawWithCache {
                val glow = Brush.radialGradient(
                    colors = listOf(AppHomeCardGlow, Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.5f,
                )
                onDrawBehind { drawRect(glow) }
            }
            .border(1.dp, AppHomeCardBorder, shape),
    ) {
        Column(
            modifier = Modifier.padding(21.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = AppHomeAccent.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, AppHomeAccent.copy(alpha = 0.20f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_home_offer),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(rotationZ = 12f),
                        tint = AppHomeAccent,
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 7.dp),
            )
        }
    }
}
