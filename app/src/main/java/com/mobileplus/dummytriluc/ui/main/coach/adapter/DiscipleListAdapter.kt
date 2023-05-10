package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.databinding.ItemDiscipleBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_disciple.view.*


class DiscipleListAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemDiscipleBinding>>() {
    var listener: DiscipleItemClickListener? = null

    var items: MutableList<ItemDisciple> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun delete(position: Int) {
        try {
            if (position != RecyclerView.NO_POSITION && items.size > position) {
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemDiscipleBinding> =
        BaseViewHolderZ(
            ItemDiscipleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolderZ<ItemDiscipleBinding>, position: Int) {
        val item = items[position]
        with(holder) {
            binding.imgAvatarDisciple.show(item.avatarPath)
            binding.txtNameDisciple.setTextNotNull(item.fullName)
            binding.txtLocationDisciple.setTextNotNull(item.getAllGroupName())
            binding.txtNumberPunchDisciple.run {
                text = "${item.hitTotal ?: 0}"
                fillGradientPrimary()
            }
            binding.txtTotalPowerPunchDisciple.run {
                text = "${item.powerPoint ?: 0}"
                fillGradientPrimary()
            }
            binding.txtTimePracticeDisciple.run {
                text = item.getTimePractice()
                fillGradientPrimary()
            }
            binding.imgMenuDisciple.clickWithDebounce {
                val contextThemeWrapper: Context =
                    ContextThemeWrapper(itemView.context, R.style.CustomPopupTheme)
                val popup = PopupMenu(contextThemeWrapper, it)
                popup.menuInflater.inflate(R.menu.disciple_item_menu, popup.menu)
                popup.setOnMenuItemClickListener { items ->
                    when (items.itemId) {
                        R.id.itemAddGroup -> {
                            listener?.onAddGroupClick(position, item)
                        }
                        R.id.itemAssignExerciseDisciple -> {
                            listener?.onAssignExercise(position, item)
                        }
                        R.id.itemDeleteDisciple -> {
                            listener?.onDeleteDiscipleClick(position, item)
                        }
                    }
                    true
                }
                popup.show()
            }
        }
    }

    fun addItem(data: ArrayList<ItemDisciple>) {
        val start = items.size
        items.addAll(data)
        notifyItemRangeInserted(start, data.size)
    }

    interface DiscipleItemClickListener {
        fun onAddGroupClick(position: Int, item: ItemDisciple)
        fun onAssignExercise(position: Int, item: ItemDisciple)
        fun onDeleteDiscipleClick(position: Int, item: ItemDisciple)
    }

}