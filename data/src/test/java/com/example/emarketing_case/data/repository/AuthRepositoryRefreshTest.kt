package com.example.emarketing_case.data.repository

import com.example.emarketing_case.data.api.AuthApiService
import com.example.emarketing_case.data.dto.LoginRequestDto
import com.example.emarketing_case.data.dto.LoginResponseDto
import com.example.emarketing_case.data.dto.RefreshTokenRequestDto
import com.example.emarketing_case.data.dto.RefreshTokenResponseDto
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.LoginCredentials
import com.example.emarketing_case.domain.model.SessionState
import com.example.emarketing_case.domain.repository.TokenStorage
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthRepositoryRefreshTest {
    private val originalSession = AuthSession("old-access", "old-refresh")
    private val renewedSession = AuthSession("new-access", "new-refresh")

    @Test
    fun `refresh sends stored token and persists both new tokens before success`() = runBlocking {
        val api = FakeAuthApiService()
        val storage = FakeTokenStorage(originalSession)

        val result = AuthRepositoryImpl(api, storage).refreshSession()

        assertEquals(RefreshTokenRequestDto(originalSession.refreshToken), api.request)
        assertEquals(AppResult.Success(renewedSession), result)
        assertEquals(renewedSession, storage.session)
        assertEquals(renewedSession.accessToken, storage.currentAccessToken())
        assertEquals(1, storage.saveCalls)
    }

    @Test
    fun `missing refresh token prevents api request`() = runBlocking {
        for (session in listOf(null, AuthSession("access", " "))) {
            val api = FakeAuthApiService()
            val storage = FakeTokenStorage(session)

            val result = AuthRepositoryImpl(api, storage).refreshSession()

            assertEquals(AppResult.Failure(AppError.Unauthorized), result)
            assertEquals(null, api.request)
            assertEquals(0, storage.saveCalls)
        }
    }

    @Test
    fun `temporary network failures preserve stored session`() = runBlocking {
        val failures = listOf(
            IOException() to AppError.NoConnection,
            SocketTimeoutException() to AppError.Timeout,
            HttpException(Response.error<Any>(503, "".toResponseBody())) to AppError.Server,
        )
        for ((exception, expectedError) in failures) {
            val storage = FakeTokenStorage(originalSession)
            val api = FakeAuthApiService(exception = exception)

            val result = AuthRepositoryImpl(api, storage).refreshSession()

            assertEquals(AppResult.Failure(expectedError), result)
            assertEquals(originalSession, storage.session)
            assertEquals(0, storage.saveCalls)
        }
    }

    @Test
    fun `rejected refresh clears credentials and ends the session`() = runBlocking {
        val storage = FakeTokenStorage(originalSession)
        val api = FakeAuthApiService(
            exception = HttpException(Response.error<Any>(401, "".toResponseBody())),
        )

        val repository = AuthRepositoryImpl(api, storage)
        val result = repository.refreshSession()

        assertEquals(AppResult.Failure(AppError.Unauthorized), result)
        assertEquals(0, storage.saveCalls)
        assertEquals(null, storage.session)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
    }

    @Test
    fun `incomplete response does not overwrite stored credentials`() = runBlocking {
        val responses = listOf(
            RefreshTokenResponseDto(),
            RefreshTokenResponseDto(accessToken = "new-access"),
            RefreshTokenResponseDto(refreshToken = "new-refresh"),
            RefreshTokenResponseDto(" ", "new-refresh"),
            RefreshTokenResponseDto("new-access", ""),
        )
        for (response in responses) {
            val storage = FakeTokenStorage(originalSession)
            val api = FakeAuthApiService(response)

            val result = AuthRepositoryImpl(api, storage).refreshSession()

            assertEquals(AppResult.Failure(AppError.InvalidData), result)
            assertEquals(originalSession, storage.session)
            assertEquals(0, storage.saveCalls)
        }
    }

    @Test
    fun `storage read failure prevents refresh without deleting session`() = runBlocking {
        val storage = FakeTokenStorage(originalSession, readException = IOException())
        val api = FakeAuthApiService()

        val result = AuthRepositoryImpl(api, storage).refreshSession()

        assertEquals(AppResult.Failure(AppError.Storage), result)
        assertEquals(null, api.request)
        assertEquals(originalSession, storage.session)
    }

    @Test
    fun `persistence failure does not return success or replace cache`() = runBlocking {
        val storage = FakeTokenStorage(originalSession, saveException = IOException())

        val result = AuthRepositoryImpl(FakeAuthApiService(), storage).refreshSession()

        assertEquals(AppResult.Failure(AppError.Storage), result)
        assertEquals(originalSession, storage.session)
        assertEquals(originalSession.accessToken, storage.currentAccessToken())
    }

    @Test
    fun `cancellation during read request or save is propagated`() = runBlocking {
        val cancellation = CancellationException("Cancelled")
        val cases = listOf(
            FakeAuthApiService() to FakeTokenStorage(originalSession, readException = cancellation),
            FakeAuthApiService(exception = cancellation) to FakeTokenStorage(originalSession),
            FakeAuthApiService() to FakeTokenStorage(originalSession, saveException = cancellation),
        )
        for ((api, storage) in cases) {
            try {
                AuthRepositoryImpl(api, storage).refreshSession()
                fail("Cancellation must propagate")
            } catch (exception: CancellationException) {
                assertEquals(cancellation.message, exception.message)
            }
            assertEquals(originalSession, storage.session)
        }
    }

    @Test
    fun `restore without stored credentials resolves to unauthenticated without network`() = runBlocking {
        val api = FakeAuthApiService()
        val repository = AuthRepositoryImpl(api, FakeTokenStorage(null))

        assertEquals(AppResult.Success(Unit), repository.restoreSession())
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(0, api.validationCalls)
        assertEquals(0, api.refreshCalls)
    }

    @Test
    fun `restore validates usable access token without refreshing`() = runBlocking {
        val api = FakeAuthApiService()
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        assertEquals(AppResult.Success(Unit), repository.restoreSession())
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
        assertEquals(1, api.validationCalls)
        assertEquals(0, api.refreshCalls)
        assertEquals(originalSession, storage.session)
    }

    @Test
    fun `restore refreshes rejected access token and publishes authenticated`() = runBlocking {
        val api = FakeAuthApiService(
            validationException = HttpException(Response.error<Any>(401, "".toResponseBody())),
        )
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        assertEquals(AppResult.Success(Unit), repository.restoreSession())
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
        assertEquals(renewedSession, storage.session)
        assertEquals(1, api.refreshCalls)
    }

    @Test
    fun `restore does not clear tokens or log out on network failure`() = runBlocking {
        val api = FakeAuthApiService(validationException = IOException())
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        assertEquals(AppResult.Failure(AppError.NoConnection), repository.restoreSession())
        assertEquals(SessionState.Unknown, repository.sessionState.value)
        assertEquals(originalSession, storage.session)
        assertEquals(0, api.refreshCalls)
    }

    @Test
    fun `restore ends session when access and refresh are both rejected`() = runBlocking {
        val unauthorized = HttpException(Response.error<Any>(401, "".toResponseBody()))
        val api = FakeAuthApiService(exception = unauthorized, validationException = unauthorized)
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        assertEquals(AppResult.Failure(AppError.Unauthorized), repository.restoreSession())
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(null, storage.session)
    }

    @Test
    fun `parallel refresh calls reuse the renewed session`() = runBlocking {
        val responseReady = CompletableDeferred<Unit>()
        val api = FakeAuthApiService(onRefresh = { responseReady.await() })
        val repository = AuthRepositoryImpl(api, FakeTokenStorage(originalSession))

        val first = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshSession() }
        val second = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshSession() }
        responseReady.complete(Unit)

        assertEquals(AppResult.Success(renewedSession), first.await())
        assertEquals(AppResult.Success(renewedSession), second.await())
        assertEquals(1, api.refreshCalls)
    }

    @Test
    fun `logout during refresh leaves no credentials after both operations finish`() = runBlocking {
        val responseReady = CompletableDeferred<Unit>()
        val api = FakeAuthApiService(onRefresh = { responseReady.await() })
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        val refresh = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshSession() }
        val logout = async(start = CoroutineStart.UNDISPATCHED) { repository.logout() }
        responseReady.complete(Unit)

        refresh.await()
        assertEquals(AppResult.Success(Unit), logout.await())
        assertEquals(null, storage.session)
        assertEquals(null, storage.currentAccessToken())
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
    }

    @Test
    fun `login during refresh retains the newly logged in account`() = runBlocking {
        val responseReady = CompletableDeferred<Unit>()
        val api = FakeAuthApiService(onRefresh = { responseReady.await() })
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        val refresh = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshSession() }
        val login = async(start = CoroutineStart.UNDISPATCHED) {
            repository.login(LoginCredentials("another-user", "password"))
        }
        responseReady.complete(Unit)

        refresh.await()
        val loginSession = AuthSession("login-access", "login-refresh")
        assertEquals(AppResult.Success(loginSession), login.await())
        assertEquals(loginSession, storage.session)
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
    }

    @Test
    fun `refresh queued after logout cannot recreate the session`() = runBlocking {
        val responseReady = CompletableDeferred<Unit>()
        val api = FakeAuthApiService(onRefresh = { responseReady.await() })
        val storage = FakeTokenStorage(originalSession)
        val repository = AuthRepositoryImpl(api, storage)

        val refresh = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshSession() }
        val logout = async(start = CoroutineStart.UNDISPATCHED) { repository.logout() }
        val waitingRefresh = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshSession() }
        responseReady.complete(Unit)

        refresh.await()
        logout.await()
        assertEquals(AppResult.Failure(AppError.Unauthorized), waitingRefresh.await())
        assertEquals(null, storage.session)
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
        assertEquals(1, api.refreshCalls)
    }

    private class FakeAuthApiService(
        private val response: RefreshTokenResponseDto = RefreshTokenResponseDto("new-access", "new-refresh"),
        private val exception: Exception? = null,
        private val validationException: Exception? = null,
        private val onRefresh: suspend () -> Unit = {},
    ) : AuthApiService {
        var request: RefreshTokenRequestDto? = null
        var refreshCalls = 0
        var validationCalls = 0

        override suspend fun login(request: LoginRequestDto): LoginResponseDto =
            LoginResponseDto("login-access", "login-refresh")

        override suspend fun validateSession() {
            validationCalls++
            validationException?.let { throw it }
        }

        override suspend fun refresh(request: RefreshTokenRequestDto): RefreshTokenResponseDto {
            refreshCalls++
            this.request = request
            onRefresh()
            exception?.let { throw it }
            return response
        }
    }

    private class FakeTokenStorage(
        var session: AuthSession?,
        private val readException: Exception? = null,
        private val saveException: Exception? = null,
    ) : TokenStorage {
        var saveCalls = 0

        override suspend fun getSession(): AuthSession? {
            readException?.let { throw it }
            return session
        }

        override suspend fun save(session: AuthSession) {
            saveCalls++
            saveException?.let { throw it }
            this.session = session
        }

        override fun currentAccessToken(): String? = session?.accessToken

        override suspend fun clear() {
            session = null
        }
    }
}
