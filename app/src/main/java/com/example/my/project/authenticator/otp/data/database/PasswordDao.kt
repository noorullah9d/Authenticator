package com.example.my.project.authenticator.otp.data.database

import androidx.room.*
import com.example.my.project.authenticator.otp.domain.entities.PasswordEntity

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords")
    suspend fun getAllPasswords(): List<PasswordEntity>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Int): PasswordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntity)

    @Update
    suspend fun updatePassword(password: PasswordEntity)

    @Delete
    suspend fun deletePassword(password: PasswordEntity)
}
