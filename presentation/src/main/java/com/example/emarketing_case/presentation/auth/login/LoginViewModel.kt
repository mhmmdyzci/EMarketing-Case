package com.example.emarketing_case.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.usecase.LoginUseCase
import com.example.emarketing_case.presentation.R
import com.example.emarketing_case.presentation.error.AppErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val errorMessageMapper: AppErrorMessageMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) return

        val currentState = _uiState.value
        if (currentState is LoginUiState.Loading) return
        if (!_uiState.compareAndSet(currentState, LoginUiState.Loading)) return

        viewModelScope.launch {
            when (
                val result = loginUseCase(
                    LoginCredentials(
                        username = username,
                        password = password,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = LoginUiState.Idle
                }

                is AppResult.Failure -> {
                    _uiState.value = LoginUiState.Error(
                        messageRes = errorMessageMapper.map(
                            error = result.error,
                            unauthorizedMessageRes = R.string.login_error_invalid_credentials,
                        ),
                    )
                }
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is LoginUiState.Error) {
            _uiState.compareAndSet(currentState, LoginUiState.Idle)
        }
    }

}
