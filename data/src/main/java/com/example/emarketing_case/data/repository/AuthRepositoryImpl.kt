package com.example.emarketing_case.data.repository

import com.example.emarketing_case.data.api.AuthApiService
import com.example.emarketing_case.data.api.safeApiCall
import com.example.emarketing_case.data.mapper.toAuthSession
import com.example.emarketing_case.data.mapper.toLoginRequestDto
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.repository.TokenStorage
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

internal class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage,
) : AuthRepository {
    override suspend fun login(credentials: LoginCredentials): AppResult<AuthSession> {
        return when (
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

    private suspend fun saveSession(session: AuthSession): AppResult<AuthSession> =
        try {
            tokenStorage.save(session)
            AppResult.Success(session)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            AppResult.Failure(AppError.Storage)
        }
}
