package com.example.emarketing_case.domain.repository

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): AppResult<AuthSession>
}
