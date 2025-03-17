package com.example.my.project.authenticator.utils

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

object GoogleSignInManager {

    private lateinit var credentialManager: CredentialManager

    suspend fun googleSignIn(
        context: Context,
        apiKey: String,
        filterByAuthorizedAccounts: Boolean,
        doOnSuccess: (GoogleIdTokenCredential) -> Unit,
        doOnError: (Exception) -> Unit,
    ) {
        if (::credentialManager.isInitialized.not()) {
            credentialManager = CredentialManager
                .create(context)
        }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(apiKey)
            .setAutoSelectEnabled(false)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest
            .Builder()
            .addCredentialOption(googleIdOption)
            .build()

        requestSignIn(
            context,
            request,
            apiKey,
            filterByAuthorizedAccounts,
            doOnSuccess,
            doOnError
        )
    }

    private suspend fun requestSignIn(
        context: Context,
        request: GetCredentialRequest,
        apiKey: String,
        filterByAuthorizedAccounts: Boolean,
        doOnSuccess: (GoogleIdTokenCredential) -> Unit,
        doOnError: (Exception) -> Unit,
    ) {
        try {
            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val tokenCredential = handleCredentials(result.credential)
            tokenCredential?.let {
                doOnSuccess(tokenCredential)
            } ?: doOnError(Exception("Invalid user"))
        } catch (e: Exception){
            if (e is NoCredentialException && filterByAuthorizedAccounts) {
                googleSignIn(
                    context,
                    apiKey,
                    false,
                    doOnSuccess,
                    doOnError
                )
            } else {
                doOnError(e)
            }
        }
    }

    private fun handleCredentials(credential: Credential): GoogleIdTokenCredential? {
        when (credential) {

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        return googleIdTokenCredential
                    } catch (e: GoogleIdTokenParsingException) {
                        println("Received an invalid google id token response $e")
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    println("Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                println("Unexpected type of credential")
            }
        }
        return null
    }
}