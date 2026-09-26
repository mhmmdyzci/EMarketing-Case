package com.example.emarketing_case.data.api

import com.example.emarketing_case.domain.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

internal class AuthorizationInterceptor(
    private val tokenStorage: TokenStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = tokenStorage.currentAccessToken()
        val request = chain.request().newBuilder().apply {
            accessToken?.takeIf(String::isNotBlank)?.let { token ->
                header(AUTHORIZATION_HEADER, "$BEARER_PREFIX $token")
            }
        }.build()

        return chain.proceed(request)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"
    }
}
