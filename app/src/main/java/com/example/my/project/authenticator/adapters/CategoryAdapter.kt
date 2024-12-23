package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ItemCategoryBinding
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.otp.data.database.Categories

class CategoryAdapter(
    private val categories: List<Categories>,
    private val viewModel: CardSelectionViewModel,
    val groupCallBack: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedIndex: Int? = null

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Categories, isSelected: Boolean) {
            binding.categoryTextView.text = category.categories



            if (isSelected) {
                binding.selectCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.n_sky_blue))
                binding.selectCard.strokeColor = ContextCompat.getColor(binding.root.context, R.color.n_sky_blue)
                binding.categoryTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            } else {
                binding.selectCard.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.categoryTextView.setTextColor(ContextCompat.getColor(binding.root.context, R.color.n_dark_gray))
                binding.selectCard.strokeColor = ContextCompat.getColor(binding.root.context, R.color.n_stroke_color)
            }



            binding.root.setOnClickListener {
                viewModel.setSelectedCategory(adapterPosition)
                groupCallBack.invoke(category.categories)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }


    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedIndex)
    }


    override fun getItemCount(): Int = categories.size


    fun updateSelectedIndex(newIndex: Int?) {
        val oldIndex = selectedIndex
        selectedIndex = newIndex
        if (oldIndex != null) notifyItemChanged(oldIndex)
        if (newIndex != null) notifyItemChanged(newIndex)
    }

}
