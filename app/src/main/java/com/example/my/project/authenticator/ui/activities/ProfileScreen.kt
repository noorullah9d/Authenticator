package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityProfileScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileScreen : BaseActivity() {

    private lateinit var binding: ActivityProfileScreenBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navigateTo = intent.getStringExtra("bundle")
        val backStack = intent.getIntExtra("backStack", 0)
        when (navigateTo) {
            "ivEnterKey" -> {
                navController.navigate(R.id.accountsDetails)
            }

            "ivScanQR" -> {
                navController.navigate(R.id.QRScannerScreen)
            }
        }

//        onBackPressedDispatcher.addCallback(this) {
//            if (backStack == 0) {
//                navController.popBackStack()
//            } else {
//                finish()
//            }
//        }


    }
}