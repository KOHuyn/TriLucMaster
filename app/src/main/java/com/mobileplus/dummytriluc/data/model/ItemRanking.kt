package com.mobileplus.dummytriluc.data.model

import android.widget.TextView
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.utils.ext.setBackgroundColorz
import com.utils.ext.setDrawableStart
import com.utils.ext.setTextColorz

data class ItemRanking(
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("rank")
    val rank: Int? = null,
    @Expose
    @SerializedName("rank_point")
    val rankPoint: Int? = null,
    @Expose
    @SerializedName("rank_up")
    val rankUp: Int? = 0,
    @Expose
    @SerializedName("user")
    val user: UserRanking? = null,
    var isCurrRank: Boolean = false
) {
    fun setRankingUp(txt: TextView) {
        when {
            rankUp == 0 -> {
                txt.run {
                    text = " "
                    setDrawableStart(R.drawable.ic_rank_normal)
                }
            }
            rankUp?:0 > 0 -> {
                txt.run {
                    text = "+$rankUp"
                    setDrawableStart(R.drawable.ic_rank_up)
                    setTextColorz(R.color.clr_green)
                }
            }
            rankUp?:0 < 0 -> {
                txt.run {
                    text = "$rankUp"
                    setDrawableStart(R.drawable.ic_rank_down)
                    setTextColorz(R.color.clr_red)
                }
            }
        }
    }

    fun setRankTop(txt: TextView) {
        if (rank == 1) {
            txt.text = ""
        } else {
            txt.text = String.format("#%d", rank)
        }
        if (isCurrRank) {
            if (rank == 1) {
                txt.setBackgroundResource(R.drawable.background_top1_ranking)
            } else {
                txt.setBackgroundColorz(R.color.clr_blue)
            }
        } else {
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
    }
}

data class UserRanking(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null
)