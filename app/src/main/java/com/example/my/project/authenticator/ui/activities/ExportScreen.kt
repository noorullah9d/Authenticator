package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.databinding.ActivityExportScreenBinding
import com.example.my.project.authenticator.otp.domain.usecases.SavingMode
import com.example.my.project.authenticator.otp.viewModel.ExportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExportScreen : AppCompatActivity() {

    private val exportOptions = listOf(
        "No encryption" to SavingMode.NoEncryption,
        "Encrypt only keys" to SavingMode.KeyEncryption,
        "Encrypt everything" to SavingMode.FullEncryption
    )
    private lateinit var exportOptionsAdapter: ArrayAdapter<String>

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


        binding.backPress.setOnClickListener { finish() }


    }


    private fun setupExportOptionsSpinner() {
        val options = exportOptions.map { it.first }
        exportOptionsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        exportOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.exportTypeSpinner.adapter = exportOptionsAdapter


        binding.exportTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    binding.passwordEditText.visibility = View.VISIBLE
                } else {
                    binding.passwordEditText.visibility = View.GONE
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
                finish()  // Handle back press on cancel
                return@registerForActivityResult
            }

            // Perform export operation in a coroutine
            lifecycleScope.launch {
                this@ExportScreen.contentResolver?.openOutputStream(contentUri)?.use { outputStream ->

//
                    val selectedMode = exportOptions[binding.exportTypeSpinner.selectedItemPosition].second
                    val password = binding.passwordEditText.text.toString()

                    viewModel.export(selectedMode, password, outputStream)
                }

                finish()
            }
        }
    }


    private fun setupExportButton() {
        binding.exportButton.setOnClickListener {
            getOutputStreamLauncher.launch("export.json")
        }
    }

}