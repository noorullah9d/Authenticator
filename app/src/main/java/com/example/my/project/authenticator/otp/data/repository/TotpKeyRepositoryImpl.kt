package com.example.my.project.authenticator.otp.data.repository

import android.util.Log
import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.otp.data.database.TotpDao
import com.example.my.project.authenticator.otp.data.database.TotpDbMapper
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TotpKeyRepositoryImpl(
    private val dao: TotpDao,
) : TotpKeyRepository {


    override fun getAllKeys(email: String, cats: String, searchQuery: String): Flow<List<EncryptedTotpKey>> {
        return if (email == "") {

            if (cats == "Default" || cats == "") {
                dao.queryDefaultAll("",searchQuery).map { it.map(TotpDbMapper::toTotpKey) }
            } else {
                dao.queryAll(cats, searchQuery).map { it.map(TotpDbMapper::toTotpKey) }
            }

        } else {
            return if (cats == "Default" || cats == "") {
                dao.queryDefaultAll(email,  searchQuery).map {
                    Log.d(TAG, "getAllKeys: ${it.size}")
                    it.map(TotpDbMapper::toTotpKey)
                }
            } else {
                dao.queryAll(email, cats, searchQuery).map {
                    Log.d(TAG, "getAllCatsKeys: ${it.size}")
                    it.map(TotpDbMapper::toTotpKey)
                }
            }
        }
    }

    override suspend fun addKey(key: EncryptedTotpKey) {
        Log.d(TAG, "addKey: ${TotpDbMapper.fromTotpKey(key)}")
        dao.insert(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun addCategories(cats: Categories) {
        dao.insertCats(cats)
    }

    override suspend fun deleteCategories(id: Int) {
       dao.deleteCategoryById(id)
    }

    override suspend fun removeKey(key: EncryptedTotpKey) {
        dao.delete(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun editKey(key: EncryptedTotpKey) {
        dao.update(TotpDbMapper.fromTotpKey(key))
    }

    override fun getAllData(email: String): List<EncryptedTotpKey> {
        return dao.queryAllData(email).map(TotpDbMapper::toTotpKey)
    }

    override fun getAllGroups(): Flow<List<Categories>> = dao.getAllGroups()


    override fun isKeyExists(name: String, secret: String): Int {
        val count = dao.countByNameAndSecret(name, secret)
        Log.d(TAG, "isKeyExists: ${dao.countByNameAndSecret(name, secret)}")
        return count
    }

    override suspend fun replaceEntity(id: Int, name: String, base32Secret: String) {
        dao.updateNameAndSecretById(id, name, base32Secret)

    }


}

private const val TAG = "TotpKeyRepositoryImpl"