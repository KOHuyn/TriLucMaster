package com.mobileplus.dummytriluc.data.response

/**
 * Created by KOHuyn on 3/26/2021
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DataSendDraftResponse(
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("image_path")
    val imagePath: String? = null,
    @Expose
    @SerializedName("data")
    val data: String? = null,
    @Expose
    @SerializedName("start_time1")
    val startTime1: String? = null,
    @Expose
    @SerializedName("start_time2")
    val startTime2: String? = null,
    @Expose
    @SerializedName("video_path")
    val videoPath: String? = null,
    @Expose
    @SerializedName("end_time")
    val endTime: String? = null,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("id")
    val id: Int? = null
)