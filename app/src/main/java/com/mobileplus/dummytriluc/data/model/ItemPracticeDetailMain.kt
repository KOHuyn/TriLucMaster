package com.mobileplus.dummytriluc.data.model

import android.graphics.Color
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.ui.main.home.adapter.PowerChartDescriptionAdapter
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
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
        var scoreTotalFace = 0
        var scoreTotalAbdomen = 0
        dataDetailPunch?.forEach { item ->
            if (item.key == BlePosition.FACE.key
                || item.key == BlePosition.LEFT_CHEEK.key
                || item.key == BlePosition.RIGHT_CHEEK.key
            ) {
                scoreTotalFace += item.score ?: 0
                if (items.none {
                        it.key == BlePosition.FACE.key || it.key == BlePosition.LEFT_CHEEK.key
                                || it.key == BlePosition.RIGHT_CHEEK.key
                    }) {
                    items.add(addPos(scoreTotalFace, BlePosition.FACE.title, BlePosition.FACE.key))
                } else {
                    items[items.indexOf(items.first { it.key == BlePosition.FACE.key })] =
                        addPos(scoreTotalFace, BlePosition.FACE.title, BlePosition.FACE.key)
                }
            } else if (item.key == BlePosition.ABDOMEN_UP.key
                || item.key == BlePosition.LEFT_ABDOMEN.key
                || item.key == BlePosition.ABDOMEN.key
                || item.key == BlePosition.RIGHT_ABDOMEN.key
            ) {
                scoreTotalAbdomen += item.score ?: 0
                if (items.none {
                        it.key == BlePosition.ABDOMEN_UP.key
                                || it.key == BlePosition.LEFT_ABDOMEN.key
                                || it.key == BlePosition.ABDOMEN.key
                                || it.key == BlePosition.RIGHT_ABDOMEN.key
                    }) {
                    items.add(
                        addPos(
                            scoreTotalAbdomen,
                            BlePosition.ABDOMEN.title,
                            BlePosition.ABDOMEN.key
                        )
                    )
                } else {
                    items[items.indexOf(items.first { it.key == BlePosition.ABDOMEN.key })] =
                        addPos(
                            scoreTotalAbdomen,
                            BlePosition.ABDOMEN.title,
                            BlePosition.ABDOMEN.key
                        )
                }
            } else {
                items.add(
                    addPos(
                        item.score ?: 0,
                        BlePosition.values().find { it.key == item.key }?.title ?: "---",
                        item.key
                    )
                )
            }
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