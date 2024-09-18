package com.example.my.project.authenticator.otp.data.database

import androidx.room.*
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

    @Query("SELECT * FROM $totpTableName")
    fun queryAllData(): List<TotpDbEntity>

    @Query("SELECT COUNT(*) FROM $totpTableName WHERE name = :name AND secret = :secret")
    suspend fun countByNameAndSecret(name: String, secret: ByteArray): Int

    @Query("SELECT * FROM $totpTableName WHERE name = :name AND secret = :secret LIMIT 1")
    fun getByNameAndSecret(name: String, secret: ByteArray): TotpDbEntity?


}