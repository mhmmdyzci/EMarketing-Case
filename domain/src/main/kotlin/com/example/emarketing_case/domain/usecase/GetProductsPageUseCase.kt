package com.example.emarketing_case.domain.usecase

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.ProductPage
import com.example.emarketing_case.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsPageUseCase @Inject constructor(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(
        limit: Int,
        skip: Int,
    ): AppResult<ProductPage> {
        require(limit > 0) { "Limit must be greater than zero" }
        require(skip >= 0) { "Skip cannot be negative" }
        return productRepository.getProducts(limit = limit, skip = skip)
    }
}
