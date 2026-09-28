package com.example.emarketing_case.presentation.products

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.domain.model.ProductPage
import com.example.emarketing_case.domain.repository.ProductRepository
import com.example.emarketing_case.domain.usecase.GetProductsPageUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductsPagingSourceTest {
    @Test
    fun `loads first page using load size as limit`() = runTest {
        val repository = FakeProductRepository(
            result = AppResult.Success(productPage(skip = 0, productCount = 20, total = 45)),
        )
        val pagingSource = ProductsPagingSource(GetProductsPageUseCase(repository))

        val result = pagingSource.load(refreshParams(key = null, loadSize = 20))

        assertEquals(20 to 0, repository.lastRequest)
        assertEquals(
            PagingSource.LoadResult.Page(
                data = products(count = 20),
                prevKey = null,
                nextKey = 20,
            ),
            result,
        )
    }

    @Test
    fun `stops pagination when last item is loaded`() = runTest {
        val repository = FakeProductRepository(
            result = AppResult.Success(productPage(skip = 20, productCount = 5, total = 25)),
        )
        val pagingSource = ProductsPagingSource(GetProductsPageUseCase(repository))

        val result = pagingSource.load(appendParams(key = 20, loadSize = 20))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(0, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `refresh and prepend requests contiguous ranges`() = runTest {
        val repository = OffsetProductRepository(total = 200)
        val pagingSource = ProductsPagingSource(GetProductsPageUseCase(repository))

        val refresh = pagingSource.load(refreshParams(key = 60, loadSize = 60))
            as PagingSource.LoadResult.Page
        val prepend = pagingSource.load(
            prependParams(key = requireNotNull(refresh.prevKey), loadSize = 20),
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf(60 to 60, 20 to 40), repository.requests)
        assertEquals(40, refresh.prevKey)
        assertEquals(60L, refresh.data.first().id)
        assertEquals(59L, prepend.data.last().id)
    }

    @Test
    fun `exposes domain failure as paging error`() = runTest {
        val repository = FakeProductRepository(
            result = AppResult.Failure(AppError.NoConnection),
        )
        val pagingSource = ProductsPagingSource(GetProductsPageUseCase(repository))

        val result = pagingSource.load(refreshParams(key = null, loadSize = 20))

        assertTrue(result is PagingSource.LoadResult.Error)
        val exception = (result as PagingSource.LoadResult.Error).throwable
        assertEquals(AppError.NoConnection, (exception as ProductPagingException).error)
    }

    @Test
    fun `rejects response with unexpected skip`() = runTest {
        val repository = FakeProductRepository(
            result = AppResult.Success(productPage(skip = 20, productCount = 20, total = 45)),
        )
        val pagingSource = ProductsPagingSource(GetProductsPageUseCase(repository))

        val result = pagingSource.load(refreshParams(key = 0, loadSize = 20))

        val exception = (result as PagingSource.LoadResult.Error).throwable
        assertEquals(AppError.InvalidData, (exception as ProductPagingException).error)
    }

    @Test
    fun `refresh key returns start of page containing anchor`() {
        val pagingSource = ProductsPagingSource(
            GetProductsPageUseCase(FakeProductRepository(AppResult.Success(productPage()))),
        )
        val state = PagingState<Int, Product>(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = products(count = 20, startId = 20),
                    prevKey = 0,
                    nextKey = 40,
                ),
            ),
            anchorPosition = 10,
            config = PagingConfig(pageSize = 20, initialLoadSize = 20),
            leadingPlaceholderCount = 0,
        )

        assertEquals(20, pagingSource.getRefreshKey(state))
    }

    private class FakeProductRepository(
        private val result: AppResult<ProductPage>,
    ) : ProductRepository {
        var lastRequest: Pair<Int, Int>? = null

        override suspend fun getProducts(limit: Int, skip: Int): AppResult<ProductPage> {
            lastRequest = limit to skip
            return result
        }
    }

    private class OffsetProductRepository(
        private val total: Int,
    ) : ProductRepository {
        val requests = mutableListOf<Pair<Int, Int>>()

        override suspend fun getProducts(limit: Int, skip: Int): AppResult<ProductPage> {
            requests += limit to skip
            val itemCount = minOf(limit, total - skip)
            return AppResult.Success(
                ProductPage(
                    products = products(count = itemCount, startId = skip),
                    total = total,
                    skip = skip,
                    limit = limit,
                ),
            )
        }
    }

    private companion object {
        fun refreshParams(key: Int?, loadSize: Int) = PagingSource.LoadParams.Refresh(
            key = key,
            loadSize = loadSize,
            placeholdersEnabled = false,
        )

        fun appendParams(key: Int, loadSize: Int) = PagingSource.LoadParams.Append(
            key = key,
            loadSize = loadSize,
            placeholdersEnabled = false,
        )

        fun prependParams(key: Int, loadSize: Int) = PagingSource.LoadParams.Prepend(
            key = key,
            loadSize = loadSize,
            placeholdersEnabled = false,
        )

        fun productPage(
            skip: Int = 0,
            productCount: Int = 20,
            total: Int = 45,
        ) = ProductPage(
            products = products(productCount),
            total = total,
            skip = skip,
            limit = 20,
        )

        fun products(
            count: Int,
            startId: Int = 1,
        ): List<Product> = List(count) { index ->
            val id = startId + index
            Product(
                id = id.toLong(),
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
}
