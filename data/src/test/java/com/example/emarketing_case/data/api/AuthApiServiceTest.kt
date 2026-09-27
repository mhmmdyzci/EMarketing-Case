package com.example.emarketing_case.data.api

import com.example.emarketing_case.data.dto.RefreshTokenRequestDto
import com.example.emarketing_case.data.dto.RefreshTokenResponseDto
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthApiServiceTest {
    @Test
    fun `refresh posts token to auth refresh and decodes rotated tokens`() = runBlocking {
        var recordedRequest: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                recordedRequest = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        """{"accessToken":"new-access","refreshToken":"new-refresh"}"""
                            .toResponseBody("application/json".toMediaType()),
                    )
                    .build()
            }
            .build()
        try {
            val api = Retrofit.Builder()
                .baseUrl("https://example.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService::class.java)

            val response = api.refresh(RefreshTokenRequestDto("stored-refresh"))

            val request = requireNotNull(recordedRequest)
            val body = Buffer().also { requireNotNull(request.body).writeTo(it) }.readUtf8()
            assertEquals("POST", request.method)
            assertEquals("/auth/refresh", request.url.encodedPath)
            assertEquals(
                JsonParser.parseString("""{"refreshToken":"stored-refresh"}"""),
                JsonParser.parseString(body),
            )
            assertEquals(RefreshTokenResponseDto("new-access", "new-refresh"), response)

            api.validateSession()

            val validationRequest = requireNotNull(recordedRequest)
            assertEquals("GET", validationRequest.method)
            assertEquals("/auth/me", validationRequest.url.encodedPath)
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }
}
