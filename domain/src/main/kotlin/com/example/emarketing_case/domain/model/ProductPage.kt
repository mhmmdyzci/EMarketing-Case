package com.example.emarketing_case.domain.model

data class ProductPage(
    val products: List<Product>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)
