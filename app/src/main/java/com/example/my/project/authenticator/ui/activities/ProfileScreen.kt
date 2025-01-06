package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
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


        val keyName = intent.getStringExtra("key_name")
        val secretKey = intent.getStringExtra("secret_key")
        val tool = intent.getStringExtra("tool")

        val bundle = Bundle()

        bundle.putString("key_name", keyName)
        bundle.putString("secret_key", secretKey)
        bundle.putString("tool", tool)

        navController.navigate(R.id.accountsDetails, bundle)


        when (navigateTo) {
            "ivEnterKey" -> {
                navController.navigate(R.id.accountsDetails)
            }

            "ivScanQR" -> {
                navController.navigate(R.id.QRScannerScreen)
            }
        }



    }
}