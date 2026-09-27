package com.example.emarketing_case.presentation.session

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.model.SessionState
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.usecase.LogoutUseCase
import com.example.emarketing_case.domain.usecase.ObserveSessionStateUseCase
import com.example.emarketing_case.domain.usecase.RestoreSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `restores session once when created`() = runTest {
        val repository = FakeAuthRepository()

        SessionViewModel(
            observeSessionState = ObserveSessionStateUseCase(repository),
            restoreSession = RestoreSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )
        advanceUntilIdle()

        assertEquals(1, repository.restoreCount)
    }

    @Test
    fun `exposes repository session state`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SessionViewModel(
            observeSessionState = ObserveSessionStateUseCase(repository),
            restoreSession = RestoreSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        repository.mutableSessionState.value = SessionState.Authenticated

        assertEquals(SessionState.Authenticated, viewModel.sessionState.value)
    }

    @Test
    fun `logs out once while logout is in progress`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SessionViewModel(
            observeSessionState = ObserveSessionStateUseCase(repository),
            restoreSession = RestoreSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.logout()
        viewModel.logout()
        advanceUntilIdle()

        assertEquals(1, repository.logoutCount)
        assertEquals(SessionState.Unauthenticated, viewModel.sessionState.value)
    }

    private class FakeAuthRepository : AuthRepository {
        val mutableSessionState = MutableStateFlow<SessionState>(SessionState.Unknown)
        override val sessionState = mutableSessionState
        var restoreCount = 0
            private set
        var logoutCount = 0
            private set

        override suspend fun restoreSession(): AppResult<Unit> {
            restoreCount += 1
            return AppResult.Success(Unit)
        }

        override suspend fun login(credentials: LoginCredentials): AppResult<AuthSession> =
            error("Login is not expected in session tests")

        override suspend fun refreshSession(failedAccessToken: String?): AppResult<AuthSession> =
            error("Refresh is not expected in session tests")

        override suspend fun logout(): AppResult<Unit> {
            logoutCount += 1
            mutableSessionState.value = SessionState.Unauthenticated
            return AppResult.Success(Unit)
        }
    }
}
