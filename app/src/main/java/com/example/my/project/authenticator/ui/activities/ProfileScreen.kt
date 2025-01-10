package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
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
        val edit = intent.getIntExtra("edit", 0)

        val id = intent.getIntExtra("id",-1)
        val keyName = intent.getStringExtra("key_name")
        val secretKey = intent.getStringExtra("secret_key")
        val tool = intent.getStringExtra("tool")
        val SHA = intent.getStringExtra("SHA")
        val filePath = intent.getStringExtra("filePath")
        val OTP = intent.getStringExtra("OTP")
        val category = intent.getStringExtra("category")


        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("key_name", keyName)
        bundle.putString("secret_key", secretKey)
        bundle.putString("tool", tool)

        bundle.putInt("edit", edit)
        bundle.putString("SHA", SHA)
        bundle.putString("filePath", filePath)
        bundle.putString("OTP", OTP)
        bundle.putString("category", category)
        Log.d(TAG, "onViewCreated: $category")


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

private const val TAG = "ProfileScreen"