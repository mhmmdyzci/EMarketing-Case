package com.example.emarketing_case.presentation.auth.login

import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.usecase.LoginUseCase
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.error.AppErrorMessageMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LoginViewModelTest {
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
    fun `clears loading state when login succeeds`() = runTest {
        val viewModel = LoginViewModel(
            LoginUseCase(FakeAuthRepository(AppResult.Success(AuthSession("access", "refresh")))),
            AppErrorMessageMapper(),
        )

        viewModel.login(username = "emilys", password = "emilyspass")

        assertEquals(LoginUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `shows invalid credentials error when login is unauthorized`() = runTest {
        val viewModel = LoginViewModel(
            LoginUseCase(FakeAuthRepository(AppResult.Failure(AppError.Unauthorized))),
            AppErrorMessageMapper(),
        )

        viewModel.login(username = "emilys", password = "invalid-password")

        assertEquals(LoginUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(
            LoginUiState.Error(R.string.login_error_invalid_credentials),
            viewModel.uiState.value,
        )
    }

    private class FakeAuthRepository(
        private val result: AppResult<AuthSession>,
    ) : AuthRepository {
        override suspend fun login(credentials: LoginCredentials): AppResult<AuthSession> = result
    }
}
