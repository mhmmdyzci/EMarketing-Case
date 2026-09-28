package com.example.emarketing_case.domain.repository

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.model.SessionState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionState: StateFlow<SessionState>

    suspend fun login(credentials: LoginCredentials): AppResult<AuthSession>

    suspend fun restoreSession(): AppResult<Unit>

    /** Renews stored credentials and returns the new session only after persistence succeeds. */
    suspend fun refreshSession(failedAccessToken: String? = null): AppResult<AuthSession>

    suspend fun logout(): AppResult<Unit>
}
