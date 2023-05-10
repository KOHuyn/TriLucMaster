package com.mobileplus.dummytriluc.data.response.session


import android.widget.TextView
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition

data class CoachSessionResultResponse(
    @Expose
    @SerializedName("sump1")
    val sump1: String? = null,
    @Expose
    @SerializedName("sump2")
    val sump2: String? = null,
    @Expose
    @SerializedName("sump3")
    val sump3: String? = null,
    @Expose
    @SerializedName("sump4")
    val sump4: String? = null,
    @Expose
    @SerializedName("sump5")
    val sump5: String? = null,
    @Expose
    @SerializedName("sump6")
    val sump6: String? = null,
    @Expose
    @SerializedName("sump7")
    val sump7: String? = null,
    @Expose
    @SerializedName("sump8")
    val sump8: String? = null,
    @Expose
    @SerializedName("sump9")
    val sump9: String? = null,
    @Expose
    @SerializedName("sumpa")
    val sumpa: String? = null,
    @Expose
    @SerializedName("sumpb")
    val sumpb: String? = null,
    @Expose
    @SerializedName("score")
    var score: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("nguoi_tao")
    val userCreated: UserCreated? = null
) {
    data class UserCreated(
        @Expose
        @SerializedName("id")
        val id: Int? = null,
        @Expose
        @SerializedName("full_name")
        val fullName: String? = null,
        @Expose
        @SerializedName("avatar_path")
        val avatarPath: String? = null,
        @Expose
        @SerializedName("rank_point")
        val rankPoint: Int? = null
    )

    fun getArrPosScore(): MutableList<Pair<String, Int>> {
        val arrPosScore = mutableListOf<Pair<String, Int>>()
        arrPosScore.add(Pair(BlePosition.FACE.title, plus(sump1, sump2, sump3)))
        arrPosScore.add(Pair(BlePosition.LEFT_CHEST.title, plus(sump4)))
        arrPosScore.add(Pair(BlePosition.RIGHT_CHEST.title, plus(sump5)))
        arrPosScore.add(Pair(BlePosition.ABDOMEN.title, plus(sump6, sump7, sump8, sump9)))
        arrPosScore.add(Pair(BlePosition.LEFT_LEG.title, plus(sumpa)))
        arrPosScore.add(Pair(BlePosition.RIGHT_LEG.title, plus(sumpb)))

        return arrPosScore
    }

    fun setRankTop(txt: TextView, rank: Int) {
        if (rank == 1) {
            txt.text = ""
        } else {
            txt.text = String.format("#%d", rank)
        }

        when (rank) {
            1 -> {
                txt.setBackgroundResource(R.drawable.background_top1_ranking)
            }
            2, 3 -> {
                txt.setBackgroundResource(R.color.clr_primary)
            }
            else -> {
                txt.setBackgroundResource(R.color.clr_tab)
            }
        }
    }

    private fun plus(vararg x: String?): Int {
        var sum = 0
        for (i in x) {
            if (i?.toIntOrNull() != null) {
                sum += i.toInt()
            }
        }
        return sum
    }
}