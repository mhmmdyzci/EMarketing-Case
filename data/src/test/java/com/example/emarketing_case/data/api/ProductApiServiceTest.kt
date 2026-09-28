package com.example.emarketing_case.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductApiServiceTest {
    @Test
    fun `requests protected products with limit and skip`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(PRODUCTS_RESPONSE),
        )
        server.start()

        try {
            val api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProductApiService::class.java)

            val response = api.getProducts(limit = 20, skip = 40)
            val request = server.takeRequest()

            assertEquals("GET", request.method)
            assertEquals("/auth/products?limit=20&skip=40", request.path)
            assertEquals(1, response.products?.size)
            assertEquals(194, response.total)
            assertEquals(40, response.skip)
            assertEquals(20, response.limit)
        } finally {
            server.shutdown()
        }
    }

    private companion object {
        val PRODUCTS_RESPONSE =
            """
            {
              "products": [
                {
                  "id": 1,
                  "title": "Essence Mascara Lash Princess",
                  "description": "A volumizing mascara.",
                  "category": "beauty",
                  "price": 9.99,
                  "discountPercentage": 7.17,
                  "rating": 4.94,
                  "stock": 5,
                  "thumbnail": "https://example.com/product.png"
                }
              ],
              "total": 194,
              "skip": 40,
              "limit": 20
            }
            """.trimIndent()
    }
}
