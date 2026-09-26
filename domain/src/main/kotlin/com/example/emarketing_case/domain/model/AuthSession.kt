package com.example.emarketing_case.domain.model

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
)
