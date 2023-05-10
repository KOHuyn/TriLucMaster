package com.mobileplus.dummytriluc.ui.main.coach.group.detail.adapter

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_disciple.view.*

class CoachGroupMemberAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items: MutableList<ItemDisciple> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: DetailGroupListener? = null

    var isEnableMenu: Boolean = false
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent.inflateExt(R.layout.item_disciple))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgMenuDisciple.setVisibility(isEnableMenu)
            imgAvatarDisciple.show(item.avatarPath)
            txtNameDisciple.setTextNotNull(item.fullName)
            txtLocationDisciple.setTextNotNull(item.getAllGroupName())
            txtNumberPunchDisciple.run {
                text = "${item.hitTotal ?: 0}"
                fillGradientPrimary()
            }
            txtTotalPowerPunchDisciple.run {
                text = "${item.powerPoint ?: 0}"
                fillGradientPrimary()
            }
            txtTimePracticeDisciple.run {
                text = item.getTimePractice()
                fillGradientPrimary()
            }
            imgMenuDisciple.clickWithDebounce {
                val contextThemeWrapper: Context =
                    ContextThemeWrapper(context, R.style.CustomPopupTheme)
                val popup = PopupMenu(contextThemeWrapper, this)
                popup.gravity = Gravity.END
                popup.menuInflater.inflate(R.menu.detail_group_menu, popup.menu)
                popup.setOnMenuItemClickListener { items ->
                    when (items.itemId) {
                        R.id.itemMove -> {
                            listener?.onMoveClick(position, item)
                        }
                        R.id.itemAssignExerciseMemberInGroup -> {
                            listener?.onAssignExercise(position, item)
                        }
                        R.id.itemRemove -> {
                            listener?.onRemoveClick(position, item)
                        }
                    }
                    true
                }
                popup.show()
            }
        }
    }

    interface DetailGroupListener {
        fun onMoveClick(position: Int, item: ItemDisciple)
        fun onAssignExercise(position: Int, item: ItemDisciple)
        fun onRemoveClick(position: Int, item: ItemDisciple)
    }
}