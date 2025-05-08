package com.example.my.project.authenticator.otp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.my.project.authenticator.otp.domain.entities.TotpDbEntity
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

    @Query("SELECT * FROM $totpTableName WHERE category = :cats AND name LIKE '%' || :searchQuery || '%'")
    fun queryAll(cats: String, searchQuery: String): Flow<List<TotpDbEntity>>

    @Query("SELECT * FROM $totpTableName WHERE (email = :email OR email='') AND category = :cats AND name LIKE '%' || :searchQuery || '%'")
    fun queryAll(email: String, cats: String = "Default", searchQuery: String): Flow<List<TotpDbEntity>>

    @Query("SELECT * FROM $totpTableName WHERE (email = :email OR email='') AND name LIKE '%' || :searchQuery || '%'")
    fun queryDefaultAll(email: String, searchQuery: String): Flow<List<TotpDbEntity>>


    @Query("SELECT * FROM $totpTableName WHERE email = :email")
    fun queryAllData(email: String): List<TotpDbEntity>

    @Query("SELECT id FROM $totpTableName WHERE name = :name AND secretKey = :secret")
    fun countByNameAndSecret(name: String, secret: String): Int

    @Query("UPDATE $totpTableName SET name = :newName, secretKey = :newSecret WHERE id = :id")
    fun updateNameAndSecretById(id: Int, newName: String, newSecret: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCats(cats: Categories)

    @Query("DELETE FROM Categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)


    @Query("Select * From Categories")
    fun getAllGroups(): Flow<List<Categories>>


}