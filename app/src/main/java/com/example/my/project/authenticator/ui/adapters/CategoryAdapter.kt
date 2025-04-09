package com.example.my.project.authenticator.ui.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ItemCategoryBinding
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.otp.data.database.Categories

class CategoryAdapter(
    private val categories: List<Categories>,
    private val viewModel: CardSelectionViewModel,
    val groupCallBack: (String) -> Unit,
    val catsId: (Categories) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    var selectedPosition: Int? = null
    var longSelected: Boolean = false

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Categories, isSelected: Boolean) {

            binding.apply {

                categoryTextView.text = category.categories

                if (isSelected) {
                    selectCard.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.n_import_buttons))
                    selectCard.strokeColor = ContextCompat.getColor(root.context, R.color.n_import_buttons)
                    categoryTextView.setTextColor(ContextCompat.getColor(root.context, R.color.n_sky_blue))
                    val typeface: Typeface? = ResourcesCompat.getFont(root.context, R.font.open_sans_bold)
                    categoryTextView.typeface = typeface

                } else {
                    selectCard.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.white))
                    categoryTextView.setTextColor(ContextCompat.getColor(root.context, R.color.n_dark_grey))
                    selectCard.strokeColor = ContextCompat.getColor(root.context, R.color.white)
                    val typeface: Typeface? = ResourcesCompat.getFont(root.context, R.font.open_sans_regular)
                    categoryTextView.typeface = typeface

                }

                /*when (adapterPosition) {
                    selectedPosition -> {
                        selectedCategory.beVisible()
                    }

                    else -> {
                        selectedCategory.beGone()
                    }
                }*/

                /*root.setOnLongClickListener {
                    notifyItemChanged(selectedPosition ?: 0)
                    selectedPosition = adapterPosition
                    catsId.invoke(category)
                    selectedCategory.beVisible()
                    groupCallBack.invoke(category.categories)
                    true
                }*/

                root.setOnClickListener {
                    if (!isSelected) {
                        viewModel.setSelectedCategory(adapterPosition)
                        groupCallBack.invoke(category.categories)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }


    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if (categories.isEmpty() || position >= categories.size) return
        holder.bind(categories[position], position == viewModel.selectedIndex)
    }

    override fun getItemCount(): Int = categories.size

    fun updateSelectedIndex(newIndex: Int?) {
        val oldIndex = viewModel.selectedIndex
        viewModel.selectedIndex = newIndex
        if (oldIndex != null) notifyItemChanged(oldIndex)
        if (newIndex != null) notifyItemChanged(newIndex)
    }
}
