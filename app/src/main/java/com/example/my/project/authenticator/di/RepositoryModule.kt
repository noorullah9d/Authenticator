package com.example.my.project.authenticator.di

import com.example.my.project.authenticator.otp.data.repository.TotpKeyRepositoryImpl
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.my.project.authenticator.otp.data.database.TotpDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideTotpKeyRepository(dao: TotpDao): TotpKeyRepository {
        return TotpKeyRepositoryImpl(dao)
    }
}