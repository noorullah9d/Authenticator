package com.example.my.project.authenticator.otp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.my.project.authenticator.otp.data.database.TotpDao
import com.example.my.project.authenticator.otp.data.database.TotpDbMapper
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository

class TotpKeyRepositoryImpl(
    private val dao: TotpDao,
) : TotpKeyRepository {

    override fun getAllKeys(): Flow<List<EncryptedTotpKey>> {
        return dao.queryAll().map { it.map(TotpDbMapper::toTotpKey) }
    }

    override suspend fun addKey(key: EncryptedTotpKey) {
        dao.insert(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun removeKey(key: EncryptedTotpKey) {
        dao.delete(TotpDbMapper.fromTotpKey(key))
    }

    override suspend fun editKey(key: EncryptedTotpKey) {
        dao.update(TotpDbMapper.fromTotpKey(key))
    }
}