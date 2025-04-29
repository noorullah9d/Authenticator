package com.example.my.project.authenticator.di

import android.content.Context
import com.example.my.project.authenticator.otp.data.repository.RemoteConfigRepositoryImpl
import com.example.my.project.authenticator.otp.domain.repository.RemoteConfigRepository
import com.example.my.project.authenticator.ui.activities.iap.BillingViewModel
import com.example.my.project.authenticator.utils.AppPreference
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideAppPreference(@ApplicationContext context: Context): AppPreference {
        return AppPreference(context)
    }

    @Provides
    @Singleton
    fun provideBillingViewModel(
        @ApplicationContext context: Context
    ): BillingViewModel {
        return BillingViewModel(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

    @Singleton
    @Provides
    fun provideRemoteConfigRepository(
        @ApplicationContext context: Context,
        firebaseRemoteConfig: FirebaseRemoteConfig
    ): RemoteConfigRepository {
        return RemoteConfigRepositoryImpl(context, firebaseRemoteConfig)
    }
}