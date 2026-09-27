package com.example.emarketing_case.domain.usecase

import com.example.emarketing_case.domain.model.SessionState
import com.example.emarketing_case.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveSessionStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): StateFlow<SessionState> = authRepository.sessionState
}
