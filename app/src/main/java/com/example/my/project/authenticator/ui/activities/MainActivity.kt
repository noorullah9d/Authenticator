package com.example.my.project.authenticator.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityMainBinding
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.showCustomDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.apply {

            btnStartOpt.setOnClickListener {
                logFirebaseEvent("scan_option", mapOf("passkey" to "clicked"))
                btnStartOpt.setImageResource(R.drawable.ic_cross)
                showCustomDialog { result ->

                    when (result) {
                        "ivScanQR" -> {
                            val intent = Intent(this@MainActivity, ProfileScreen::class.java)
                            intent.putExtra("bundle", "ivScanQR")
                            startActivity(intent)
                        }

                        "ivEnterKey" -> {
                            val intent = Intent(this@MainActivity, ProfileScreen::class.java)
                            intent.putExtra("bundle", "ivEnterKey")
                            startActivity(intent)
                        }

                        "dismiss" -> {
                            btnStartOpt.setImageResource(R.drawable.add)
                        }
                    }

                }
            }



            ivHome.setOnClickListener {
                ivSettings.setImageResource(R.drawable.ic_home_settings)
                ivHome.setImageResource(R.drawable.ic_home)

                val navController = findNavController(R.id.nav_fragment)
                if (navController.currentDestination?.id != R.id.homeFragment) {
                    navController.navigate(R.id.homeFragment)
                }
            }

            ivSettings.setOnClickListener {
                ivSettings.setImageResource(R.drawable.ic_selected_settings)
                ivHome.setImageResource(R.drawable.ic_unselect_home)

                val navController = findNavController(R.id.nav_fragment)
                if (navController.currentDestination?.id != R.id.settingScreen) {
                    navController.navigate(R.id.settingScreen)
                }
            }

        }


    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )
        }
    }


}