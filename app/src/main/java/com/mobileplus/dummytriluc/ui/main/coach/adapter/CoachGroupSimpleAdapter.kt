package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_coach_group_select_simple.view.*

/**
 * Created by KOHuyn on 5/20/2021
 */
class CoachGroupSimpleAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemDiscipleGroup>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun selectItem(position: Int) {
        if (items[position].isChecked) {
            items[position].isChecked = false
            notifyItemChanged(position)
            return
        }
        items.map { it.isChecked = it.id == items[position].id }
        notifyDataSetChanged()
    }

    fun getItemSelected(): ItemDiscipleGroup? = items.firstOrNull { it.isChecked }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_group_select_simple))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            txtNameGroupCoach.text = item.name ?: "unknown"
            cbGroupSelected.isChecked = item.isChecked
            cbGroupSelected.setOnClickListener {
                selectItem(position)
            }
            setOnClickListener {
                selectItem(position)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}