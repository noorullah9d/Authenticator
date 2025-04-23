package com.example.my.project.authenticator.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.databinding.AccountItemBinding
import com.example.my.project.authenticator.extensions.formatCode
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.invisible
import com.example.my.project.authenticator.extensions.loadIssuerLogo
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.utils.TotpCardState

class AccountAdapter(
    private val accounts: MutableList<TotpCardState>,
    private val onDeleteSelected: (Int, List<TotpCardState>) -> Unit,
    private val onHOTPRefreshClicked: (TotpCardState) -> Unit,
    private val onItemClick: (TotpCardState) -> Unit
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

    fun removeAccount(account: TotpCardState) {
        accounts.remove(account)
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

    inner class AccountViewHolder(private val binding: AccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: TotpCardState, isSelected: Boolean) {
            Log.d(TAG, "bind: account= $account")

            binding.apply {
                tvName.text = account.name
                val code = account.oneTimeCode.toString().padStart(6, '0')
                tvPassCode.text = code.formatCode()

                if (account.type == "TOTP") {
                    circularProgress.show()
                    icRefresh.invisible()
                    circularProgress.progress = account.secondsLeft.toFloat()
                    circularProgress.text = account.secondsLeft.toString()
                } else {
                    circularProgress.hide()
                    icRefresh.show()

                    icRefresh.setOnClickListener {
                        onHOTPRefreshClicked.invoke(account)
                    }
                }

                if (account.issuer.isEmpty()) {
                    tvIssuer.hide()
                    ivProfileImage.hide()
                    tvProfileImage.show()
                    tvProfileImage.setProfileImage(account.name)
                } else {
                    tvProfileImage.hide()
                    tvIssuer.show()
                    tvIssuer.text = account.issuer
                    ivProfileImage.show()
                    ivProfileImage.loadIssuerLogo(account.issuer)
                }

                root.setOnClickListener {
                    onItemClick.invoke(account)

                    if (!selectedAccounts.contains(account)) {
                        selectedAccounts.add(account)
                    }
                }
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