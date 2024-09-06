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

    @Query("SELECT * FROM $totpTableName")
    fun queryAll(): Flow<List<TotpDbEntity>>
}