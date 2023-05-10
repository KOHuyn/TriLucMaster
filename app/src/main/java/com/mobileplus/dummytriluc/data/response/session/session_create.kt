package com.mobileplus.dummytriluc.data.response.session


import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.ui.utils.extensions.fromJsonSafe
import kotlin.math.abs

data class DataCoachSessionCreatedResponse(
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("practice_detail")
    val practiceDetail: MutableList<PracticeDetail> = mutableListOf()
) {
    data class PracticeDetail(
        @Expose
        @SerializedName("id")
        val id: Int? = null,
        @Expose
        @SerializedName("data")
        val data: String? = null,
        @Expose
        @SerializedName("type")
        val type: String? = null,
        @Expose
        @SerializedName("start_time1")
        val startTime1: Long? = null,
        @Expose
        @SerializedName("start_time2")
        val startTime2: Long? = null,
        @Expose
        @SerializedName("end_time")
        val endTime: Long? = null,
        @Expose
        @SerializedName("round")
        val round: Int? = 1
    ) {
        fun getTotalTime(): Int {
            return if (startTime1 == null || endTime == null) 0 else {
                abs(endTime - startTime1).toInt()
            }
        }

        fun transformToArrPos(gson: Gson): List<String> {
            return if (data != null) {
                gson.fromJsonSafe<List<DataBluetooth>>(data)?.sortedBy { it.time }
                    ?.mapNotNull { it.position }
                    ?: emptyList()
            } else emptyList()
        }

        fun transformToArrPosString(gson: Gson): String? {
            return if (data != null) {
                val arrPos = gson.fromJsonSafe<List<DataBluetooth>>(data)?.sortedBy { it.time }
                    ?.mapNotNull { it.position } ?: mutableListOf()
                if (arrPos.isNotEmpty()) {
                    arrPos.joinToString("", "", "-")
                } else null
            } else null
        }

        private fun minusTime(a: Long?, b: Long?): Long {
            return if (a == null || b == null) 0 else abs(a - b)
        }

        fun transformToArrDelay(gson: Gson): List<Long> {
            return if (data != null) {
                val arrData = gson.fromJsonSafe<List<DataBluetooth>>(data)?.sortedBy { it.time }
                    ?: emptyList()
                val arrDelay = mutableListOf<Long>()
                for (i in arrData.indices) {
                    val dataPunch = arrData[i]
                    if (i == 0) {
                        arrDelay.add(minusTime(dataPunch.time, startTime2))
                    } else {
                        val dataPunchPrev = arrData[i - 1]
                        arrDelay.add(minusTime(dataPunch.time, dataPunchPrev.time))
                    }
                }
                arrDelay
            } else emptyList()
        }
    }
}

