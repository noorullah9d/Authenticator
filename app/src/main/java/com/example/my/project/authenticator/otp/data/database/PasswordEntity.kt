package com.example.my.project.authenticator.otp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String?,
    val emailOrUsername: String,
    val password: String,
    val notes: String?,
    val imageRes: Int
)

