package com.mobileplus.dummytriluc.ui.main.challenge.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemAppellation
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_my_appellation.view.*

class AppellationAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemAppellation>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(
            parent.inflateExt(R.layout.item_my_appellation)
        )

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgMyAppellation.show(item.icon)
            txtNameMyAppellation.text = item.title
            clickWithDebounce { onItemClick?.setOnClickListener(this, position) }
        }
    }

    override fun getItemCount(): Int = items.size

}