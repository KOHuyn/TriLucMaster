package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_add_disciple.view.*

/**
 * Created by KOHuyn on 3/15/2021
 */
class AddDiscipleAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemDisciple>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var itemChangeListener: OnItemChangeListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_add_disciple))

    fun getIdsSelected(): List<Int> {
        return items.filter { it.isSelected }.mapNotNull { it.studentId }
    }

    fun getItemsSelected(): MutableList<ItemDisciple> {
        return items.filter { it.isSelected }.toMutableList()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgAvatarAddDisciple.show(item.avatarPath)
            txtNameAddDisciple.text = item.fullName ?: "unknown"
            cbAddDisciple.isChecked = item.isSelected
            cbAddDisciple.setOnClickListener {
                item.isSelected = !item.isSelected
                itemChangeListener?.setOnItemChangeListener(item, item.isSelected)
                notifyItemChanged(position)
            }
            setOnClickListener {
                item.isSelected = !item.isSelected
                itemChangeListener?.setOnItemChangeListener(item, item.isSelected)
                notifyItemChanged(position)
            }
            viewTopAddDisciple.setVisibility(position != 0)
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnItemChangeListener {
        fun setOnItemChangeListener(item: ItemDisciple, isSelected: Boolean)
    }
}