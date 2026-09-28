package com.example.emarketing_case.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.emarketing_case.presentation.theme.AppPrimaryActionBorder
import com.example.emarketing_case.presentation.theme.AppPrimaryActionGradient
import com.example.emarketing_case.presentation.theme.AppPrimaryActionShadow
import com.example.emarketing_case.presentation.theme.AppControlHeight
import com.example.emarketing_case.presentation.theme.AppSoftAction
import com.example.emarketing_case.presentation.theme.AppSoftActionContent
import com.example.emarketing_case.presentation.theme.AppSoftActionShadow

enum class AppButtonStyle {
    Primary,
    Soft,
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    style: AppButtonStyle = AppButtonStyle.Primary,
) {
    val visualStyle = appButtonVisualStyle(style)
    val isInteractive = enabled && !isLoading

    Box(
        modifier = modifier
            .height(AppControlHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .shadow(
                elevation = 14.dp,
                shape = visualStyle.shape,
                ambientColor = visualStyle.shadowColor,
                spotColor = visualStyle.shadowColor,
            )
            .clip(visualStyle.shape)
            .background(visualStyle.containerBrush)
            .border(
                width = 1.dp,
                color = visualStyle.borderColor,
                shape = visualStyle.shape,
            )
            .clickable(
                enabled = isInteractive,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = text },
                color = visualStyle.contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = visualStyle.contentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun appButtonVisualStyle(style: AppButtonStyle): AppButtonVisualStyle {
    return when (style) {
        AppButtonStyle.Primary -> AppButtonVisualStyle(
            shape = MaterialTheme.shapes.medium,
            containerBrush = AppPrimaryActionGradient,
            contentColor = Color.White,
            borderColor = AppPrimaryActionBorder,
            shadowColor = AppPrimaryActionShadow,
        )
        AppButtonStyle.Soft -> AppButtonVisualStyle(
            shape = MaterialTheme.shapes.extraLarge,
            containerBrush = Brush.linearGradient(listOf(AppSoftAction, AppSoftAction)),
            contentColor = AppSoftActionContent,
            borderColor = Color.Transparent,
            shadowColor = AppSoftActionShadow,
        )
    }
}

private data class AppButtonVisualStyle(
    val shape: Shape,
    val containerBrush: Brush,
    val contentColor: Color,
    val borderColor: Color,
    val shadowColor: Color,
)
