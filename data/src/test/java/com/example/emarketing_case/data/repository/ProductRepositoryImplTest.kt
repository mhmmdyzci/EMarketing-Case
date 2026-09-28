package com.example.emarketing_case.data.repository

import com.example.emarketing_case.data.api.ProductApiService
import com.example.emarketing_case.data.dto.ProductDto
import com.example.emarketing_case.data.dto.ProductsResponseDto
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.domain.model.ProductPage
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductRepositoryImplTest {
    @Test
    fun `returns mapped product page`() = runBlocking {
        val api = FakeProductApiService(response = VALID_RESPONSE)
        val repository = ProductRepositoryImpl(api)

        val result = repository.getProducts(limit = 20, skip = 40)

        assertEquals(20 to 40, api.lastRequest)
        assertEquals(AppResult.Success(EXPECTED_PAGE), result)
    }

    @Test
    fun `returns invalid data when required product field is missing`() = runBlocking {
        val invalidResponse = VALID_RESPONSE.copy(
            products = listOf(VALID_PRODUCT.copy(title = null)),
        )
        val repository = ProductRepositoryImpl(FakeProductApiService(response = invalidResponse))

        val result = repository.getProducts(limit = 20, skip = 0)

        assertEquals(AppResult.Failure(AppError.InvalidData), result)
    }

    @Test
    fun `returns no connection when request fails with io exception`() = runBlocking {
        val repository = ProductRepositoryImpl(
            FakeProductApiService(exception = IOException("No network")),
        )

        val result = repository.getProducts(limit = 20, skip = 0)

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
    }

    private class FakeProductApiService(
        private val response: ProductsResponseDto? = null,
        private val exception: Exception? = null,
    ) : ProductApiService {
        var lastRequest: Pair<Int, Int>? = null

        override suspend fun getProducts(limit: Int, skip: Int): ProductsResponseDto {
            lastRequest = limit to skip
            exception?.let { throw it }
            return requireNotNull(response)
        }
    }

    private companion object {
        val VALID_PRODUCT = ProductDto(
            id = 1,
            title = "Essence Mascara Lash Princess",
            description = "A volumizing mascara.",
            category = "beauty",
            price = 9.99,
            discountPercentage = 7.17,
            rating = 4.94,
            stock = 5,
            thumbnail = "https://example.com/product.png",
        )
        val VALID_RESPONSE = ProductsResponseDto(
            products = listOf(VALID_PRODUCT),
            total = 194,
            skip = 40,
            limit = 20,
        )
        val EXPECTED_PAGE = ProductPage(
            products = listOf(
                Product(
                    id = 1,
                    title = "Essence Mascara Lash Princess",
                    description = "A volumizing mascara.",
                    category = "beauty",
                    price = 9.99,
                    discountPercentage = 7.17,
                    rating = 4.94,
                    stock = 5,
                    thumbnailUrl = "https://example.com/product.png",
                ),
            ),
            total = 194,
            skip = 40,
            limit = 20,
        )
    }
}
