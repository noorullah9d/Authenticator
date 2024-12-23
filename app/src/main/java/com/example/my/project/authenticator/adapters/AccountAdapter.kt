package com.example.my.project.authenticator.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.databinding.AccountItemBinding
import com.example.my.project.authenticator.databinding.PopupMenuCustomBinding
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.utils.SwipeToDeleteCallback
import com.example.my.project.authenticator.utils.TotpCardState

class AccountAdapter(val accounts: MutableList<TotpCardState>, private val onItemLongClick: (Int, TotpCardState) -> Unit) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    fun addAccounts(newAccounts: List<TotpCardState>) {
        val initialSize = accounts.size
        accounts.addAll(newAccounts)
        notifyItemRangeInserted(initialSize, newAccounts.size)
    }

    fun removeAccounts(removedAccounts: List<TotpCardState>) {
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

        holder.itemView.setOnClickListener {
            it.context.copyTextToClipboard(account.oneTimeCode.toString())
        }

        holder.itemView.setOnLongClickListener { view ->
            (view.parent as? RecyclerView)?.suppressLayout(true)

            showPopupMenu(position, view, account) {
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
        val dialog = Dialog(view.context)
        val binding = PopupMenuCustomBinding.inflate(LayoutInflater.from(view.context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.window?.setLayout(370, WindowManager.LayoutParams.WRAP_CONTENT)

        binding.menuCopy.setOnClickListener {
            view.context.copyTextToClipboard(account.oneTimeCode.toString())
            dialog.dismiss()
        }

        binding.menuDelete.setOnClickListener {
            onItemLongClick.invoke(position, account)
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            onDismiss()
        }

        dialog.show()
    }

    fun getSwipeToDeleteCallback(context: Context): SwipeToDeleteCallback {
        return object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                onItemLongClick.invoke(position, accounts[position])
            }
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