package com.example.my.project.authenticator.di

import android.app.Application
import android.util.Log
import com.example.my.project.authenticator.admob.AppOpenManager
import com.example.my.project.authenticator.ui.activities.iap.BillingViewModel
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Authenticator : Application(){

    @Inject
    lateinit var billingViewModel: BillingViewModel

    override fun onCreate() {
        super.onCreate()

        AppOpenManager(this)
        PrefsHelper.init(this)

        billingViewModel.startBillingConnection()

        CoroutineScope(Dispatchers.IO).launch {
            billingViewModel.purchases.collect { purchaseList ->
                Log.d("IAP", "purchase list: $purchaseList")
                isAdsRemoved = purchaseList?.isNotEmpty() == true
            }
        }
    }
}