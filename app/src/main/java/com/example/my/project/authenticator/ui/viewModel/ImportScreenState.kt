package com.example.my.project.authenticator.ui.viewModel

data class ImportScreenState(
    val errorText: String? = null,
    val importedKeys: List<ImportedItemState>? = null,
)

data class ImportedItemState(
    val name: String,
    val secretKey: String,
    val nameSimilarity: String? = null,
    val secretSimilarity: String? = null,
    var checked: Boolean = false,
    val SHA: String? = null,
    val type: String? = null,
)
