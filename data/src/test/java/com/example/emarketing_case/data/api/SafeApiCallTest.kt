package com.example.emarketing_case.data.api

import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SafeApiCallTest {
    @Test
    fun `returns success for a successful request`() = runBlocking {
        val result = safeApiCall { "response" }

        assertEquals(AppResult.Success("response"), result)
    }

    @Test
    fun `maps a timeout to timeout error`() = runBlocking {
        val result = safeApiCall<String> { throw SocketTimeoutException() }

        assertEquals(AppResult.Failure(AppError.Timeout), result)
    }

    @Test
    fun `maps io exception to no connection error`() = runBlocking {
        val result = safeApiCall<String> { throw IOException() }

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
    }
}
