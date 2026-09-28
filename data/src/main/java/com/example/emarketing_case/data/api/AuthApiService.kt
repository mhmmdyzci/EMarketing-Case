package com.example.emarketing_case.data.api

import com.example.emarketing_case.data.dto.LoginRequestDto
import com.example.emarketing_case.data.dto.LoginResponseDto
import com.example.emarketing_case.data.dto.RefreshTokenRequestDto
import com.example.emarketing_case.data.dto.RefreshTokenResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): RefreshTokenResponseDto

    @GET("auth/me")
    suspend fun validateSession()
}
