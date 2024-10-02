package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.appclass.DragRatingView
import com.example.my.project.authenticator.databinding.ActivityFeedbackScreenBinding
import com.example.my.project.authenticator.extensions.openAppInPlayStore
import com.example.my.project.authenticator.extensions.sendEmail
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.CardSelectionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackScreen : BaseActivity() {

    val viewModel by viewModels<CardSelectionViewModel>()
    private lateinit var binding: ActivityFeedbackScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.apply {

            val cardViews = listOf(
                Pair(overAllServices, textView),
                Pair(inAppPurchases, tvInApp),
                Pair(overAditionalSecurity, tvAditionalSecurity),
                Pair(overPerformance, tvPerformance),
                Pair(userSupport, tvuserSupport),
                Pair(dataPrivacy, tvdataPrivacy)
            )


            ivBackPress.setOnClickListener {
                finish()
            }


            submitProblem.setOnClickListener {
                feedback()
            }


            viewModel.selectedCardIndex.observe(this@FeedbackScreen) { selectedIndex ->
                cardViews.forEachIndexed { index, pair ->
                    val (cardView, textView) = pair
                    val isSelected = index == selectedIndex

                    val cardColor = if (isSelected) R.color.secondary_color else R.color.light_blue
                    cardView.setCardBackgroundColor(ContextCompat.getColor(this@FeedbackScreen, cardColor))

                    val textColor = if (isSelected) R.color.white else R.color.secondary_color
                    textView.setTextColor(ContextCompat.getColor(this@FeedbackScreen, textColor))
                }
            }

            cardViews.forEachIndexed { index, pair ->
                val (cardView, _) = pair
                cardView.setOnClickListener {
                    viewModel.toggleCardSelection(index)
                }
            }

        }


    }

    private fun feedback() {
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