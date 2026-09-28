package com.example.emarketing_case.data.api

import com.example.emarketing_case.data.di.NetworkModule
import com.example.emarketing_case.data.repository.AuthRepositoryImpl
import com.example.emarketing_case.domain.model.AuthSession
import com.example.emarketing_case.domain.model.SessionState
import com.example.emarketing_case.domain.repository.TokenStorage
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SessionAuthenticatorTest {
    private val server = MockWebServer()
    private val storage = MemoryTokenStorage()
    private lateinit var authClient: OkHttpClient
    private lateinit var client: OkHttpClient
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        authClient = NetworkModule.provideAuthOkHttpClient(storage)
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
        repository = AuthRepositoryImpl(api, storage)
        client = NetworkModule.provideOkHttpClient(storage, repository)
            .newBuilder()
            .dispatcher(Dispatcher().apply {
                maxRequests = 4
                maxRequestsPerHost = 4
            })
            .build()
    }

    @After
    fun tearDown() {
        for (httpClient in listOf(client, authClient)) {
            httpClient.dispatcher.cancelAll()
            httpClient.dispatcher.executorService.shutdownNow()
            httpClient.connectionPool.evictAll()
        }
        server.shutdown()
    }

    @Test
    fun `saturated request dispatcher refreshes once and retries all calls with new token`() {
        val oldRequests = CountDownLatch(4)
        val refreshCalls = AtomicInteger()
        val retries = ConcurrentLinkedQueue<String>()
        dispatch { request ->
            when {
                request.path == "/auth/refresh" -> {
                    refreshCalls.incrementAndGet()
                    refreshedResponse()
                }
                request.getHeader("Authorization") == "Bearer old-access" -> {
                    oldRequests.countDown()
                    await(oldRequests)
                    response(401)
                }
                else -> {
                    retries.add(request.getHeader("Authorization").orEmpty())
                    response(200)
                }
            }
        }

        val requests = List(4) { enqueue("/auth/products") }

        requests.forEach { assertEquals(200, it.get(10, TimeUnit.SECONDS)) }
        assertEquals(1, refreshCalls.get())
        assertEquals(List(4) { "Bearer new-access" }, retries.toList())
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
    }

    @Test
    fun `new request waits until active refresh finishes before sending headers`() {
        val refreshStarted = CountDownLatch(1)
        val releaseRefresh = CountDownLatch(1)
        val queuedEntered = CountDownLatch(1)
        val queuedReachedServer = CountDownLatch(1)
        val queuedHeaders = ConcurrentLinkedQueue<String>()
        client = client.newBuilder().apply {
            interceptors().add(0) { chain ->
                if (chain.request().url.encodedPath == "/queued") queuedEntered.countDown()
                chain.proceed(chain.request())
            }
        }.build()
        dispatch { request ->
            when {
                request.path == "/auth/refresh" -> {
                    refreshStarted.countDown()
                    await(releaseRefresh)
                    refreshedResponse()
                }
                request.path == "/queued" -> {
                    queuedHeaders.add(request.getHeader("Authorization").orEmpty())
                    queuedReachedServer.countDown()
                    response(200)
                }
                request.getHeader("Authorization") == "Bearer old-access" -> response(401)
                else -> response(200)
            }
        }

        try {
            val first = enqueue("/auth/products")
            await(refreshStarted)
            val queued = enqueue("/queued")
            await(queuedEntered)
            assertFalse(queuedReachedServer.await(150, TimeUnit.MILLISECONDS))
            releaseRefresh.countDown()

            assertEquals(200, first.get(10, TimeUnit.SECONDS))
            assertEquals(200, queued.get(10, TimeUnit.SECONDS))
            assertEquals(listOf("Bearer new-access"), queuedHeaders.toList())
        } finally {
            releaseRefresh.countDown()
        }
    }

    @Test
    fun `late unauthorized response reuses refreshed token without another refresh`() {
        val slowRequestStarted = CountDownLatch(1)
        val releaseSlowResponse = CountDownLatch(1)
        val refreshCalls = AtomicInteger()
        dispatch { request ->
            when {
                request.path == "/auth/refresh" -> {
                    refreshCalls.incrementAndGet()
                    refreshedResponse()
                }
                request.getHeader("Authorization") == "Bearer new-access" -> response(200)
                request.path == "/slow" -> {
                    slowRequestStarted.countDown()
                    await(releaseSlowResponse)
                    response(401)
                }
                else -> response(401)
            }
        }

        try {
            val slow = enqueue("/slow")
            await(slowRequestStarted)
            assertEquals(200, enqueue("/fast").get(10, TimeUnit.SECONDS))
            releaseSlowResponse.countDown()

            assertEquals(200, slow.get(10, TimeUnit.SECONDS))
            assertEquals(1, refreshCalls.get())
        } finally {
            releaseSlowResponse.countDown()
        }
    }

    @Test
    fun `parallel failures share failed refresh without deleting stored session`() {
        val oldRequests = CountDownLatch(4)
        val refreshCalls = AtomicInteger()
        dispatch { request ->
            if (request.path == "/auth/refresh") {
                refreshCalls.incrementAndGet()
                response(503)
            } else {
                oldRequests.countDown()
                await(oldRequests)
                response(401)
            }
        }

        val requests = List(4) { enqueue("/auth/products") }

        requests.forEach { assertEquals(401, it.get(10, TimeUnit.SECONDS)) }
        assertEquals(1, refreshCalls.get())
        assertEquals("old-access", storage.currentAccessToken())
    }

    @Test
    fun `rejected refresh does not recurse and clears the session`() {
        val refreshCalls = AtomicInteger()
        dispatch { request ->
            if (request.path == "/auth/refresh") refreshCalls.incrementAndGet()
            response(401)
        }

        assertEquals(401, enqueue("/auth/products").get(10, TimeUnit.SECONDS))
        assertEquals(1, refreshCalls.get())
        assertEquals(null, storage.currentAccessToken())
        assertEquals(SessionState.Unauthenticated, repository.sessionState.value)
    }

    @Test
    fun `retried request returning unauthorized does not refresh again`() {
        val refreshCalls = AtomicInteger()
        val productCalls = AtomicInteger()
        dispatch { request ->
            if (request.path == "/auth/refresh") {
                refreshCalls.incrementAndGet()
                refreshedResponse()
            } else {
                productCalls.incrementAndGet()
                response(401)
            }
        }

        assertEquals(401, enqueue("/auth/products").get(10, TimeUnit.SECONDS))
        assertEquals(1, refreshCalls.get())
        assertEquals(2, productCalls.get())
    }

    private fun enqueue(path: String): CompletableFuture<Int> {
        val result = CompletableFuture<Int>()
        client.newCall(Request.Builder().url(server.url(path)).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                result.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { result.complete(it.code) }
            }
        })
        return result
    }

    private fun dispatch(block: (RecordedRequest) -> MockResponse) {
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = block(request)
        }
    }

    private fun await(latch: CountDownLatch) {
        assertTrue("Concurrent request did not reach the expected step", latch.await(5, TimeUnit.SECONDS))
    }

    private fun response(code: Int): MockResponse = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody("{}")

    private fun refreshedResponse(): MockResponse = response(200)
        .setBody("""{"accessToken":"new-access","refreshToken":"new-refresh"}""")

    private class MemoryTokenStorage : TokenStorage {
        @Volatile
        private var session: AuthSession? = AuthSession("old-access", "old-refresh")

        override suspend fun getSession(): AuthSession? = session

        override suspend fun save(session: AuthSession) {
            this.session = session
        }

        override fun currentAccessToken(): String? = session?.accessToken

        override suspend fun clear() {
            session = null
        }
    }
}
