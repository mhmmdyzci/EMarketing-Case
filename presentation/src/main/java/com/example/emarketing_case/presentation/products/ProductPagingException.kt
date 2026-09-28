package com.example.emarketing_case.presentation.products

import com.example.emarketing_case.domain.model.AppError

class ProductPagingException(
    val error: AppError,
) : Exception()

internal fun Throwable.toAppError(): AppError =
    (this as? ProductPagingException)?.error ?: AppError.Unknown
