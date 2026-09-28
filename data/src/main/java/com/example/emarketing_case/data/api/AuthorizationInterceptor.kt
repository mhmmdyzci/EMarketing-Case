package com.example.emarketing_case.data.api

import com.example.emarketing_case.domain.repository.TokenStorage
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class AuthorizationInterceptor(
    private val tokenStorage: TokenStorage,
    private val sessionAuthenticator: SessionAuthenticator? = null,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = sessionAuthenticator?.authorizeRequest(chain.request())
            ?: chain.request().withAccessToken(tokenStorage.currentAccessToken())

        return chain.proceed(request)
    }

}

internal fun Request.withAccessToken(accessToken: String?): Request = newBuilder().apply {
    if (accessToken.isNullOrBlank()) {
        removeHeader("Authorization")
    } else {
        header("Authorization", "Bearer $accessToken")
    }
}.build()
