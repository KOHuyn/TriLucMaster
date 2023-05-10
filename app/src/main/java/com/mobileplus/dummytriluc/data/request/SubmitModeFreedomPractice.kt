package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KOHuyn on 2/17/2021
 */
data class SubmitModeFreedomPractice(
    @Expose
    @SerializedName("dummy_id")
    var dummyId: Int? = null,
    @Expose
    @SerializedName("mode")
    var mode: Int? = null,
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
    @SerializedName("data")
    var data: String? = null,
)
