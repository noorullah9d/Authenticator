package com.example.my.project.authenticator.otp.domain.crypto

import com.example.my.project.authenticator.model.Account

interface SaveFirebase {
    fun saveDataToDB(email: String, passcode: String, accountName: String)
    fun retrieveDataFromDB(email: String, callback: (List<Account>?, String?) -> Unit)
    fun deleteAccount(email: String, accountName: String)
}