package com.example.my.project.authenticator.di

import android.app.Application
import com.example.my.project.authenticator.admob.AppOpenManager
import com.example.my.project.authenticator.utils.PrefsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Authenticator : Application(){
    override fun onCreate() {
        super.onCreate()

        AppOpenManager(this)
        PrefsHelper.init(this)
    }
}