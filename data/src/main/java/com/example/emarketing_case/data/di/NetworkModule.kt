package com.example.emarketing_case.data.di

import com.example.emarketing_case.data.BuildConfig
import com.example.emarketing_case.data.api.AuthApiService
import com.example.emarketing_case.data.api.AuthorizationInterceptor
import com.example.emarketing_case.data.api.NetworkConfig
import com.example.emarketing_case.data.api.ProductApiService
import com.example.emarketing_case.data.api.SessionAuthenticator
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.repository.TokenStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @AuthClient
    fun provideAuthOkHttpClient(tokenStorage: TokenStorage): OkHttpClient =
        clientBuilder()
            .addInterceptor(AuthorizationInterceptor(tokenStorage))
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenStorage: TokenStorage,
        authRepository: AuthRepository,
    ): OkHttpClient {
        val authenticator = SessionAuthenticator(authRepository, tokenStorage)
        return clientBuilder()
            .addInterceptor(AuthorizationInterceptor(tokenStorage, authenticator))
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit = createRetrofit(okHttpClient, gson)

    @Provides
    @Singleton
    fun provideAuthApiService(
        @AuthClient okHttpClient: OkHttpClient,
        gson: Gson,
    ): AuthApiService = createRetrofit(okHttpClient, gson).create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideProductApiService(retrofit: Retrofit): ProductApiService =
        retrofit.create(ProductApiService::class.java)

    private fun createRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    private fun clientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
}
