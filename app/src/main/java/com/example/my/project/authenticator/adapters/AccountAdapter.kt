package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.utils.TotpCardState
import com.owl93.dpb.CircularProgressView


class AccountAdapter(private val accounts: List<TotpCardState>,private val onItemLongClick: (Int) -> Unit) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountNameTextView: TextView = itemView.findViewById(R.id.tvName)
        val passcodeTextView: TextView = itemView.findViewById(R.id.tvPassCode)
        val ivProfileImage: TextView = itemView.findViewById(R.id.ivProfileImage)
        val circularProgress: CircularProgressView = itemView.findViewById(R.id.circularProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.account_item, parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]



        holder.apply {

            accountNameTextView.text = account.name
            passcodeTextView.text = account.oneTimeCode.toString()

            circularProgress.progress = account.secondsLeft.toFloat()
            circularProgress.text = account.secondsLeft.toString()


            holder.ivProfileImage.setProfileImage(account.name)



            itemView.setOnLongClickListener {
                showPopupMenu(it, account)
                true
            }

        }


    }

    private fun showPopupMenu(view: View, account: TotpCardState) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.account_options_menu)


        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.menu_edit -> {
//                    true
//                }

                R.id.menu_delete -> {
                    onItemLongClick.invoke(account.id)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }


    override fun getItemCount() = accounts.size
}


