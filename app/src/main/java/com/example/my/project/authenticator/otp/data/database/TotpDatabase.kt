package com.example.my.project.authenticator.otp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.my.project.authenticator.otp.domain.entities.PasswordEntity
import com.example.my.project.authenticator.otp.domain.entities.TotpDbEntity

@Database(
    entities = [TotpDbEntity::class, Categories::class, PasswordEntity::class],
    version = 3,
    exportSchema = false
)
abstract class TotpDatabase : RoomDatabase() {
    abstract val totpDao: TotpDao
    abstract val passwordDao: PasswordDao

    companion object {
        const val DATABASE_NAME = "mf_authenticator_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check if the 'issuer' column already exists
                val cursor = database.query("PRAGMA table_info($totpTableName)")
                var columnExists = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(1)
                    if (columnName == "issuer") {
                        columnExists = true
                        break
                    }
                }
                cursor.close()

                // Only add the column if it doesn't exist
                if (!columnExists) {
                    database.execSQL("ALTER TABLE $totpTableName ADD COLUMN issuer TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS passwords (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                url TEXT,
                emailOrUsername TEXT NOT NULL,
                password TEXT NOT NULL,
                notes TEXT,
                imageRes INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }
    }
}