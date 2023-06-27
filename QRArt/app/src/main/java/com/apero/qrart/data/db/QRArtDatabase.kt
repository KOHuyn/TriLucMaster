package com.apero.qrart.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apero.qrart.data.db.dao.QrHistoryDao
import com.apero.qrart.data.db.model.HistoryEntity

/**
 * Created by KO Huyn on 26/06/2023.
 */

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class QRArtDatabase : RoomDatabase() {
    abstract fun getHistoryDao(): QrHistoryDao
}