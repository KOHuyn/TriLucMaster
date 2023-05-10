package com.mobileplus.dummytriluc.ui.main.ranking.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemRanking
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.inflateExt
import com.utils.ext.setBackgroundColorz
import kotlinx.android.synthetic.main.item_ranking.view.*

class RankingAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemRanking>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_ranking))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            layoutBodyRanking.setBackgroundColorz(if (item.isCurrRank) R.color.clr_blue_light else R.color.white)
            item.setRankTop(txtItemRankNumber)
            item.setRankingUp(txtItemRankingDifference)
            txtItemScorePersonRank.setTextNotNull(item.rankPoint.toString())
            txtItemNamePersonRank.setTextNotNull(item.user?.fullName)
            imgItemAvatarPersonRank.show(item.user?.avatarPath)
        }
    }

    override fun getItemCount(): Int = items.size
}