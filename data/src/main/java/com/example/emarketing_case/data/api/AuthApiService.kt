package com.example.emarketing_case.data.api

import com.example.emarketing_case.data.dto.LoginRequestDto
import com.example.emarketing_case.data.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}
