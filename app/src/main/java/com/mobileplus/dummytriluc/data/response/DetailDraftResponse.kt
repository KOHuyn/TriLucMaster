package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.data.model.ItemPracticeDetailMain

data class DetailDraftResponse(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("data")
    val data: String = "",
    @Expose
    @SerializedName("start_time2")
    val startTime2: Long = 0,
    @Expose
    @SerializedName("end_time")
    val endTime: Long = 0,
    @Expose
    @SerializedName("start_time1")
    val startTime1: Long = 0,
    @Expose
    @SerializedName("image_path")
    val imagePath: String? = null,
    @Expose
    @SerializedName("video_path")
    val videoPath: String? = null,
    @Expose
    @SerializedName("folder_id")
    val folderId: Int? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @Expose
    @SerializedName("video_path_origin")
    val videoPathOrigin: String? = null,
    @Expose
    @SerializedName("image_path_origin")
    val imagePathOrigin: String? = null
)