package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemRankingDisciple
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_disciple_ranking.view.*


/**
 * Created by KO Huyn on 1/18/2021.
 */
class RankingDiscipleAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemRankingDisciple>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_disciple_ranking))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            item.setRankTop(txtItemRankNumberDisciple)
            item.setRankingUp(txtItemRankingDifferenceDisciple)
            txtItemScorePersonRankDisciple.setTextNotNull(item.rankPoint.toString())
            txtItemNamePersonRankDisciple.setTextNotNull(item.fullName)
            imgItemAvatarPersonRankDisciple.show(item.avatarPath)
        }
    }

    override fun getItemCount(): Int = items.size
}