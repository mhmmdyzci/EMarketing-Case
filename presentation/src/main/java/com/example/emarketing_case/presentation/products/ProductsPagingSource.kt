package com.example.emarketing_case.presentation.products

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.domain.model.ProductPage
import com.example.emarketing_case.domain.usecase.GetProductsPageUseCase

class ProductsPagingSource(
    private val getProductsPage: GetProductsPageUseCase,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) : PagingSource<Int, Product>() {
    init {
        require(pageSize > 0) { "Page size must be greater than zero" }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        val requestedSkip = params.key ?: INITIAL_SKIP

        return when (
            val result = getProductsPage(
                limit = params.loadSize,
                skip = requestedSkip,
            )
        ) {
            is AppResult.Success -> result.data.toLoadResult(requestedSkip)
            is AppResult.Failure -> LoadResult.Error(ProductPagingException(result.error))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null

        return anchorPage.prevKey?.plus(pageSize)
            ?: anchorPage.nextKey
                ?.minus(anchorPage.data.size)
                ?.coerceAtLeast(INITIAL_SKIP)
    }

    private fun ProductPage.toLoadResult(
        requestedSkip: Int,
    ): LoadResult<Int, Product> {
        if (skip != requestedSkip) {
            return LoadResult.Error(ProductPagingException(AppError.InvalidData))
        }

        val nextSkip = skip + products.size
        return LoadResult.Page(
            data = products,
            prevKey = skip
                .takeIf { it > INITIAL_SKIP }
                ?.let { (it - pageSize).coerceAtLeast(INITIAL_SKIP) },
            nextKey = nextSkip.takeIf {
                products.isNotEmpty() && it < total
            },
        )
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        private const val INITIAL_SKIP = 0
    }
}
