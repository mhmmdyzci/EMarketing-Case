package com.example.emarketing_case.data.mapper

import com.example.emarketing_case.data.dto.ProductDto
import com.example.emarketing_case.data.dto.ProductsResponseDto
import com.example.emarketing_case.domain.model.Product
import com.example.emarketing_case.domain.model.ProductPage
import com.google.gson.JsonSyntaxException

internal fun ProductsResponseDto.toDomain(): ProductPage {
    val mappedProducts = products
        ?.map(ProductDto::toDomain)
        ?: invalidField("products")
    val mappedTotal = total?.takeIf { it >= 0 } ?: invalidField("total")
    val mappedSkip = skip?.takeIf { it >= 0 } ?: invalidField("skip")
    val mappedLimit = limit?.takeIf { it > 0 } ?: invalidField("limit")

    return ProductPage(
        products = mappedProducts,
        total = mappedTotal,
        skip = mappedSkip,
        limit = mappedLimit,
    )
}

private fun ProductDto.toDomain(): Product =
    Product(
        id = id?.takeIf { it > 0 } ?: invalidField("product.id"),
        title = title.nonBlankOrInvalid("product.title"),
        description = description ?: invalidField("product.description"),
        category = category.nonBlankOrInvalid("product.category"),
        price = price?.takeIf { it.isFinite() && it >= 0 } ?: invalidField("product.price"),
        discountPercentage = discountPercentage
            ?.takeIf { it.isFinite() && it in 0.0..100.0 }
            ?: invalidField("product.discountPercentage"),
        rating = rating
            ?.takeIf { it.isFinite() && it in 0.0..5.0 }
            ?: invalidField("product.rating"),
        stock = stock?.takeIf { it >= 0 } ?: invalidField("product.stock"),
        thumbnailUrl = thumbnail.nonBlankOrInvalid("product.thumbnail"),
    )

private fun String?.nonBlankOrInvalid(fieldName: String): String =
    this?.takeIf(String::isNotBlank) ?: invalidField(fieldName)

private fun invalidField(fieldName: String): Nothing =
    throw JsonSyntaxException("Missing or invalid field: $fieldName")
