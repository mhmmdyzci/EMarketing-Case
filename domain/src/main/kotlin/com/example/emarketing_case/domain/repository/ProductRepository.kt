package com.example.emarketing_case.domain.repository

import com.example.emarketing_case.domain.model.AppResult
import com.example.emarketing_case.domain.model.ProductPage

interface ProductRepository {
    suspend fun getProducts(
        limit: Int,
        skip: Int,
    ): AppResult<ProductPage>
}
