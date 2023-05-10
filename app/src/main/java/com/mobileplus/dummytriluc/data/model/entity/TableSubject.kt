package com.mobileplus.dummytriluc.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "table_subject")
data class TableSubject(
    @Expose
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @Expose
    val title: String,
    @Expose
    @SerializedName("image_path")
    val imagePath: String?,
    @Expose
    val status: Int
)