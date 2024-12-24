package com.example.my.project.authenticator.otp.domain.repository

import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import kotlinx.coroutines.flow.Flow

interface TotpKeyRepository {
    fun getAllKeys(email: String="",cats:String, searchQuery: String = ""): Flow<List<EncryptedTotpKey>>
    suspend fun addCategories(cats: Categories)
    suspend fun addKey(key: EncryptedTotpKey)

    suspend fun removeKey(key: EncryptedTotpKey)

    /** Edit a key by its id */
    suspend fun editKey(key: EncryptedTotpKey)
    fun getAllData(email: String): List<EncryptedTotpKey>
    fun getAllGroups(): Flow<List<Categories>>

    fun isKeyExists(name: String, secret: String): Int
    suspend fun replaceEntity(id: Int, name: String, base32Secret: String)

}