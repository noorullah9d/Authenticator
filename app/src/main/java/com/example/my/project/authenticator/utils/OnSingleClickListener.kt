package com.example.my.project.authenticator.utils

import android.os.SystemClock
import android.view.View

abstract class OnSingleClickListener : View.OnClickListener {

    private var mLastClickTime: Long = 0
    abstract fun onSingleClick(v: View?)
    override fun onClick(v: View?) {
        val currentClickTime: Long = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        if (elapsedTime <= MIN_CLICK_INTERVAL) return
        mLastClickTime = currentClickTime
        onSingleClick(v)
    }
    companion object {
        private const val MIN_CLICK_INTERVAL: Long = 700
    }

}