package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentSetPasswordBinding
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.extensions.validatePassword
import com.example.my.project.authenticator.extensions.validatePasswordChange
import com.example.my.project.authenticator.utils.PrefsHelper
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.isVisible
import com.example.my.project.authenticator.analytics.SAVE_APP_PWD_CLICK
import com.example.my.project.authenticator.analytics.SET_APP_PWD_SCREEN
import com.example.my.project.authenticator.analytics.logScreen
import com.example.my.project.authenticator.analytics.postAnalytics

@AndroidEntryPoint
class SetPasswordFragment : Fragment() {

    private lateinit var binding: FragmentSetPasswordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().logScreen(SET_APP_PWD_SCREEN)

        binding.apply {
            if (PrefsHelper.userPassword.isNotEmpty()) {
                currentPassword.show()
//                currentPasswordNotCorrect.beVisible()
            }

            icBack.setOnClickListener { findNavController().popBackStack() }

            hidePassword.setOnClickListener {
                hidePassword(enterCurrentPassword, hidePassword)
            }

            hideNewPassword.setOnClickListener {
                hidePassword(etNewPassword, hideNewPassword)
            }

            hideConfirmPassword.setOnClickListener {
                hidePassword(etConfirmPassword, hideConfirmPassword)
            }

            savePassword.setOnDebouncedClickListener {
                requireActivity().postAnalytics(SAVE_APP_PWD_CLICK)
                if (currentPassword.isVisible) {
                    val isTrue = PrefsHelper.userPassword.validatePasswordChange(enterCurrentPassword.text.toString(), etNewPassword.text.toString(), etConfirmPassword.text.toString())
                    if (isTrue == "not") {
                        currentPasswordNotCorrect.show()
                    } else {
                        findNavController().popBackStack()
                    }
                } else {
                    val validate = etNewPassword.text.toString().validatePassword(etConfirmPassword.text.toString())
                    if (validate == "Password is valid") {
                        PrefsHelper.userPassword = etConfirmPassword.text.toString()
                        findNavController().popBackStack()
                    } else requireActivity().toast(validate)
                }

            }


        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

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


}

