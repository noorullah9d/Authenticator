package com.example.my.project.authenticator.otp.data.repository

import com.example.my.project.authenticator.otp.data.database.PasswordDao
import com.example.my.project.authenticator.otp.data.database.toEntity
import com.example.my.project.authenticator.otp.data.database.toPassword
import com.example.my.project.authenticator.otp.domain.model.Password
import com.example.my.project.authenticator.otp.domain.repository.PasswordRepository
import javax.inject.Inject

class PasswordRepositoryImpl @Inject constructor(
    private val dao: PasswordDao
) : PasswordRepository {

    override suspend fun getAllPasswords(): List<Password> {
        return dao.getAllPasswords().map { it.toPassword() }
    }

    override suspend fun getPasswordById(id: Int): Password? {
        return dao.getPasswordById(id)?.toPassword()
    }

    override suspend fun insertPassword(password: Password) {
        dao.insertPassword(password.toEntity())
    }

    override suspend fun updatePassword(password: Password) {
        dao.updatePassword(password.toEntity())
    }

    override suspend fun deletePassword(password: Password) {
        dao.deletePassword(password.toEntity())
    }
}
