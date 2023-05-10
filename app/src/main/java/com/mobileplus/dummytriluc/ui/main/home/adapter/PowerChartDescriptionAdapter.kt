package com.mobileplus.dummytriluc.ui.main.home.adapter

import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.utils.ext.applyColorFilter
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_description_chart_power.view.*

class PowerChartDescriptionAdapter(private val isShowDot: Boolean = true) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemDescriptionChartPower>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onClickItem: OnClickItemAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_description_chart_power))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            colorItemChartPower.setVisibility(isShowDot)
            colorItemChartPower.background.applyColorFilter(item.color)
            txtTitleItemChartPower.text = item.title
            txtScoreItemChartPower.text = item.score.toString()
            clickWithDebounce { onClickItem?.setOnClickListener(this, position) }
        }
    }

    override fun getItemCount(): Int = items.size

    data class ItemDescriptionChartPower(
        @ColorInt val color: Int,
        val score: Int,
        val title: String,
        val key: String? = null,
    )
}