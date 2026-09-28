package com.example.emarketing_case.data.dto

data class ProductsResponseDto(
    val products: List<ProductDto>? = null,
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null,
)
