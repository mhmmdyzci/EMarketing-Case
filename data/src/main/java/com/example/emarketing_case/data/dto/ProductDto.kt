package com.example.emarketing_case.data.dto

data class ProductDto(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val discountPercentage: Double? = null,
    val rating: Double? = null,
    val stock: Int? = null,
    val thumbnail: String? = null,
)
