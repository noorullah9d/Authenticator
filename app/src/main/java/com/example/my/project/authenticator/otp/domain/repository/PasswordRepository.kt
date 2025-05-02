package com.example.my.project.authenticator.otp.domain.repository

import com.example.my.project.authenticator.otp.domain.model.Password

interface PasswordRepository {
    suspend fun getAllPasswords(): List<Password>
    suspend fun getPasswordById(id: Int): Password?
    suspend fun insertPassword(password: Password)
    suspend fun updatePassword(password: Password)
    suspend fun deletePassword(password: Password)
}