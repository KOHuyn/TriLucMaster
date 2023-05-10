package com.mobileplus.dummytriluc.bluetooth.request


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class BleSessionRequest(
    @Expose
    @SerializedName("id")
    var id: Int? = null,
    @Expose
    @SerializedName("l_count")
    var lessonCount: Int? = null,
    @Expose
    @SerializedName("u_count")
    var userCount: Int? = null,
    @Expose
    @SerializedName("l_data")
    var lessonData: MutableList<BleLessonDataOfSession> = mutableListOf(),
    @Expose
    @SerializedName("u_data")
    var arrDisciple: MutableList<BleArrDisciple> = mutableListOf(),
    @Expose
    @SerializedName("mode")
    var mode: Int = 10
) {
    enum class ActionChange(private val key: Int) {
        NEXT(0), PREV(1), PAUSE(2), RESUME(3), END(4), RELOAD(5);

        fun writeBle(): String = """{"action":${this.key}}"""
    }

    data class BleLessonDataOfSession(
        @Expose
        @SerializedName("mode")
        var mode: Int? = null,
        @Expose
        @SerializedName("id")
        var lessonId: Int? = null,
        @Expose
        @SerializedName("round")
        var round: Int? = null,
        @Expose
        @SerializedName("total_time")
        var totalTime: Int? = null,
        @Expose
        @SerializedName("a_p")
        var arrPos: String? = null,
//        @Expose
//        @SerializedName("arr_position")
//        var arrPosition: List<String>? = null,
//        @Expose
//        @SerializedName("arr_delay")
//        var arrDelay: List<Long>? = null
    )

    data class BleArrDisciple(
        @Expose
        @SerializedName("id")
        var id: Int? = null,
        @Expose
        @SerializedName("u_n")
        var userName: String? = null
    )
}

data class SessionResponse(
    @Expose
    @SerializedName("sessionId")
    val sessionId: Int? = null
)