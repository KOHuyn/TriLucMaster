package com.mobileplus.dummytriluc.data.model

import android.graphics.Color
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.ui.main.home.adapter.PowerChartDescriptionAdapter
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.mobileplus.dummytriluc.ui.utils.extensions.BodyPosition
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import kotlin.math.abs

data class ItemPracticeDetailMain(
    @Expose
    @SerializedName("id")
    val id: Int,
    @Expose
    @SerializedName("practice_id")
    val practiceId: Int,
    @Expose
    @SerializedName("start_time1")
    val startTime1: Long? = 0,
    @Expose
    @SerializedName("time")
    val time: Long? = 0,
    @Expose
    @SerializedName("total_power")
    val totalPower: Long? = 0,
    @Expose
    @SerializedName("total_punch")
    val totalPunch: Long? = 0,
    @Expose
    @SerializedName("max_power")
    val maxPower: Long? = 0,
    @Expose
    @SerializedName("data")
    val data: String? = null,
    @Expose
    @SerializedName("data_detail_punch")
    val dataDetailPunch: MutableList<DataDetailPunch>? = mutableListOf(),
    var isSelected: Boolean = false
) {
    fun getCurrDate(): String? {
        return if (startTime1 == null) null else DateTimeUtil.convertTimeStampToDate(abs(startTime1))
    }

    fun getTimehhmm(): String? {
        return if (startTime1 == null) null else DateTimeUtil.convertTimeStampToHHMM(abs(startTime1))
    }

    fun getTimePractice(): String? {
        return if (time == null) null else DateTimeUtil.convertTimeStampToMMSS(abs(time))
    }

    fun items(): MutableList<PowerChartDescriptionAdapter.ItemDescriptionChartPower> {
        val items = mutableListOf<PowerChartDescriptionAdapter.ItemDescriptionChartPower>()
        dataDetailPunch?.forEach { item ->
            items.add(
                addPos(
                    item.score ?: 0,
                    BodyPosition.values()
                        .find { it.key == item.key }?.titleRes?.let { loadStringRes(it) } ?: "---",
                    item.key
                )
            )
        }
        return items
    }

    private fun addPos(score: Int, title: String, key: String?) =
        PowerChartDescriptionAdapter.ItemDescriptionChartPower(
            Color.WHITE,
            score, title, key
        )

}

data class DataDetailPunch(
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("key")
    val key: String? = null,
    @Expose
    @SerializedName("punch")
    val punch: Int? = 0,
    @Expose
    @SerializedName("score")
    val score: Int? = 0,
)