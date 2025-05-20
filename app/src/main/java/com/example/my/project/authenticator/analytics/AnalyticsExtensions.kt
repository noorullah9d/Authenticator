package com.example.my.project.authenticator.analytics

import android.content.Context
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.example.my.project.authenticator.di.AnalyticsEntryPoint
import dagger.hilt.android.EntryPointAccessors

fun Context.postAnalytics(eventName: String, params: Bundle? = null) {
    getAnalyticsHelper().logEvent(eventName, params)
}

fun Context.setUserId(userId: String) {
    getAnalyticsHelper().setUserId(userId)
}

fun Context.setUserProperty(name: String, value: String) {
    getAnalyticsHelper().setUserProperty(name, value)
}

fun Context.logScreen(screenName: String, screenClass: String? = null) {
    getAnalyticsHelper().logScreenView(screenName, screenClass)
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun Context.getAnalyticsHelper(): AnalyticsHelper {
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        AnalyticsEntryPoint::class.java
    )
    return entryPoint.analyticsHelper()
}
