package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.my.project.authenticator.databinding.ActivityFeedbackScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackScreen : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}