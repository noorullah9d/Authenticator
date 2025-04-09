package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityFeedbackScreenBinding
import com.example.my.project.authenticator.extensions.sendEmail
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackScreen : BaseActivity() {

    val viewModel by viewModels<CardSelectionViewModel>()
    private lateinit var binding: ActivityFeedbackScreenBinding

    private var messageBuilder: StringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChipGroup()
        initViews()
        setupClickListeners()

        binding.apply {
            ivBackPress.setOnClickListener {
                finish()
            }

            submitProblem.setOnClickListener {
                messageBuilder.append(etFeedback.text.trim().toString())
                if (messageBuilder.isEmpty()) {
                    toast(getString(R.string.field_should_not_empty))
                } else {
                    Log.d(TAG, "onCreate: feedback= $messageBuilder")
                    sendEmail("apps@galixo.ai", getString(R.string.feedback), messageBuilder.toString())
                }
            }
        }
    }

    private fun initViews() {
        binding.apply {
            etFeedback.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    Log.d(TAG, "onTextChanged: ${s?.length}")
                    countChars.text = "${s?.length}/500"
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun setupClickListeners() {
        binding.apply {

        }
    }

    private fun setupChipGroup() {
        val chipIds = arrayOf(
            R.id.chip_overall_service,
            R.id.chip_in_app_purchase,
            R.id.chip_additional_security,
            R.id.chip_performance,
            R.id.chip_user_support,
            R.id.chip_data_privacy
        )

        chipIds.forEach { chipId ->
            val chip = binding.chipGroup.findViewById<Chip>(chipId)
            chip.setOnClickListener {
                // Directly access the current state of chip after click
                if (chip.isChecked) {
                    // Append chip text to messageBuilder if chip is checked
                    messageBuilder.append(chip.text).append("\n")
                } else {
                    // Remove chip text from messageBuilder if chip is unchecked
                    val textToRemove = chip.text.toString() + "\n"
                    val start = messageBuilder.indexOf(textToRemove)
                    if (start >= 0) {
                        messageBuilder.delete(start, start + textToRemove.length)
                    }
                }
            }
        }
    }

    private fun submitFeedback() {
        val selectedCardIndex = viewModel.getSelectedCardIndex()
        Log.d(TAG, "onCreate: $selectedCardIndex")

        val subject = when (selectedCardIndex) {
            0 -> {
                "Overall Service"
            }

            1 -> {
                "In-app Purchases"
            }

            2 -> {
                "Additional Security"
            }

            3 -> {
                "Performance"
            }

            4 -> {
                "User Support"
            }

            5 -> {
                "Data Privacy"
            }

            else -> {
                "Issues"
            }
        }


        Log.d(TAG, "feedback: $subject")


        val feedback = binding.etFeedback.text.toString()
        if (feedback == "") toast("Enter Feedback")
        else sendEmail("apps@galixo.ai", subject, feedback)
    }
}


private const val TAG = "FeedbackScreen"