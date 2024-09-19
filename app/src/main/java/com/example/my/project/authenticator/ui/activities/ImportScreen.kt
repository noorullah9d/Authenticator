package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my.project.authenticator.adapters.ImportedKeysAdapter
import com.example.my.project.authenticator.databinding.ActivityImportScreenBinding
import com.example.my.project.authenticator.extensions.showAskPasswordDialog
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.startActivityWithAnimationAndClearStack
import com.example.my.project.authenticator.otp.viewModel.ImportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportScreen : BaseActivity() {

    private lateinit var binding: ActivityImportScreenBinding
    private val viewModel by viewModels<ImportViewModel>()
    private lateinit var getInputStreamLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var importedKeysAdapter: ImportedKeysAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView with ImportedKeysAdapter
        setupRecyclerView()

        // Initialize the ActivityResultLauncher for choosing a file
        getInputStreamLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { content ->
            if (content == null) {
                finish()
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                contentResolver.openInputStream(content)?.use { importStream ->
                    val isPasswordNeeded = viewModel.prepareAndCheckPassword(importStream)
                    if (!isPasswordNeeded) {
                        viewModel.import()
                    } else {
                        showPasswordDialog()
                    }
                }
            }
        }

        binding.apply {
            if (viewModel.count == 0) {
                getInputStreamLauncher.launch(arrayOf("application/json"))
                viewModel.count++
            }



            backPress.setOnClickListener {
                finish()
            }

            tryAgainButton.setOnClickListener {
                getInputStreamLauncher.launch(arrayOf("application/json"))
            }

            addSelectedButton.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.addSelected()
                    startActivityWithAnimationAndClearStack<MainActivity>()
                }
            }
        }

        viewModel.importScreenState.observe(this) { state ->
            state?.let {
                // Update error text visibility
                binding.errorText.text = state.errorText

                // Check if importedKeys is null or empty
                val importedKeys = state.importedKeys

                binding.importedKeysList.visibility = if (!importedKeys.isNullOrEmpty()) View.VISIBLE else View.GONE
                binding.tryAgainButton.visibility = if (!state.errorText.isNullOrEmpty()) View.VISIBLE else View.GONE
                binding.importProgressMessage.visibility = if (importedKeys.isNullOrEmpty() && state.errorText.isNullOrEmpty()) View.VISIBLE else View.GONE
                binding.addSelectedButton.visibility = if (!importedKeys.isNullOrEmpty()) View.VISIBLE else View.GONE

                importedKeys?.let {
                    importedKeysAdapter.submitList(it)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        importedKeysAdapter = ImportedKeysAdapter(emptyList()) { index ->
            viewModel.changeCheck(index)
        }

        binding.importedKeysList.apply {
            layoutManager = LinearLayoutManager(this@ImportScreen)
            adapter = importedKeysAdapter
        }
    }


    private fun showPasswordDialog() {
        showAskPasswordDialog(
            onDismiss = {
                finish()
            },
            onSuccess = { password ->
                lifecycleScope.launch {
                    viewModel.import(password)
                }
            }
        )
    }


}