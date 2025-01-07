package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityPasswordScreenBinding
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.ra.fingerprint_auth.FingerprintCallback
import com.ra.fingerprint_auth.FingerprintManager

class PasswordScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordScreenBinding
    private var prefsHelper: SharedPreferencesHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        prefsHelper = SharedPreferencesHelper(this)
        thumbVisibility()
        clicks()


    }

    private fun clicks() {
        binding.apply {

            hideNewPassword.setOnClickListener {
                hidePassword(etNewPassword, hideNewPassword)
            }

            ivThumb.setOnClickListener { fingerprint() }






            mbContinue.setOnClickListener {
                if (etNewPassword.text!!.isEmpty()) {
                    toast("Please Enter Password")
                } else {

                    if (prefsHelper?.userPassword == etNewPassword.text.toString()) {
                        startActivityWithAnimation<MainActivity>()
                    } else {
                        passwordWrong.beVisible()
                    }
                }
            }
        }
    }

    private fun thumbVisibility() {
        binding.apply {
            if (prefsHelper?.isFingerprintEnabled!!) {
                ivThumb.beVisible()
                tvOpenThumb.beVisible()
            } else {
                ivThumb.beGone()
                tvOpenThumb.beGone()
            }
        }
    }

    private fun hidePassword(et: AppCompatEditText, image: ImageView) {
        if (et.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            et.setSelection(et.length())
            image.setImageResource(R.drawable.hide_number)
        } else {
            et.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            et.setSelection(et.length())
            image.setImageResource(R.drawable.show_number)
        }
    }


    private fun fingerprint() {
        FingerprintManager.FingerprintBuilder(this).setTitle("Unlock to use Authenticator")
            .setTitle("Touch the fingerprint sensor")
            .setNegativeButtonText("Dismiss")
            .build().authenticate(object : FingerprintCallback {
                override fun onAuthenticationCancelled() {
                    Log.d(TAG, "onAuthenticationCancelled: ")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationError: ")
                }

                override fun onAuthenticationFailed() {
                    Log.d(TAG, "onAuthenticationFailed: ")
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationHelp: ")
                }

                override fun onAuthenticationSuccessful() {
                    Log.d(TAG, "onAuthenticationSuccessful: ")
                    startActivityWithAnimation<MainActivity>()
                }

                override fun onBiometricAuthenticationInternalError(error: String?) {
                    Log.d(TAG, "onBiometricAuthenticationInternalError: ")
                }

                override fun onBiometricAuthenticationNotAvailable() {
                    Log.d(TAG, "onBiometricAuthenticationNotAvailable: ")
                }

                override fun onBiometricAuthenticationNotSupported() {
                    Log.d(TAG, "onBiometricAuthenticationNotSupported: ")
                }

                override fun onBiometricAuthenticationPermissionNotGranted() {
                    Log.d(TAG, "onBiometricAuthenticationPermissionNotGranted: ")
                }

                override fun onSdkVersionNotSupported() {
                    Log.d(TAG, "onSdkVersionNotSupported: ")
                }

            })
    }


}

private const val TAG = "PasswordScreen"