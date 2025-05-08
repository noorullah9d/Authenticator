package com.example.my.project.authenticator.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ItemPasswordBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.otp.domain.model.Password

class PasswordAdapter(
    private var items: List<Password>,
    private val onItemClick: (Password) -> Unit,
    private val onCopyClicked: (String) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    inner class PasswordViewHolder(val binding: ItemPasswordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Password) {
            binding.apply {
                tvName.text = item.name
                tvEmail.text = item.emailOrUsername
                if (item.profileImagePath.isNullOrEmpty()) {
                    ivProfileImage.hide()
                    tvProfileImage.show()
                    tvProfileImage.setProfileImage(item.name)
                } else {
                    tvProfileImage.hide()
                    ivProfileImage.show()
                    ivProfileImage.load(item.profileImagePath.toUri()) {
                        placeholder(R.drawable.ic_profile_placeholder)
                        error(R.drawable.ic_profile_placeholder)
                    }
                }

                icCopy.setOnClickListener {
                    onCopyClicked(item.password)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val binding =
            ItemPasswordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PasswordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<Password>) {
        this.items = newList.reversed()
        notifyDataSetChanged()
    }
}