package com.mobileplus.dummytriluc.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

/**
 * Created by KOHuyn on 2/25/2021
 */
@Entity(tableName = "table_config")
data class TableConfig(
    @Expose
    val name: String? = null,
    @Expose
    val description: String? = null,
    @Expose
    val value: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}