package com.example.emarketing_case.data.api

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.repository.TokenStorage
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

internal class SessionAuthenticator(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
) : Authenticator {
    private val refreshLock = Any()
    private var refreshAttempt = 0L
    private var lastRefreshedToken: String? = null

    fun authorizeRequest(request: Request): Request = synchronized(refreshLock) {
        request.withAccessToken(tokenStorage.currentAccessToken())
            .newBuilder()
            .tag(AuthorizationAttempt::class.java, AuthorizationAttempt(refreshAttempt))
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        val attempt = request.tag(AuthorizationAttempt::class.java) ?: return null
        if (attempt.retried || request.body?.isOneShot() == true || request.body?.isDuplex() == true) {
            return null
        }
        val failedToken = request.header("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return synchronized(refreshLock) {
            val currentToken = tokenStorage.currentAccessToken() ?: return@synchronized null
            if (currentToken != failedToken) {
                // Only replay with credentials renewed here, never with a different login.
                if (currentToken != lastRefreshedToken) return@synchronized null
                return@synchronized request.retryWith(currentToken)
            }

            // Requests from the same attempt share its failure as well as its success.
            if (attempt.number < refreshAttempt) {
                return@synchronized if (currentToken == lastRefreshedToken) {
                    request.retryWith(currentToken)
                } else {
                    null
                }
            }

            refreshAttempt++
            lastRefreshedToken = null
            // OkHttp's callback is synchronous. Auth API calls use a separate dispatcher.
            val result = try {
                runBlocking { authRepository.refreshSession(failedAccessToken = failedToken) }
            } catch (_: CancellationException) {
                return@synchronized null
            }
            if (result !is AppResult.Success) return@synchronized null
            val renewedToken = result.data.accessToken
            if (tokenStorage.currentAccessToken() != renewedToken) return@synchronized null
            lastRefreshedToken = renewedToken
            request.retryWith(renewedToken)
        }
    }

    private fun Request.retryWith(token: String): Request = withAccessToken(token)
        .newBuilder()
        .tag(AuthorizationAttempt::class.java, AuthorizationAttempt(refreshAttempt, retried = true))
        .build()

    private class AuthorizationAttempt(val number: Long, val retried: Boolean = false)
}
