package com.example.my.project.authenticator.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext context: Context
) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Logs a custom event with optional parameters.
     * @param eventName Name of the event
     * @param params Optional bundle of parameters
     */
    fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(eventName, params)
    }

    /**
     * Sets a user ID for tracking.
     * @param userId ID of the user
     */
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }

    /**
     * Sets a user property (e.g., user_type = premium).
     * @param name Name of the property
     * @param value Value of the property
     */
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Logs a screen view manually.
     * @param screenName Name of the screen
     * @param screenClass Class name of the screen (optional)
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}