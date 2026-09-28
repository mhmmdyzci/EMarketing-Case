package com.example.emarketing_case.domain.usecase

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.repository.AuthRepository
import javax.inject.Inject

class RestoreSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = authRepository.restoreSession()
}
