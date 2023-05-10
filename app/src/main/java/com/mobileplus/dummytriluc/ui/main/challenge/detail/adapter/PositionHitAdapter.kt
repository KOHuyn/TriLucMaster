package com.mobileplus.dummytriluc.ui.main.challenge.detail.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_position_hit.view.*

class PositionHitAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_position_hit))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder.itemView) {
            txtPositionHit.text = items[position]
        }
    }

    override fun getItemCount(): Int = items.size
}