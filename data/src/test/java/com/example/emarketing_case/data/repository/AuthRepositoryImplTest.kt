package com.example.emarketing_case.data.repository

import com.example.emarketing_case.data.api.AuthApiService
import com.example.emarketing_case.data.dto.LoginRequestDto
import com.example.emarketing_case.data.dto.LoginResponseDto
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.repository.TokenStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryImplTest {
    @Test
    fun `returns mapped session when login succeeds`() = runBlocking {
        val tokenStorage = FakeTokenStorage()
        val apiService = FakeAuthApiService(
            response = LoginResponseDto(
                accessToken = "access-token",
                refreshToken = "refresh-token",
            ),
        )
        val repository = AuthRepositoryImpl(apiService, tokenStorage)

        val result = repository.login(LoginCredentials("emilys", "emilyspass"))

        assertEquals(LoginRequestDto("emilys", "emilyspass"), apiService.request)
        assertEquals(
            AppResult.Success(AuthSession("access-token", "refresh-token")),
            result,
        )
        assertEquals(AuthSession("access-token", "refresh-token"), tokenStorage.savedSession)
    }

    @Test
    fun `maps api exception to app error`() = runBlocking {
        val tokenStorage = FakeTokenStorage()
        val repository = AuthRepositoryImpl(
            FakeAuthApiService(exception = java.io.IOException("Network unavailable")),
            tokenStorage,
        )

        val result = repository.login(LoginCredentials("emilys", "emilyspass"))

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
        assertEquals(null, tokenStorage.savedSession)
    }

    @Test
    fun `returns storage error when token persistence fails`() = runBlocking {
        val repository = AuthRepositoryImpl(
            authApiService = FakeAuthApiService(
                response = LoginResponseDto(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                ),
            ),
            tokenStorage = FakeTokenStorage(exception = IllegalStateException("Storage unavailable")),
        )

        val result = repository.login(LoginCredentials("emilys", "emilyspass"))

        assertEquals(AppResult.Failure(AppError.Storage), result)
    }

    private class FakeAuthApiService(
        private val response: LoginResponseDto? = null,
        private val exception: Exception? = null,
    ) : AuthApiService {
        var request: LoginRequestDto? = null

        override suspend fun login(request: LoginRequestDto): LoginResponseDto {
            this.request = request
            exception?.let { throw it }
            return requireNotNull(response)
        }
    }

    private class FakeTokenStorage(
        private val exception: Exception? = null,
    ) : TokenStorage {
        var savedSession: AuthSession? = null

        override suspend fun save(session: AuthSession) {
            exception?.let { throw it }
            savedSession = session
        }

        override suspend fun restoreIfNeeded() = Unit

        override fun currentAccessToken(): String? = null

        override suspend fun clear() = Unit
    }
}
