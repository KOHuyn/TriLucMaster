package com.mobileplus.dummytriluc.data.local.db.dao

import androidx.room.*
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity

/**
 * Created by KO Huyn on 28/10/2021.
 */
@Dao
interface BluetoothDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDataBluetoothRequest(data: DataBluetoothRetryEntity): Long

    @Delete
    fun deleteDataBluetoothRequest(data: DataBluetoothRetryEntity)

    @Query("DELETE FROM data_bluetooth_retry WHERE data=:content")
    fun deleteDataBluetoothRequestByContent(content: String)

    @Query("DELETE FROM data_bluetooth_retry")
    fun deleteAllBluetoothRequest()

    @Query("SELECT * FROM data_bluetooth_retry")
    fun getAllDataBluetoothRetry(): List<DataBluetoothRetryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDataBluetoothError(data: BleErrorRequest): Long

    @Delete
    fun deleteDataBluetoothError(data: BleErrorRequest)

    @Query("DELETE FROM error_ble_table WHERE content=:content")
    fun deleteDataBluetoothErrorByContent(content: String)

    @Query("DELETE FROM error_ble_table")
    fun deleteAllBluetoothError()

    @Query("SELECT * FROM error_ble_table")
    fun getAllDataBluetoothError(): List<BleErrorRequest>

    @Delete
    fun deleteBluetoothJSONError(bleError: BleErrorRequest)

}