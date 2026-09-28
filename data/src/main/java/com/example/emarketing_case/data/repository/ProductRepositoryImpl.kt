package com.example.emarketing_case.data.repository

import com.example.emarketing_case.data.api.ProductApiService
import com.example.emarketing_case.data.api.safeApiCall
import com.example.emarketing_case.data.mapper.toDomain
import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.ProductPage
import com.example.emarketing_case.domain.repository.ProductRepository
import javax.inject.Inject

internal class ProductRepositoryImpl @Inject constructor(
    private val productApiService: ProductApiService,
) : ProductRepository {
    override suspend fun getProducts(
        limit: Int,
        skip: Int,
    ): AppResult<ProductPage> = safeApiCall {
        productApiService.getProducts(limit = limit, skip = skip).toDomain()
    }
}
