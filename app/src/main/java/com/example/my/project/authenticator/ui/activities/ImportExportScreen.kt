package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.databinding.ActivityImportExportScreenBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.ui.viewModel.ImportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportExportScreen : BaseActivity() {

    private lateinit var binding: ActivityImportExportScreenBinding
    private val viewModel by viewModels<ImportViewModel>()


    private lateinit var getInputStreamLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportExportScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        getInputStreamLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { content ->

            if (content == null) {
                finish()
                return@registerForActivityResult
            }


            lifecycleScope.launch {
                this@ImportExportScreen.contentResolver?.openInputStream(content)?.use { importStream ->
                    val isPasswordNeeded = viewModel.prepareAndCheckPassword(importStream)
                    if (!isPasswordNeeded) {
                        viewModel.import()
                    } else {
//                        toast("Corrupted File")
//                        showPasswordDialog()
                    }
                }
            }


        }



        binding.apply {


            importFile.setOnClickListener {
                startActivityWithAnimation<ImportScreen>()

            }

            exportFile.setOnClickListener {
                startActivityWithAnimation<ExportScreen>()
            }

            icBack.setOnClickListener {
                finish()
            }

        }
    }


}