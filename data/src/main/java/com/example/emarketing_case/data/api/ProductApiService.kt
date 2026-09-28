package com.example.emarketing_case.data.api

import com.example.emarketing_case.data.dto.ProductsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {
    @GET("auth/products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): ProductsResponseDto
}
