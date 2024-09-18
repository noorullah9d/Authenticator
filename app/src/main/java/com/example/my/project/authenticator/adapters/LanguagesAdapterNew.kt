package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.LanguagesItemNewBinding
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.changeCardStorkColor
import com.example.my.project.authenticator.model.LanguagesModel

class LanguagesAdapterNew(
    private val currentLang: String,
    private val languageSelected: (LanguagesModel) -> Unit
) : RecyclerView.Adapter<LanguagesAdapterNew.LanguagesViewHolder>() {
    private var selectedPosition: Int? = null

    var checkDefault = true
    private var languagesList = ArrayList<LanguagesModel>()


    fun setData(languagesList: ArrayList<LanguagesModel>) {
        this.languagesList = languagesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LanguagesAdapterNew.LanguagesViewHolder {
        val binding =
            LanguagesItemNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguagesAdapterNew.LanguagesViewHolder, position: Int) {

        holder.bindData(currentLang, languagesList[holder.adapterPosition], languageSelected)
    }

    override fun getItemCount(): Int {
        return languagesList.size
    }

    inner class LanguagesViewHolder(val binding: LanguagesItemNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            currentLang: String,
            language: LanguagesModel,
            languageSelected: (LanguagesModel) -> Unit
        ) {
            binding.apply {
                if (checkDefault) {

                    if (currentLang == languagesList[adapterPosition].code) {
                        selectedPosition = position
                    }

                    if (selectedPosition != null) {
                        val isSelected = adapterPosition == selectedPosition

                        if (isSelected) {
                            selectorIcon.setImageResource(R.drawable.selector_icon)
                            root.changeCardStorkColor(R.color.md_theme_light_primary, itemView.context.theme)
                            root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.of_white))

                        } else {
                            root.changeCardStorkColor(R.color.white, itemView.context.theme)
                            root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.of_white))

                            selectorIcon.setImageResource(R.drawable.ic_unchecked)
                        }
                    }


                } else {
                    val isSelected = adapterPosition == (selectedPosition ?: 0)

                    if (isSelected) {

                        selectorIcon.setImageResource(R.drawable.selector_icon)
                        root.changeCardStorkColor(R.color.md_theme_light_primary, itemView.context.theme)
                        root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_bg))

                    } else {

                        root.changeCardStorkColor(R.color.card_bg, itemView.context.theme)
                        root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_bg))
                        selectorIcon.setImageResource(R.drawable.ic_unchecked)

                    }
                }





                "${language.name}  (${language.code})".also { languageName.text = it }
                localName.text = language.localName




                root.setOnClickListener {
                    checkDefault = false
                    languageSelected.invoke(language)
                    selectItem(adapterPosition)
                }
            }

        }
    }

    fun selectItem(position: Int) {
        val previousPosition = selectedPosition ?: 0
        selectedPosition = position
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition ?: 0)
        }
    }


}