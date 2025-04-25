package com.example.my.project.authenticator.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.my.project.authenticator.otp.data.database.TotpDao
import com.example.my.project.authenticator.otp.data.database.TotpDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideTotpDatabase(@ApplicationContext context: Context): TotpDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TotpDatabase::class.java,
            TotpDatabase.DATABASE_NAME
        )
            .addMigrations(TotpDatabase.MIGRATION_1_2)
//            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideTotpDao(db: TotpDatabase): TotpDao = db.totpDao
}