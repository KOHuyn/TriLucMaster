package com.mobileplus.dummytriluc.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.local.db.dao.BluetoothDao
import com.mobileplus.dummytriluc.data.local.db.dao.UtilsDao
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity
import com.mobileplus.dummytriluc.data.model.entity.TableConfig
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject

@Database(
    entities = [TableLevel::class, TableSubject::class, TableConfig::class, DataBluetoothRetryEntity::class, BleErrorRequest::class],
    exportSchema = false,
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun utilsDao(): UtilsDao
    abstract fun bluetoothDao(): BluetoothDao
}