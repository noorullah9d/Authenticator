package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.otp.viewModel.ImportedItemState

class ImportedKeysAdapter(
    private var items: List<ImportedItemState>,
    private val onCheckedChange: (Int) -> Unit
) : RecyclerView.Adapter<ImportedKeysAdapter.ImportedKeyViewHolder>() {

    // ViewHolder class
    class ImportedKeyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        val similarityText: TextView = itemView.findViewById(R.id.similarityText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportedKeyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_imported_key, parent, false)
        return ImportedKeyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImportedKeyViewHolder, position: Int) {
        val item = items[position]
        holder.nameText.text = item.name
        holder.similarityText.text = when {
            !item.secretSimilarity.isNullOrEmpty() -> "Secret value is similar to ${item.secretSimilarity}"
            !item.nameSimilarity.isNullOrEmpty() -> "Name is similar to ${item.nameSimilarity}"
            else -> "Not similar to any of the existing"
        }
        holder.checkBox.isChecked = item.checked
        holder.checkBox.setOnCheckedChangeListener { _, _ -> onCheckedChange(position) }
    }

    override fun getItemCount(): Int = items.size

    // Helper method to update the list
    fun submitList(newItems: List<ImportedItemState>) {
        items = newItems
        notifyDataSetChanged()
    }
}