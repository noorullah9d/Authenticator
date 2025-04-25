package com.example.my.project.authenticator.otp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TotpDbEntity::class, Categories::class], version = 2, exportSchema = false)
abstract class TotpDatabase : RoomDatabase() {
    abstract val totpDao: TotpDao

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
    }
}