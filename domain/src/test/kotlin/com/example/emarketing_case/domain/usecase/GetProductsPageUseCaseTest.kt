package com.example.emarketing_case.domain.usecase

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.ProductPage
import com.example.emarketing_case.domain.repository.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GetProductsPageUseCaseTest {
    @Test
    fun `delegates valid page request to repository`() = runBlocking {
        val repository = FakeProductRepository()
        val useCase = GetProductsPageUseCase(repository)

        val result = useCase(limit = 20, skip = 40)

        assertEquals(AppResult.Success(EMPTY_PAGE), result)
        assertEquals(20 to 40, repository.lastRequest)
    }

    @Test
    fun `rejects invalid page request`() {
        val useCase = GetProductsPageUseCase(FakeProductRepository())

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(limit = 0, skip = 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(limit = 20, skip = -1) }
        }
    }

    private class FakeProductRepository : ProductRepository {
        var lastRequest: Pair<Int, Int>? = null

        override suspend fun getProducts(limit: Int, skip: Int): AppResult<ProductPage> {
            lastRequest = limit to skip
            return AppResult.Success(EMPTY_PAGE)
        }
    }

    private companion object {
        val EMPTY_PAGE = ProductPage(
            products = emptyList(),
            total = 0,
            skip = 0,
            limit = 20,
        )
    }
}
