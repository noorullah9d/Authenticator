package com.example.my.project.authenticator.di

import com.example.my.project.authenticator.analytics.AnalyticsHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AnalyticsEntryPoint {
    fun analyticsHelper(): AnalyticsHelper
}