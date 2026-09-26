package com.example.emarketing_case.data.mapper

import com.example.emarketing_case.data.dto.LoginResponseDto
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthMapperTest {
    @Test
    fun `maps credentials to login request`() {
        val credentials = LoginCredentials(username = "emilys", password = "emilyspass")

        val request = credentials.toLoginRequestDto()

        assertEquals("emilys", request.username)
        assertEquals("emilyspass", request.password)
    }

    @Test
    fun `maps login response to auth session`() {
        val response = LoginResponseDto(
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )

        val session = response.toAuthSession()

        assertEquals(AuthSession("access-token", "refresh-token"), session)
    }
}
