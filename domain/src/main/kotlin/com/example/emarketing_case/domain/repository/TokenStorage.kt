package com.example.emarketing_case.domain.repository

import com.example.emarketing_case.domain.model.AuthSession

interface TokenStorage {
    suspend fun save(session: AuthSession)
    suspend fun getSession(): AuthSession?
    fun currentAccessToken(): String?
    suspend fun clear()
}
