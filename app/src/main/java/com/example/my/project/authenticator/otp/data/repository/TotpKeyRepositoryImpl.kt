package com.example.my.project.authenticator.otp.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.my.project.authenticator.otp.data.database.TotpDao
import com.example.my.project.authenticator.otp.data.database.TotpDbMapper
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository

class TotpKeyRepositoryImpl(
    private val dao: TotpDao,
) : TotpKeyRepository {

    override fun getAllKeys(email:String): Flow<List<EncryptedTotpKey>> {
        return dao.queryAll(email).map {
            it.map(TotpDbMapper::toTotpKey)
        }
    }

    override suspend fun addKey(key: EncryptedTotpKey) {
        Log.d(TAG, "addKey: ${TotpDbMapper.fromTotpKey(key)}")
        dao.insert(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun removeKey(key: EncryptedTotpKey) {
        dao.delete(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun editKey(key: EncryptedTotpKey) {
        dao.update(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun getAllData(): List<EncryptedTotpKey> {
        return dao.queryAllData().map (TotpDbMapper::toTotpKey)
    }

}

private const val TAG = "TotpKeyRepositoryImpl"