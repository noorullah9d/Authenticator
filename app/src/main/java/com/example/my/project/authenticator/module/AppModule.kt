package com.example.my.project.authenticator.module

import android.content.Context
import com.example.my.project.authenticator.utils.AppPreference
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
}