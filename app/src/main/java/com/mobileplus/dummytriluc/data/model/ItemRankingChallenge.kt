package com.mobileplus.dummytriluc.data.model

import android.widget.TextView
import com.mobileplus.dummytriluc.R
import com.utils.ext.setBackgroundColorz
import com.utils.ext.setDrawableStart
import com.utils.ext.setTextColorz


/**
 * Created by KO Huyn on 12/23/2020.
 */
class ItemRankingChallenge(
    val rank: Int,
    val rankUp: Int,
    val point: Int?,
    val pointBonus: Int? = 0,
    val fullName: String,
    val avatarPath: String?,
    val isCurrRank: Boolean = false
) {

    fun setTextPointBonus() = if (pointBonus != null) "+$pointBonus" else ""
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

    fun setRankingUp(txt: TextView) {
        when {
            rankUp == 0 -> {
                txt.run {
                    text = " "
                    setDrawableStart(R.drawable.ic_rank_normal)
                }
            }
            rankUp > 0 -> {
                txt.run {
                    text = "+$rankUp"
                    setDrawableStart(R.drawable.ic_rank_up)
                    setTextColorz(R.color.clr_green)
                }
            }
            rankUp < 0 -> {
                txt.run {
                    text = "$rankUp"
                    setDrawableStart(R.drawable.ic_rank_down)
                    setTextColorz(R.color.clr_red)
                }
            }
        }
    }
}