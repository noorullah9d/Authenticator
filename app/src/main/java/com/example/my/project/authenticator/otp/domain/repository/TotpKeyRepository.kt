package com.example.my.project.authenticator.otp.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey

interface TotpKeyRepository {
    fun getAllKeys(): Flow<List<EncryptedTotpKey>>

    suspend fun addKey(key: EncryptedTotpKey)

    suspend fun removeKey(key: EncryptedTotpKey)

    /** Edit a key by its id */
    suspend fun editKey(key: EncryptedTotpKey)
}