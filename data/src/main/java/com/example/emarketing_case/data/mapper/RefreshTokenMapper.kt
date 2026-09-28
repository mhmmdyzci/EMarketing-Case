package com.example.emarketing_case.data.mapper

import com.example.emarketing_case.data.dto.RefreshTokenResponseDto
import com.example.emarketing_case.domain.model.AuthSession

internal fun RefreshTokenResponseDto.toAuthSessionOrNull(): AuthSession? {
    val accessToken = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val refreshToken = refreshToken?.takeIf { it.isNotBlank() } ?: return null
    return AuthSession(accessToken = accessToken, refreshToken = refreshToken)
}
