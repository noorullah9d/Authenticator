package com.example.my.project.authenticator.otp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

const val totpTableName = "totp"

@Dao
interface TotpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(totp: TotpDbEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(totp: TotpDbEntity)

    @Delete
    suspend fun delete(totp: TotpDbEntity)

    @Query("SELECT * FROM $totpTableName WHERE email = :email")
    fun queryAll(email: String): Flow<List<TotpDbEntity>>

    @Query("SELECT * FROM $totpTableName WHERE email = :email")
    fun queryAllData(email: String): List<TotpDbEntity>

    @Query("SELECT id FROM $totpTableName WHERE name = :name AND secretKey = :secret")
    fun countByNameAndSecret(name: String, secret: String): Int

    @Query("UPDATE $totpTableName SET name = :newName, secretKey = :newSecret WHERE id = :id")
    fun updateNameAndSecretById(id: Int, newName: String, newSecret: String): Int





}