package com.example.my.project.authenticator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.model.Account
import com.example.my.project.authenticator.utils.TOTPGenerator

class AccountAdapter(private val accounts: List<Account>) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountNameTextView: TextView = itemView.findViewById(R.id.tvName)
        val passcodeTextView: TextView = itemView.findViewById(R.id.tvPassCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.account_item, parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.accountNameTextView.text = account.accountName
//        holder.passcodeTextView.text = account.passcode

        val secretKey = account.passcode
//        val totpCode = TOTPGenerator.generateTOTP(secretKey)
//        holder.passcodeTextView.text = totpCode

//        TOTPGenerator.parseTOTPURI(secretKey)

        val generatedTOTP = TOTPGenerator.generateTOTP(TOTPGenerator.decodeBase32Secret("AGAKZPHSCWSRZ66Y"))
        holder.passcodeTextView.text = generatedTOTP
    }

    override fun getItemCount() = accounts.size
}