package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.PairResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import java.lang.StringBuilder

/**
 * Created by KO Huyn on 1/19/2021.
 */
data class ItemDisciple(
    @Expose
    @SerializedName("student_id")
    val studentId: Int? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    @SerializedName("hit_total")
    val hitTotal: Int? = null,
    @Expose
    @SerializedName("power_point")
    val powerPoint: Int? = null,
    @Expose
    @SerializedName("total_time_practice")
    val totalTimePractice: Int? = null,
    @Expose
    @SerializedName("nhom_tham_gia")
    val groupJoined: MutableList<PairResponse>? = mutableListOf(),
    var isSelected: Boolean = false,
    @Expose
    var isChoosePractice: Boolean = false
) {
    fun getAllGroupName(): String? {
        return if (groupJoined.isNullOrEmpty()) null else groupJoined.joinToString(",") { it.title }
    }

    fun getTimePractice(): String =
        when {
            totalTimePractice == null -> "0 ${loadStringRes(R.string.second)}"
            totalTimePractice > 60 -> "${(totalTimePractice / 60)} ${loadStringRes(R.string.minute)}"
            else -> "$totalTimePractice ${loadStringRes(R.string.second)}"
        }
}
