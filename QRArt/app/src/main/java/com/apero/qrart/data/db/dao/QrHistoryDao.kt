package com.apero.qrart.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apero.qrart.data.db.model.HistoryEntity

/**
 * Created by KO Huyn on 26/06/2023.
 */
@Dao
interface QrHistoryDao {
    @Query("SELECT * FROM HISTORY_TABLE")
    suspend fun getListHistory(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: HistoryEntity): Long
}