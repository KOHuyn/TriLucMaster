package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_choose_group.view.*

class ModifyDiscipleGroupAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items: MutableList<ItemDiscipleGroup> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent.inflateExt(R.layout.item_choose_group))
    }

    override fun getItemCount(): Int = items.size

    fun checkGroupAdded(idsGroup: List<Int?>) {
        if (idsGroup.isEmpty()) return
        items.forEach { item ->
            if (idsGroup.contains(item.id)) item.isChecked = true
        }
        notifyDataSetChanged()
    }

    fun getArrGroupID(): List<Int> {
        val arrID = arrayListOf<Int>()
        items.forEach {
            if (it.isChecked && it.id != null) arrID.add(it.id!!)
        }
        return arrID
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            txtGroupNameDisciple.text = item.name
            cbAddGroupDisciple.isChecked = item.isChecked
            cbAddGroupDisciple.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
            }
        }
    }
}