package com.example.emarketing_case.data.api

import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthorizationInterceptorTest {
    @Test
    fun `adds authorization header when access token exists`() {
        val request = executeRequest(accessToken = "access-token")

        assertEquals("Bearer access-token", request.header(AUTHORIZATION_HEADER))
    }

    @Test
    fun `does not add authorization header when access token is missing`() {
        val request = executeRequest(accessToken = null)

        assertNull(request.header(AUTHORIZATION_HEADER))
    }

    private fun executeRequest(accessToken: String?): Request {
        var interceptedRequest: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(FakeTokenStorage(accessToken)))
            .addInterceptor { chain ->
                interceptedRequest = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody())
                    .build()
            }
            .build()

        client.newCall(
            Request.Builder()
                .url("https://example.com")
                .build(),
        ).execute().close()

        return requireNotNull(interceptedRequest)
    }

    private class FakeTokenStorage(
        private val accessToken: String?,
    ) : TokenStorage {
        override suspend fun save(session: AuthSession) = Unit

        override suspend fun getSession(): AuthSession? =
            accessToken?.let { AuthSession(it, "refresh-token") }

        override fun currentAccessToken(): String? = accessToken

        override suspend fun clear() = Unit
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
