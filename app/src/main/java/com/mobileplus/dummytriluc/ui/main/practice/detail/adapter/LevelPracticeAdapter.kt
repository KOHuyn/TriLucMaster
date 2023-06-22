package com.mobileplus.dummytriluc.ui.main.practice.detail.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.*
import kotlinx.android.synthetic.main.item_level_practice.view.*

class LevelPracticeAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    private var itemsTemp: MutableList<Int> = mutableListOf()

    var items = mutableListOf<Int>()
        set(value) {
            field = value
            value.firstOrNull()?.let {
                roundSelected = it
                itemsTemp = mutableListOf(it)
            }
            isShowAllItems = false
        }

    private var roundSelected: Int = -1

    private var isShowAllItems = false
        set(value) {
            field = value
            try {
                if (items.isEmpty()) return
                if (field) {
                    itemsTemp.clear()
                    itemsTemp.addAll(items)
                } else {
                    val itemChoice = if (itemsTemp.isEmpty()) {
                        items[lastPosShowItems]
                    } else {
                        itemsTemp[lastPosShowItems]
                    }
                    itemsTemp.clear()
                    itemsTemp.add(itemChoice)
                }
                notifyDataSetChanged()
            } catch (e: Exception) {
                e.logErr()
            }
        }

    fun getLevelCurrent(): Int? =
        if (items.isEmpty()) null else {
            if (itemsTemp.size == 1) {
                itemsTemp[0]
            } else {
                roundSelected
            }
        }

    private var lastPosShowItems = 0

    private fun clickLevel(position: Int) {
        roundSelected = itemsTemp[position]
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_level_practice))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder.itemView) {
            if (itemsTemp[position] == roundSelected) {
                txtNameLevelPractice.setTextColorz(R.color.clr_primary)
            } else {
                txtNameLevelPractice.setTextColorz(R.color.clr_tab)
            }
            viewLevel.setVisibility(position != 0)
            val round = "${itemsTemp[position]} ${holder.itemView.context.getString(R.string.round).toLowerCase()}"
            txtNameLevelPractice.text = round
            clickWithDebounce {
                clickLevel(position)
                lastPosShowItems = position
                isShowAllItems = !isShowAllItems
            }
        }
    }

    override fun getItemCount(): Int = itemsTemp.size
}