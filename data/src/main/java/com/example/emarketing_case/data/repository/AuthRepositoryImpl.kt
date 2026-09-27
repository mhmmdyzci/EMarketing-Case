package com.example.emarketing_case.data.repository

import com.example.emarketing_case.data.api.AuthApiService
import com.example.emarketing_case.data.api.safeApiCall
import com.example.emarketing_case.data.dto.RefreshTokenRequestDto
import com.example.emarketing_case.data.mapper.toAuthSession
import com.example.emarketing_case.data.mapper.toAuthSessionOrNull
import com.example.emarketing_case.data.mapper.toLoginRequestDto
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.model.SessionState
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.repository.TokenStorage
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage,
) : AuthRepository {
    private val sessionMutex = Mutex()
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val sessionState = _sessionState.asStateFlow()

    override suspend fun login(credentials: LoginCredentials): AppResult<AuthSession> =
        sessionMutex.withLock {
            when (
                val result = safeApiCall {
                    authApiService
                        .login(credentials.toLoginRequestDto())
                        .toAuthSession()
                }
            ) {
                is AppResult.Success -> saveSession(result.data)
                is AppResult.Failure -> result
            }
        }

    override suspend fun restoreSession(): AppResult<Unit> = sessionMutex.withLock {
        val session = when (val result = readSession()) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> return@withLock result
        }
        if (session == null) {
            _sessionState.value = SessionState.Unauthenticated
            return@withLock AppResult.Success(Unit)
        }

        when (val result = safeApiCall { authApiService.validateSession() }) {
            is AppResult.Success -> {
                _sessionState.value = SessionState.Authenticated
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> {
                if (result.error != AppError.Unauthorized) return@withLock result
                when (val refreshed = refreshStoredSession(session)) {
                    is AppResult.Success -> AppResult.Success(Unit)
                    is AppResult.Failure -> refreshed
                }
            }
        }
    }

    override suspend fun refreshSession(failedAccessToken: String?): AppResult<AuthSession> {
        val previousSession = when (val result = readSession()) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> return result
        }

        return sessionMutex.withLock {
            val currentSession = when (val result = readSession()) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> return@withLock result
            }
            if (currentSession == null) {
                _sessionState.value = SessionState.Unauthenticated
                return@withLock AppResult.Failure(AppError.Unauthorized)
            }
            if (failedAccessToken != null && currentSession.accessToken != failedAccessToken) {
                return@withLock AppResult.Failure(AppError.Unauthorized)
            }
            // A preceding login or refresh already replaced the captured session.
            if (currentSession != previousSession) {
                return@withLock AppResult.Success(currentSession)
            }
            refreshStoredSession(currentSession)
        }
    }

    override suspend fun logout(): AppResult<Unit> = sessionMutex.withLock {
        clearSession()
    }

    private suspend fun refreshStoredSession(session: AuthSession): AppResult<AuthSession> {
        if (session.refreshToken.isBlank()) return rejectSession()

        return when (
            val result = safeApiCall {
                authApiService.refresh(RefreshTokenRequestDto(session.refreshToken)).toAuthSessionOrNull()
            }
        ) {
            is AppResult.Success -> {
                val renewedSession = result.data ?: return AppResult.Failure(AppError.InvalidData)
                saveSession(renewedSession)
            }
            is AppResult.Failure -> {
                if (result.error == AppError.Unauthorized) rejectSession() else result
            }
        }
    }

    private suspend fun rejectSession(): AppResult.Failure =
        when (val result = clearSession()) {
            is AppResult.Success -> AppResult.Failure(AppError.Unauthorized)
            is AppResult.Failure -> result
        }

    private suspend fun readSession(): AppResult<AuthSession?> =
        try {
            AppResult.Success(tokenStorage.getSession())
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            AppResult.Failure(AppError.Storage)
        }

    private suspend fun saveSession(session: AuthSession): AppResult<AuthSession> =
        try {
            // Finish persistence and state publication together even if the caller leaves.
            withContext(NonCancellable) {
                tokenStorage.save(session)
                _sessionState.value = SessionState.Authenticated
                AppResult.Success(session)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            AppResult.Failure(AppError.Storage)
        }

    private suspend fun clearSession(): AppResult<Unit> =
        try {
            withContext(NonCancellable) {
                tokenStorage.clear()
                _sessionState.value = SessionState.Unauthenticated
                AppResult.Success(Unit)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            AppResult.Failure(AppError.Storage)
        }
}
