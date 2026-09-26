package com.example.emarketing_case.presentation.error

import androidx.annotation.StringRes
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.presentation.R
import javax.inject.Inject

class AppErrorMessageMapper @Inject constructor() {

    @StringRes
    fun map(
        error: AppError,
        @StringRes unauthorizedMessageRes: Int = R.string.error_unauthorized,
    ): Int =
        when (error) {
            AppError.Timeout -> R.string.error_timeout
            AppError.Unauthorized -> unauthorizedMessageRes
            AppError.Server -> R.string.error_server
            AppError.InvalidData -> R.string.error_invalid_data
            AppError.NoConnection -> R.string.error_no_connection
            AppError.Storage -> R.string.error_storage
            AppError.Unknown -> R.string.error_unknown
        }
}
