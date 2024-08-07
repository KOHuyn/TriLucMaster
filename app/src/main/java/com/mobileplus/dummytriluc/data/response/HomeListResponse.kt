package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemChart
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import java.text.DecimalFormat
import java.util.*
import kotlin.Exception
import kotlin.math.roundToInt

data class HomeListResponse(
    @SerializedName("CHART")
    @Expose
    val chart: MutableList<ItemChart>? = mutableListOf(),
    @SerializedName("AVG_SCORE_CHART")
    @Expose
    val avg: Float? = null,
    @SerializedName("PUNCH")
    @Expose
    val punch: Punch? = null,
    @SerializedName("CALORIES")
    @Expose
    val calories: GoalInfo? = null,
    @SerializedName("TIME_PRACTICE")
    @Expose
    val timePractice: GoalInfo? = null,
    @SerializedName("POWER")
    @Expose
    val power: Power? = null,
    @SerializedName("RANK")
    @Expose
    val rank: Rank? = null,
    @SerializedName("CHALLENGE")
    @Expose
    val challenge: Any? = null,
    @SerializedName("WEEK")
    @Expose
    val week: Week?,
    @SerializedName("REWARD")
    @Expose
    val reward: Any? = null,
    @SerializedName("HISTORY")
    @Expose
    val history: Any? = null,
    @SerializedName("LAST_PRACTICE")
    @Expose
    val lastPractice: ArrayList<LastPractice>? = arrayListOf()
)

data class Rank(
    @SerializedName("SCORE")
    @Expose
    val score: Int? = 0,
    @SerializedName("TOP")
    @Expose
    val top: Int? = 0,
    @SerializedName("RANK_TITLE")
    @Expose
    val rankTitle: String? = null,
    @SerializedName("NEXT_POINT")
    @Expose
    val nextPoint: Int? = 0,
    @SerializedName("RANK_UP")
    @Expose
    val rankUp: Int? = 0,
) {
    fun getProgress() =
        if (nextPoint != null && score != null && nextPoint != 0) (score * 100) / nextPoint else 0

    val scoreZ get() = score?.formatter() ?: "--"
}

data class Power(
    @SerializedName("GOAL")
    @Expose
    val goal: Int? = null,
    @SerializedName("TIME_TYPE")
    @Expose
    val timeType: String? = null,
    @SerializedName("DETAIL")
    @Expose
    val detail: ArrayList<ItemChart>? = null,
    @SerializedName("TOTAL")
    @Expose
    val totalPower: Int? = null,
) {
    fun getProgress() =
        if (totalPower != null && goal != null && goal != 0) (totalPower.toFloat() * 100 / goal.toFloat()).roundToInt() else 0

    val powerZ: String
        get() = totalPower?.formatter() ?: "-"
    val goalZ: String
        get() = goal?.formatter() ?: "-"
}

private fun Number.formatter(): String {
    val num = this.toLong()
    return try {
        val decimal = DecimalFormat("###,###")
        decimal.format(num)
    } catch (e: Exception) {
        num.toString()
    }
}

data class Punch(
    @SerializedName("GOAL")
    @Expose
    val goal: List<GoalItem>? = null,
    @SerializedName("TIME_TYPE")
    @Expose
    val timeType: String? = null,
    @SerializedName("TOTAL_PUNCH")
    @Expose
    val totalPunch: Int? = null
) {
    fun getProgress() =
        if (totalPunch != null && getGoalNumber() != null && getGoalNumber() != 0) (totalPunch.toFloat() * 100 / goalZ.toFloat()).roundToInt() else 0

    val totalZ: String get() = totalPunch?.formatter() ?: "-"
    val goalZ: String get() = goal?.sumBy { it.score ?: 0 }?.formatter() ?: "-"

    fun getGoalNumber(): Int? {
        return goal?.sumBy { it.score ?: 0 }
    }

    data class GoalItem(
        @SerializedName("title")
        @Expose
        val title: String? = null,
        @SerializedName("score")
        @Expose
        val score: Int? = null,
        @SerializedName("color")
        @Expose
        val color: String? = null,
    )
}
data class GoalInfo(
    @SerializedName("GOAL")
    @Expose
    val goal: Int? = null,
    @SerializedName("TIME_TYPE")
    @Expose
    val timeType: String? = null,
    @SerializedName("TOTAL")
    @Expose
    val total: Int? = null
) {
    fun getProgress() =
        if (total != null && goal != null && goal != 0) (total.toFloat() * 100 / goal.toFloat()).roundToInt() else 0

    val totalZ: String get() = total?.formatter() ?: "-"
    val goalZ: String get() = goal?.formatter() ?: "-"

    val goalType get() = GoalTimeType.getType(timeType)
}

enum class GoalTimeType(val type: String) {
    BY_DAY("by_day"),
    BY_WEEK("by_week"),
    BY_MONTH("by_month");

    companion object {
        fun getType(type: String?): GoalTimeType? {
            return values().find { it.type == type }
        }
    }
}
data class LastPractice(
    @Expose
    @SerializedName("practice_id")
    val practiceId: String?,
    @Expose
    @SerializedName("sum_total_power")
    val sumTotalPower: Long? = 0,
    @Expose
    @SerializedName("sum_total_punch")
    val sumTotalPunch: Long? = 0,
    @Expose
    @SerializedName("sum_calories")
    val sumCalories: String?,
    @Expose
    val title: String?,
    @Expose
    @SerializedName("start_time2")
    val startTime2: Long? = 0,
    @Expose
    @SerializedName("start_time1")
    val startTime1: Long? = 0,
    @Expose
    @SerializedName("end_time")
    val endTime: Long? = 0,
    @Expose
    @SerializedName("time")
    val time: Long? = 0,
) {
    fun getTimeLesson() = try {
        "${loadStringRes(R.string.time)} : ${DateTimeUtil.convertTimeStampToMMSS(time ?: 0)}s"
    } catch (e: Exception) {
        "${loadStringRes(R.string.time)} : --"
    }
}

data class Week(
    @Expose
    @SerializedName("TOTAL_CALORIES")
    val totalCalories: Int? = null,
    @Expose
    @SerializedName("TOTAL_PUNCH")
    val totalPunch: Int? = null,
    @Expose
    @SerializedName("TOTAL_POWER")
    val totalPower: Int? = null,
    @Expose
    @SerializedName("ACCURACY")
    val accuracy: String? = null,
    @Expose
    @SerializedName("CHART")
    val chart: MutableList<ItemChart> = mutableListOf()
) {
    val totalPunchZ get() = totalPunch?.formatter() ?: "--"
    val totalPowerZ get() = totalPower?.formatter() ?: "--"
    val totalCaloriesZ get() = totalCalories?.formatter() ?: "--"
}