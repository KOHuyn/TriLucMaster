package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by KO Huyn on 12/29/2020.
 */

data class SubmitChallengeRequest(
    @Expose
    @SerializedName("dummy_id")
    var dummyId: Int? = null,
    @Expose
    @SerializedName("challenge_id")
    var challengeId: Int? = null,
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
    @SerializedName("video_path")
    var videoPath: String? = null,
    @Expose
    @SerializedName("data")
    var data: String? = null,
)