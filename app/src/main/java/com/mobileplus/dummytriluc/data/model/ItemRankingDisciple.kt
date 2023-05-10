package com.mobileplus.dummytriluc.data.model

import android.widget.TextView
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.utils.ext.setDrawableStart
import com.utils.ext.setTextColorz


/**
 * Created by KO Huyn on 1/18/2021.
 */
data class ItemRankingDisciple(
    @Expose
    @SerializedName("user_id")
    val userId: Int? = 0,
    @Expose
    @SerializedName("rank")
    val rank: Int? = null,
    @Expose
    @SerializedName("rank_point")
    val rankPoint: Int? = null,
    @Expose
    @SerializedName("rank_up")
    val rankUp: Int? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null
) {
    fun setRankingUp(txt: TextView) {
        when {
            rankUp == 0 -> {
                txt.run {
                    text = " "
                    setDrawableStart(R.drawable.ic_rank_normal)
                }
            }
            rankUp ?: 0 > 0 -> {
                txt.run {
                    text = "+$rankUp"
                    setDrawableStart(R.drawable.ic_rank_up)
                    setTextColorz(R.color.clr_green)
                }
            }
            rankUp ?: 0 < 0 -> {
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