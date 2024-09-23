package com.example.my.project.authenticator.otp.domain.repository

import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import kotlinx.coroutines.flow.Flow

interface TotpKeyRepository {
    fun getAllKeys(email: String): Flow<List<EncryptedTotpKey>>

    suspend fun addKey(key: EncryptedTotpKey)

    suspend fun removeKey(key: EncryptedTotpKey)

    /** Edit a key by its id */
    suspend fun editKey(key: EncryptedTotpKey)
    fun getAllData(email: String): List<EncryptedTotpKey>

    fun isKeyExists(name: String, secret: String): Int
    suspend fun replaceEntity(id: Int, name: String, base32Secret: String)

}