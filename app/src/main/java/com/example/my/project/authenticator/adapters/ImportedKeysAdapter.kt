package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ItemImportedKeyBinding
import com.example.my.project.authenticator.otp.viewModel.ImportedItemState

class ImportedKeysAdapter(
    private val onCheckedChange: (ArrayList<ImportedItemState>, Int) -> Unit
) : ListAdapter<ImportedItemState, ImportedKeysAdapter.ImportedKeyViewHolder>(DiffCallback) {

    class ImportedKeyViewHolder(val binding: ItemImportedKeyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportedKeyViewHolder {
        val binding = ItemImportedKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImportedKeyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ImportedKeyViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            nameText.text = item.name
            similarityText.text = when {
                !item.secretSimilarity.isNullOrEmpty() -> "Secret value is similar to ${item.secretSimilarity}"
                !item.nameSimilarity.isNullOrEmpty() -> "Name is similar to ${item.nameSimilarity}"
                else -> "Not similar to any of the existing"
            }

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.checked

            checkBox.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked){
                    checkBox.setButtonIconDrawableResource(R.drawable.checked_drawable)
                }else{
                    checkBox.setButtonIconDrawableResource(R.drawable.unchecked_drawable)
                }

                item.checked = isChecked
                onCheckedChange(ArrayList(currentList), position)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ImportedItemState>() {
        override fun areItemsTheSame(oldItem: ImportedItemState, newItem: ImportedItemState): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ImportedItemState, newItem: ImportedItemState): Boolean {
            return oldItem == newItem
        }
    }
}