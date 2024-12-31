package com.example.my.project.authenticator.otp.data.crypto

import android.util.Log
import com.example.my.project.authenticator.model.Account
import com.example.my.project.authenticator.otp.domain.crypto.SaveFirebase
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class SaveFirebaseImpl @Inject constructor() : SaveFirebase {

    private val fireStore = FirebaseFirestore.getInstance()

    override fun saveDataToDB(email: String, passcode: String, accountName: String, tool: String, category: String) {
        // Include 'tool' in the new account map
        val newAccount = mapOf(
            "accountName" to accountName,
            "passcode" to passcode,
            "category" to category,
            "tool" to tool
        )

        val documentRef = fireStore.collection("Authenticator").document(email)

        documentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val existingAccounts = document.get("accounts") as? MutableList<Map<String, String>> ?: mutableListOf()
                existingAccounts.add(newAccount)

                documentRef.update("accounts", existingAccounts)
                    .addOnSuccessListener {
                        Log.d("SaveFirebase", "New account added under the same email")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SaveFirebase", "Failed to add account: ${e.message}")
                    }
            } else {
                documentRef.set(mapOf("accounts" to listOf(newAccount)))
                    .addOnSuccessListener {
                        Log.d("SaveFirebase", "Document created and account added")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SaveFirebase", "Failed to create document: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("SaveFirebase", "Failed to retrieve document: ${e.message}")
        }
    }


    override fun retrieveDataFromDB(email: String, callback: (List<Account>?, String?) -> Unit) {
        val documentRef = fireStore.collection("Authenticator").document(email)

        documentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val accountsList = document.get("accounts") as? List<Map<String, String>>
                val accountObjects = accountsList?.map {
                    Account(it["accountName"].toString(), it["passcode"].toString(),it["category"].toString())
                } ?: listOf()
                callback(accountObjects, null) // Pass data to the callback
            } else {
                callback(null, "No data found for this email")
            }
        }.addOnFailureListener { e ->
            callback(null, "Failed to retrieve data: ${e.message}")
        }
    }


    override fun deleteAccount(email: String, accountName: String) {
        val documentRef = fireStore.collection("Authenticator").document(email)

        documentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val accountsList = document.get("accounts") as? MutableList<Map<String, String>> ?: mutableListOf()

                // Find the account by accountName and remove it
                val updatedAccountsList = accountsList.filterNot { it["accountName"] == accountName }

                // If the updated list has fewer items, proceed to update the document
                if (updatedAccountsList.size != accountsList.size) {
                    documentRef.update("accounts", updatedAccountsList)
                        .addOnSuccessListener {
                            Log.d("SaveFirebase", "Account successfully deleted: $accountName")
                        }
                        .addOnFailureListener { e ->
                            Log.e("SaveFirebase", "Failed to delete account: ${e.message}")
                        }
                } else {
                    Log.d("SaveFirebase", "No account found with the name: $accountName")
                }
            } else {
                Log.e("SaveFirebase", "No document found for email: $email")
            }
        }.addOnFailureListener { e ->
            Log.e("SaveFirebase", "Failed to retrieve document: ${e.message}")
        }
    }


}

