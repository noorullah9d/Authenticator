package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityFeedbackScreenBinding
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


        val cardViews = listOf(
            Pair(binding.overAllServices, binding.textView),
            Pair(binding.inAppPurchases, binding.tvInApp),
            Pair(binding.overAditionalSecurity, binding.tvAditionalSecurity),
            Pair(binding.overPerformance, binding.tvPerformance),
            Pair(binding.userSupport, binding.tvuserSupport),
            Pair(binding.dataPrivacy, binding.tvdataPrivacy)
        )

        binding.apply {

            ivBackPress.setOnClickListener {
                finish()
            }


            submitProblem.setOnClickListener {
                val selectedCardIndex = viewModel.getSelectedCardIndex()
                if (selectedCardIndex != null) {
                    finish()
                    Log.d(TAG, "Selected card index: $selectedCardIndex")
                } else {
                    Log.d(TAG, "No card is selected")
                }
            }


        }


        viewModel.selectedCardIndex.observe(this) { selectedIndex ->
            cardViews.forEachIndexed { index, pair ->
                val (cardView, textView) = pair
                val isSelected = index == selectedIndex

                val cardColor = if (isSelected) R.color.secondary_color else R.color.light_blue
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, cardColor))

                val textColor = if (isSelected) R.color.white else R.color.secondary_color
                textView.setTextColor(ContextCompat.getColor(this, textColor))
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


private const val TAG = "FeedbackScreen"