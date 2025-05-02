package com.example.my.project.authenticator.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.databinding.ItemGuideBinding
import com.example.my.project.authenticator.otp.domain.model.GuideItem

@SuppressLint("NotifyDataSetChanged")
class GuideAdapter(
    private val onGuideSelected: (url: String) -> Unit
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    private var platforms = ArrayList<GuideItem>()

    fun setData(data: ArrayList<GuideItem>) {
        this.platforms = data
        notifyDataSetChanged()
    }

    inner class GuideViewHolder(
        val binding: ItemGuideBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(platform: GuideItem) {
            binding.apply {
                name.text = platform.name
                icon.setImageResource(platform.imgRes)
                root.setOnClickListener {
                    onGuideSelected.invoke(platform.url)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val binding = ItemGuideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuideViewHolder(binding)
    }

    override fun getItemCount(): Int = platforms.size

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val platform = platforms[position]
        holder.bind(platform)
    }
}