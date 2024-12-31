package com.example.my.project.authenticator.appclass

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner


class CustomSpinner(context: Context, attrs: AttributeSet?) : AppCompatSpinner(context, attrs) {

    var isDropdownShown: Boolean = false



    override fun performClick(): Boolean {
        isDropdownShown = true
        return super.performClick()
    }

    override fun onDetachedFromWindow() {
        isDropdownShown = false
        super.onDetachedFromWindow()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            isDropdownShown = false
        }
    }
}
