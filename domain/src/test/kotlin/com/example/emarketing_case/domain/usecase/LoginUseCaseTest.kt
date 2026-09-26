package com.example.emarketing_case.domain.usecase

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginUseCaseTest {
    @Test
    fun `forwards credentials to repository`() = runBlocking {
        val credentials = LoginCredentials(username = "emilys", password = "emilyspass")
        val expectedSession = AuthSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )
        val repository = RecordingAuthRepository(AppResult.Success(expectedSession))

        val result = LoginUseCase(repository)(credentials)

        assertEquals(credentials, repository.receivedCredentials)
        assertEquals(AppResult.Success(expectedSession), result)
    }

    private class RecordingAuthRepository(
        private val result: AppResult<AuthSession>,
    ) : AuthRepository {
        var receivedCredentials: LoginCredentials? = null

        override suspend fun login(credentials: LoginCredentials): AppResult<AuthSession> {
            receivedCredentials = credentials
            return result
        }
    }
}
