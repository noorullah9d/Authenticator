package com.example.my.project.authenticator.otp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Categories(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val categories: String
)
