package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.StorageDetailsSpinnerArrayAdapter
import com.example.my.project.authenticator.databinding.ActivityExportScreenBinding
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.domain.usecases.SavingMode
import com.example.my.project.authenticator.otp.viewModel.ExportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.OutputStream

@AndroidEntryPoint
class ExportScreen : BaseActivity() {

    private val exportOptions = listOf(
        "No encryption" to SavingMode.NoEncryption,
        "Encrypt only keys" to SavingMode.KeyEncryption,
        "Encrypt everything" to SavingMode.FullEncryption
    )

    private lateinit var binding: ActivityExportScreenBinding
    private val viewModel: ExportViewModel by viewModels()
    private lateinit var getOutputStreamLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupExportOptionsSpinner()
        setupOutputStreamLauncher()
        setupExportButton()


        binding.dropdownIcon.setOnClickListener {
            binding.exportTypeSpinner.performClick()
        }




        binding.backPress.setOnClickListener { finish() }


    }


    private fun setupExportOptionsSpinner() {
        val options = exportOptions.map { it.first }

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(
            this,
            options,
            false
        )

        binding.exportTypeSpinner.adapter = exportOptionsAdapter

        binding.exportTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    binding.passwordEditText.beVisible()
                } else {
                    binding.passwordEditText.beGone()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }


    private fun setupOutputStreamLauncher() {
        getOutputStreamLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { contentUri ->
            if (contentUri == null) {
                finish()
                return@registerForActivityResult
            }


            lifecycleScope.launch {
                this@ExportScreen.contentResolver?.openOutputStream(contentUri)?.use { outputStream ->
                    Log.d(TAG, "setupOutputStreamLauncher: ${binding.exportTypeSpinner.selectedItemPosition}")
                    when (binding.exportTypeSpinner.selectedItemPosition) {
                        0 -> {
                            exportFun(0, outputStream)
                        }

                        1 -> {
                            exportFun(2, outputStream)
                        }

                        2 -> {
                            exportFun(2, outputStream)
                        }
                    }

                }

                finish()
            }
        }
    }


    private fun setupExportButton() {
        binding.apply {

            exportButton.setOnClickListener {
                if (passwordEditText.isVisible) {
                    if (passwordEditText.text.isNotEmpty()) {
                        getOutputStreamLauncher.launch("export.json")
                    } else {
                        toast(getString(R.string.enter_password))
                    }
                } else {
                    getOutputStreamLauncher.launch("export.json")
                }
            }
        }
    }

    private suspend fun exportFun(position: Int, outputStream: OutputStream) {
        val selectedMode = exportOptions[position].second
        val password = binding.passwordEditText.text.toString()

        viewModel.export(selectedMode, password, outputStream)
    }


}


private const val TAG = "ExportScreen"