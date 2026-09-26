package com.example.emarketing_case.data.mapper

import com.example.emarketing_case.data.dto.LoginRequestDto
import com.example.emarketing_case.data.dto.LoginResponseDto
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials

internal fun LoginCredentials.toLoginRequestDto(): LoginRequestDto =
    LoginRequestDto(
        username = username,
        password = password,
    )

internal fun LoginResponseDto.toAuthSession(): AuthSession =
    AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
