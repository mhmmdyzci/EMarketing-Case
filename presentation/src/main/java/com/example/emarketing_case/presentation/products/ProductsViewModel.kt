package com.example.emarketing_case.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.domain.usecase.GetProductsPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class ProductsViewModel @Inject constructor(
    getProductsPage: GetProductsPageUseCase,
) : ViewModel() {
    val products: Flow<PagingData<Product>> = Pager(
        config = PagingConfig(
            pageSize = ProductsPagingSource.DEFAULT_PAGE_SIZE,
            initialLoadSize = ProductsPagingSource.DEFAULT_PAGE_SIZE,
            enablePlaceholders = false,
            maxSize = MAX_CACHED_ITEMS,
        ),
        pagingSourceFactory = {
            ProductsPagingSource(
                getProductsPage = getProductsPage,
                pageSize = ProductsPagingSource.DEFAULT_PAGE_SIZE,
            )
        },
    ).flow.cachedIn(viewModelScope)

    private companion object {
        const val MAX_CACHED_ITEMS = ProductsPagingSource.DEFAULT_PAGE_SIZE * 3
    }
}
