package com.example.my.project.authenticator.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityMainBinding
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

                findNavController(R.id.nav_fragment).navigate(R.id.homeFragment)

            }

            ivSettings.setOnClickListener {
                ivSettings.setImageResource(R.drawable.ic_selected_settings)
                ivHome.setImageResource(R.drawable.ic_unselect_home)

                findNavController(R.id.nav_fragment).navigate(R.id.settingScreen)

            }

        }


    }
}