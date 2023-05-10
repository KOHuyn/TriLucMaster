package com.mobileplus.dummytriluc.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

/**
 * Created by KO Huyn on 28/10/2021.
 */
@Entity(tableName = "data_bluetooth_retry")
data class DataBluetoothRetryEntity(
    @Expose
    val data: String? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}