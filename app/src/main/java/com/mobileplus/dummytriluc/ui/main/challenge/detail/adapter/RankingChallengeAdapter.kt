package com.mobileplus.dummytriluc.ui.main.challenge.detail.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemRankingChallenge
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.inflateExt
import com.utils.ext.setBackgroundColorz
import kotlinx.android.synthetic.main.item_ranking_challenge.view.*

class RankingChallengeAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemRankingChallenge>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_ranking_challenge))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            holder.itemView.setBackgroundColorz(if (item.isCurrRank) R.color.clr_blue_light else R.color.white)
            item.setRankTop(txtRankingTopChallenge)
            imageAvatarUserChallenge.show(item.avatarPath)
            txtNameUserChallenge.text = item.fullName
            item.setRankingUp(txtScoreUserChallenge)
            txtPointBonusChallenge.text = item.setTextPointBonus()
            txtPointChallenge.fillGradientPrimary()
            txtPointChallenge.setTextNotNull(item.point?.toString())
        }
    }

    override fun getItemCount(): Int = items.size
}