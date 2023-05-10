package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SubmitCoachDraftRequest(
    @Expose
    @SerializedName("start_time1")
    var startTime1: Long? = null,
    @Expose
    @SerializedName("start_time2")
    var startTime2: Long? = null,
    @Expose
    @SerializedName("end_time")
    var endTime: Long? = null,
    @Expose
    @SerializedName("video_path")
    var videoPath: String? = null,
    @Expose
    @SerializedName("data")
    var data: String? = null,
    @Expose
    @SerializedName("title")
    var title: String? = null,
)