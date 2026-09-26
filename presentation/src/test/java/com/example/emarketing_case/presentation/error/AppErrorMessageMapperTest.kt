package com.example.emarketing_case.presentation.error

import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.presentation.R
import org.junit.Assert.assertEquals
import org.junit.Test

class AppErrorMessageMapperTest {
    private val mapper = AppErrorMessageMapper()

    @Test
    fun `maps application errors to shared message resources`() {
        val expectedMessages = mapOf(
            AppError.Timeout to R.string.error_timeout,
            AppError.Unauthorized to R.string.error_unauthorized,
            AppError.Server to R.string.error_server,
            AppError.InvalidData to R.string.error_invalid_data,
            AppError.NoConnection to R.string.error_no_connection,
            AppError.Storage to R.string.error_storage,
            AppError.Unknown to R.string.error_unknown,
        )

        expectedMessages.forEach { (error, expectedMessage) ->
            assertEquals(expectedMessage, mapper.map(error))
        }
    }

    @Test
    fun `uses contextual unauthorized message when provided`() {
        val message = mapper.map(
            error = AppError.Unauthorized,
            unauthorizedMessageRes = R.string.login_error_invalid_credentials,
        )

        assertEquals(R.string.login_error_invalid_credentials, message)
    }
}
