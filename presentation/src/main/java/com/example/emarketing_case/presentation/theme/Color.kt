package com.example.emarketing_case.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val AppBackground = Color(0xFF0B0F17)
val AppSurface = Color(0xCC101725)
val AppOutline = Color(0xFF28354D)
val AppTextPrimary = Color(0xFFF2F3F5)
val AppTextSecondary = Color(0xFF94A3B8)
val AppTextPlaceholder = Color(0xFF64748B)
val AppError = Color(0xFFFCA5A5)

val AppPrimary = Color(0xFF7C3AED)
val AppSecondary = Color(0xFF9333EA)
val AppTertiary = Color(0xFFC084FC)

val AppBrandGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE0E7FF),
        AppTertiary,
        Color(0xFFA855F7),
    ),
)

val AppPrimaryActionGradient = Brush.horizontalGradient(
    colors = listOf(AppPrimary, AppSecondary),
)

val AppPrimaryActionBorder = Color(0x33C084FC)
val AppPrimaryActionShadow = Color(0x4D581C87)
val AppTopAmbientGlow = Color(0x1F8B5CF6)
val AppBottomAmbientGlow = Color(0x146366F1)

val AppSoftAction = Color(0xFFB69DF8)
val AppSoftActionContent = Color(0xFF131127)
val AppSoftActionShadow = Color(0x40A892FE)
val AppHomeCardBorder = Color(0x14FFFFFF)
val AppHomeCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xBF1B2436),
        Color(0xF2131B2B),
    ),
)
val AppHomeCardGlow = Color(0x26A892FE)
val AppCategorySurface = Color(0xFF172030)
val AppCategoryBorder = Color(0x1AFFFFFF)
val AppHomeIcon = Color(0xFFE2E8F0)
val AppHomeAccent = Color(0xFFA892FE)

val AppTopBarSurface = Color(0xF20B111B)
val AppTopBarBorder = Color(0x66233045)
val AppProductSurface = Color(0xFF151E2E)
val AppProductBorder = Color(0xFF233045)
val AppProductThumbnailSurface = Color(0xFF1F2A3D)
val AppProductThumbnailBorder = Color(0x66334155)
val AppProductRating = Color(0xFFF59E0B)
val AppProductPrice = Color(0xFF38BDF8)
val AppProductSeparator = Color(0xFF475569)
