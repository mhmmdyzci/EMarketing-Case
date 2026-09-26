package com.example.emarketing_case.data.di

import com.example.emarketing_case.data.local.token.SecureTokenStorage
import com.example.emarketing_case.data.repository.AuthRepositoryImpl
import com.example.emarketing_case.domain.repository.AuthRepository
import com.example.emarketing_case.domain.repository.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    internal abstract fun bindAuthRepository(
        authRepository: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    internal abstract fun bindTokenStorage(
        tokenStorage: SecureTokenStorage,
    ): TokenStorage
}
