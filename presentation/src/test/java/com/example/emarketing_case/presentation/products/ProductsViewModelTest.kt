package com.example.emarketing_case.presentation.products

import androidx.paging.testing.asSnapshot
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.domain.model.ProductPage
import com.example.emarketing_case.domain.repository.ProductRepository
import com.example.emarketing_case.domain.usecase.GetProductsPageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads pages as user scrolls and stops at total`() = runTest {
        val repository = FakeProductRepository(total = 45)
        val viewModel = ProductsViewModel(GetProductsPageUseCase(repository))

        val snapshot = viewModel.products.asSnapshot {
            scrollTo(index = 44)
        }

        assertEquals((1L..45L).toList(), snapshot.map(Product::id))
        assertEquals(
            listOf(
                20 to 0,
                20 to 20,
                20 to 40,
            ),
            repository.requests,
        )
    }

    private class FakeProductRepository(
        private val total: Int,
    ) : ProductRepository {
        val requests = mutableListOf<Pair<Int, Int>>()

        override suspend fun getProducts(limit: Int, skip: Int): AppResult<ProductPage> {
            requests += limit to skip
            val itemCount = (total - skip).coerceAtLeast(0).coerceAtMost(limit)
            return AppResult.Success(
                ProductPage(
                    products = List(itemCount) { index -> product(id = skip + index + 1L) },
                    total = total,
                    skip = skip,
                    limit = limit,
                ),
            )
        }
    }

    private companion object {
        fun product(id: Long) = Product(
            id = id,
            title = "Product $id",
            description = "Description $id",
            category = "category",
            price = 10.0,
            discountPercentage = 0.0,
            rating = 4.0,
            stock = 10,
            thumbnailUrl = "https://example.com/$id.png",
        )
    }
}
