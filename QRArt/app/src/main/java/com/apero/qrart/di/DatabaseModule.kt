package com.apero.qrart.di

import android.app.Application
import androidx.room.Room
import com.apero.qrart.data.db.QRArtDatabase
import com.apero.qrart.data.db.dao.QrHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by KO Huyn on 26/06/2023.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): QRArtDatabase {
        return Room.databaseBuilder(application, QRArtDatabase::class.java, "qrart.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: QRArtDatabase): QrHistoryDao {
        return database.getHistoryDao()
    }
}