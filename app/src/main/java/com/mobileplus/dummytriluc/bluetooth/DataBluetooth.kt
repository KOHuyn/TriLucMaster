package com.mobileplus.dummytriluc.bluetooth

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class BluetoothResponse(
    @Expose
    @SerializedName("mode")
    val mode: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("session_id")
    var sessionId: Int? = null,
    @Expose
    @SerializedName("practice_id")
    var practiceId: Int? = null,
    @Expose
    @SerializedName("machine_id")
    val machineId: Int? = null,
    @Expose
    @SerializedName("start_time1")
    val startTime1: Long? = null,
    @Expose
    @SerializedName("end_time")
    var endTime: Long? = null,
    @Expose
    @SerializedName("start_time2")
    val startTime2: Long? = null,
    @Expose
    @SerializedName("data")
    val data: MutableList<DataBluetooth?> = mutableListOf(),
    @Expose
    @SerializedName("error")
    val error: JsonObject? = null,
) {
    companion object {
        const val MODE_FREE_FIGHT = 5
        const val MODE_ACCORDING_LED = 4
    }
}

data class DataBluetooth(
    @Expose
    @SerializedName("f")
    var force: Float? = null,
    //    Hủy truyền onTarget
    @Expose
    @SerializedName("o")
    var onTarget: Int? = null,
    @Expose
    @SerializedName("p")
    val position: String? = null,
    @Expose
    @SerializedName("t")
    var time: Long? = null
)


data class BluetoothResponseMachine(
    @Expose
    @SerializedName("mode")
    val mode: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("session_id")
    var sessionId: Int? = null,
    @Expose
    @SerializedName("lesson_id")
    var practiceId: Int? = null,
    @Expose
    @SerializedName("machine_id")
    val machineId: Int? = null,
    @Expose
    @SerializedName("start_time1")
    val startTime1: Long? = null,
    @Expose
    @SerializedName("end_time")
    var endTime: Long? = null,
    @Expose
    @SerializedName("start_time2")
    val startTime2: Long? = null,
    @Expose
    @SerializedName("data")
    val data: MutableList<DataBluetoothMachine?> = mutableListOf(),
    @Expose
    @SerializedName("error")
    val error: JsonObject? = null,
) {
    companion object {
        const val MODE_FREE_FIGHT = 2
        const val MODE_ACCORDING_LED = 3
    }
}

data class DataBluetoothMachine(
    @Expose
    @SerializedName("f")
    var force: Float? = null,
    @Expose
    @SerializedName("o")
    var onTarget: Int? = null,
    @Expose
    @SerializedName("p")
    val position: String? = null,
    @Expose
    @SerializedName("t")
    var time: Long? = null
)
