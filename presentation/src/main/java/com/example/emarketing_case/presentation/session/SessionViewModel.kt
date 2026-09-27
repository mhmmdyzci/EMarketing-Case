package com.example.emarketing_case.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emarketing_case.domain.usecase.LogoutUseCase
import com.example.emarketing_case.domain.usecase.ObserveSessionStateUseCase
import com.example.emarketing_case.domain.usecase.RestoreSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    observeSessionState: ObserveSessionStateUseCase,
    private val restoreSession: RestoreSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    val sessionState = observeSessionState()
    private var logoutJob: Job? = null

    init {
        viewModelScope.launch {
            restoreSession()
        }
    }

    fun logout() {
        if (logoutJob?.isActive == true) return

        logoutJob = viewModelScope.launch {
            logoutUseCase()
        }
    }
}
