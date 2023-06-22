package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth

data class SubmitPracticeResultRequest(
    @Expose
    @SerializedName("dummy_id")
    var dummyId: Int? = null,
    @Expose
    @SerializedName("practice_id")
    var practiceId: Int? = null,
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
    @Expose
    @SerializedName("level_value")
    var levelValue: Int? = null,
    @Expose
    @SerializedName("sessionId")
    var sessionId: String? = null,
    @Expose
    @SerializedName("dataArr")
    //ignore when push server
    var dataArr: List<BluetoothResponse>? = emptyList()
)