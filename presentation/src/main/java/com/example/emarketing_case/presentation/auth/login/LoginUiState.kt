package com.example.emarketing_case.presentation.auth.login

import androidx.annotation.StringRes

sealed interface LoginUiState {
    data object Idle : LoginUiState

    data object Loading : LoginUiState

    data class Error(
        @StringRes val messageRes: Int,
    ) : LoginUiState
}
