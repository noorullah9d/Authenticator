package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my.project.authenticator.adapters.ImportedKeysAdapter
import com.example.my.project.authenticator.databinding.ActivityImportScreenBinding
import com.example.my.project.authenticator.extensions.showAskPasswordDialog
import com.example.my.project.authenticator.extensions.startActivityWithAnimationAndClearStack
import com.example.my.project.authenticator.extensions.toast
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

        setupRecyclerView()

        getInputStreamLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { content ->
            if (content == null) {
                finish()
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                contentResolver.openInputStream(content)?.use { importStream ->
                    val size: Int = importStream.available()

                    Log.d(TAG, "size: $size")

                    if (size == 0) {
                        toast("Invalid Json")
                        finish()
                    } else {
                        val isPasswordNeeded = viewModel.prepareAndCheckPassword(importStream)
                        if (!isPasswordNeeded) {
                            viewModel.import()
                            updateUI(true)
                        } else {
                            showPasswordDialog()
                        }
                    }
                }
            }
        }

        binding.apply {
            if (viewModel.count == 0) {
                getInputStreamLauncher.launch(arrayOf("application/json"))
                viewModel.count++
            }

            ivBackPress.setOnClickListener {
                finish()
            }

            tryAgainButton.setOnClickListener {
                getInputStreamLauncher.launch(arrayOf("application/json"))
            }

            addSelectedButton.setOnClickListener {
                lifecycleScope.launch {
                    val size = viewModel.importedKeys.filter { it.checked }
                    if (size.isNotEmpty()) {
                        viewModel.addSelected()
                        startActivityWithAnimationAndClearStack<MainActivity>()
                        viewModel.importedKeys.clear()
                    } else {
                        toast("Please Select any one")
                    }
                }
            }
        }

        updateUI(false)
    }

    private fun setupRecyclerView() {
        importedKeysAdapter = ImportedKeysAdapter { importedList, index ->
            Log.d(TAG, "setupRecyclerView: $index")
            viewModel.changeCheck(importedList, index)
            updateUI(true)
        }

        binding.importedKeysList.apply {
            layoutManager = LinearLayoutManager(this@ImportScreen)
            adapter = importedKeysAdapter
        }
    }

    private fun updateUI(isComing: Boolean) {
        binding.errorText.text = viewModel.errorText

        val importedKeys = viewModel.importedKeys
        Log.i(TAG, "updateUI: ${importedKeys.toList().size}")
        binding.importedKeysList.visibility = if (importedKeys.isNotEmpty()) View.VISIBLE else View.GONE
        binding.importProgressMessage.visibility = if (importedKeys.isEmpty() && viewModel.errorText.isNullOrEmpty()) View.VISIBLE else View.GONE
        binding.addSelectedButton.visibility = if (importedKeys.isNotEmpty()) View.VISIBLE else View.GONE
        if (!viewModel.errorText.isNullOrEmpty()) {
            toast("Wrong Password")
            finish()
        }
        if (isComing) {
            if (importedKeys.toList().isEmpty()) {
                toast("no key found")
                finish()
            }
        }

        importedKeysAdapter.submitList(importedKeys)
    }

    private fun showPasswordDialog() {
        showAskPasswordDialog(
            onDismiss = {
                finish()
            },
            onSuccess = { password ->
                lifecycleScope.launch {
                    viewModel.import(password)
                    updateUI(true)
                }
            }
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.importedKeys.clear()
    }

}


private const val TAG = "ImportScreen"