package com.mobileplus.dummytriluc.data.model.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "table_level")
data class TableLevel(
    @Expose
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @Expose
    val title: String,
    @Expose
    val description: String? = "",
    @Expose
    @SerializedName("image_path")
    val imagePath: String?,
    @Expose
    @SerializedName("min_point")
    val minPoint: Int?,
    @Expose
    @SerializedName("next_point")
    val nextPoint: Int?,
    @Expose
    val status: Int
)