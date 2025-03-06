package com.example.my.project.authenticator.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.databinding.AccountItemBinding
import com.example.my.project.authenticator.extensions.invisible
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.utils.TotpCardState


class AccountAdapter(
    private val accounts: MutableList<TotpCardState>,
    private val onDeleteSelected: (Int, List<TotpCardState>) -> Unit,
    private val onHOTPRefreshClicked: (TotpCardState) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private val selectedAccounts = mutableSetOf<TotpCardState>()
    private var isSelectionMode = false

    fun addAccounts(newAccounts: List<TotpCardState>) {
        val initialSize = accounts.size
        accounts.addAll(newAccounts)
        notifyItemRangeInserted(initialSize, newAccounts.size)
    }

    fun removeAccounts(removedAccounts: List<TotpCardState>) {
        accounts.removeAll(removedAccounts)
        notifyDataSetChanged()
    }

    fun selectAll() {
        selectedAccounts.clear()
        selectedAccounts.addAll(accounts)
        notifyDataSetChanged()
    }

    fun deselectAll() {
        isSelectionMode = false
        selectedAccounts.clear()
        notifyDataSetChanged()
    }

    fun getSelectedAccounts(): List<TotpCardState> {
        return selectedAccounts.toList()
    }

    inner class AccountViewHolder(private val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(account: TotpCardState, isSelected: Boolean) {
            /*val otp = account.oneTimeCode.toString().length
            binding.tvName.text = account.name
            if (otp == 5) {
                binding.tvPassCode.text = "0" + account.oneTimeCode.toString()
            } else {
                binding.tvPassCode.text = account.oneTimeCode.toString()
            }*/
            binding.tvName.text = account.name
            binding.tvPassCode.text = account.oneTimeCode.toString().padStart(6, '0')

            if (account.type == "TOTP") {
                binding.circularProgress.show()
                binding.icRefresh.invisible()
                binding.circularProgress.progress = account.secondsLeft.toFloat()
                binding.circularProgress.text = account.secondsLeft.toString()
            } else {
                binding.circularProgress.hide()
                binding.icRefresh.show()

                binding.icRefresh.setOnClickListener{
                    onHOTPRefreshClicked.invoke(account)
                }
            }

//            binding.circularProgress.progress = account.secondsLeft.toFloat()
//            binding.circularProgress.text = account.secondsLeft.toString()
            binding.ivProfileImage.setProfileImage(account.name)

            binding.selected.visibility = if (isSelected) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(adapterPosition, account)
                } else {
                    it.context.copyTextToClipboard(account.oneTimeCode.toString())
                }
            }

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    enterSelectionMode()
                }
                toggleSelection(adapterPosition, account)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = AccountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        val isSelected = selectedAccounts.contains(account)
        holder.bind(account, isSelected)
    }

    override fun getItemCount(): Int = accounts.size

    private fun toggleSelection(position: Int, account: TotpCardState) {
        if (selectedAccounts.contains(account)) {
            selectedAccounts.remove(account)
        } else {
            selectedAccounts.add(account)
        }
        notifyItemChanged(position)

        onDeleteSelected.invoke(position, selectedAccounts.toList())

        if (selectedAccounts.isEmpty()) {
            exitSelectionMode()
        }
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedAccounts.clear()
        notifyDataSetChanged()
    }

    fun updateAccounts(newAccounts: List<TotpCardState>) {
        Log.d(TAG, "updateAccounts: ${newAccounts.size}")
        val selectedAccountIds = selectedAccounts.map { it.id }
        accounts.clear()
        accounts.addAll(newAccounts)

        selectedAccounts.clear()
        selectedAccounts.addAll(accounts.filter { it.id in selectedAccountIds })

        notifyDataSetChanged()
    }
}


private const val TAG = "AccountAdapter"