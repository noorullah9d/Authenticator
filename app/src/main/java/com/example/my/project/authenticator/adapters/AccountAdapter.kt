package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.databinding.AccountItemBinding
import com.example.my.project.authenticator.databinding.PopupMenuCustomBinding
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.utils.TotpCardState

class AccountAdapter(
    val accounts: MutableList<TotpCardState>,
    private val onItemLongClick: (Int,TotpCardState) -> Unit
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    fun addAccounts(newAccounts: List<TotpCardState>) {
        val initialSize = accounts.size
        accounts.addAll(newAccounts)
        notifyItemRangeInserted(initialSize, newAccounts.size)
    }

    fun removeAccounts(removedAccounts: List<TotpCardState>) {
        // Iterate through the list of items to remove
        removedAccounts.forEach { removedAccount ->
            val indexToRemove = accounts.indexOfFirst { it.id == removedAccount.id }
            if (indexToRemove != -1) {
                accounts.removeAt(indexToRemove)
                notifyItemRemoved(indexToRemove)
            }
        }
    }

    class AccountViewHolder(private val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(account: TotpCardState) {
            binding.tvName.text = account.name
            binding.tvPassCode.text = account.oneTimeCode.toString()
            binding.circularProgress.progress = account.secondsLeft.toFloat()
            binding.circularProgress.text = account.secondsLeft.toString()
            binding.ivProfileImage.setProfileImage(account.name)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = AccountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]

        holder.itemView.setOnLongClickListener { view ->
            (view.parent as? RecyclerView)?.suppressLayout(true)

            showPopupMenu(position,view, account) {
                (view.parent as? RecyclerView)?.suppressLayout(false)
            }
            true
        }


        holder.bind(account)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    private fun showPopupMenu(position: Int, view: View, account: TotpCardState, onDismiss: () -> Unit) {
        val binding = PopupMenuCustomBinding.inflate(LayoutInflater.from(view.context))
        val popupWindow = PopupWindow(binding.root, 370, 200, true)



        binding.menuCopy.setOnClickListener {
            view.context.copyTextToClipboard(account.oneTimeCode.toString())
            popupWindow.dismiss()
        }

        binding.menuDelete.setOnClickListener {
            onItemLongClick.invoke(position, account)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(view)

        popupWindow.setOnDismissListener {
            onDismiss()
        }
    }



    fun updateSecondsLeftAtPosition(position: Int, secondsLeft: Int) {
        accounts[position].secondsLeft = secondsLeft
        notifyItemChanged(position, secondsLeft)
    }

    fun updateOneTimeCodeAtPosition(position: Int, oneTimeCode: Int) {
        accounts[position].oneTimeCode = oneTimeCode
        notifyItemChanged(position, oneTimeCode)
    }


}

private const val TAG = "AccountAdapter"