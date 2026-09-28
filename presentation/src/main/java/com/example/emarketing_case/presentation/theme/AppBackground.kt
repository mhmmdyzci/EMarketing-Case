package com.example.emarketing_case.presentation.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithCache

fun Modifier.appBackground(): Modifier = drawWithCache {
    val topGlow = Brush.radialGradient(
        colors = listOf(AppTopAmbientGlow, Color.Transparent),
        center = Offset(x = size.width * 0.18f, y = size.height * 0.18f),
        radius = size.width * 0.92f,
    )
    val bottomGlow = Brush.radialGradient(
        colors = listOf(AppBottomAmbientGlow, Color.Transparent),
        center = Offset(x = size.width * 0.82f, y = size.height * 0.70f),
        radius = size.width * 0.72f,
    )

    onDrawBehind {
        drawRect(color = AppBackground)
        drawRect(brush = topGlow)
        drawRect(brush = bottomGlow)
    }
}
