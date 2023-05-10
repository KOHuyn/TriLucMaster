package com.mobileplus.dummytriluc.ui.main.challenge.detail.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.inflateExt
import com.utils.ext.show
import kotlinx.android.synthetic.main.item_new_join_challenge.view.*


/**
 * Created by KO Huyn on 12/23/2020.
 */
class NewJoinAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<String?>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_new_join_challenge))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder.itemView) {
            imageNewJoinChallenge.show(items[position])
        }
    }

    override fun getItemCount(): Int = items.size
}