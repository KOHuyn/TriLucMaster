package com.mobileplus.dummytriluc.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobileplus.dummytriluc.data.model.entity.TableConfig
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject

@Dao
interface UtilsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveListSubject(subjects: List<TableSubject>): List<Long>

    @Query("select *from table_subject")
    fun getAllSubject(): List<TableSubject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveListLevel(levels: List<TableLevel>): List<Long>

    @Query("select *from table_level")
    fun getAllLevel(): List<TableLevel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveListConfig(configs: List<TableConfig>): List<Long>

    @Query("select * from table_config")
    fun getAllConfig(): List<TableConfig>

}