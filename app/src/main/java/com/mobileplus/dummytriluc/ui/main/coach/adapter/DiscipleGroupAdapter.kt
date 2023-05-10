package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_big_disciple_group.view.*

class DiscipleGroupAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    var listener: GroupAdapterListener? = null

    var items: MutableList<ItemDiscipleGroup> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isEnableMenu: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent.inflateExt(R.layout.item_big_disciple_group))
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

    fun updateNewName(newName: String, position: Int) {
        items[position].name = newName
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgMenuDisciple.setVisibility(isEnableMenu)
            txtGroupNameDisciple.setTextNotNull(item.name)
            txtDiscipleCountDisciple.setTextNotNull(
                if (item.discipleCount == null) null else String.format(
                    loadStringRes(R.string.format_disciple_count),
                    item.discipleCount
                )
            )
            imgMenuDisciple.clickWithDebounce {
                val contextThemeWrapper: Context =
                    ContextThemeWrapper(context, R.style.CustomPopupTheme)
                val popup = PopupMenu(contextThemeWrapper, this)
                popup.menuInflater.inflate(R.menu.big_disciple_group_menu, popup.menu)
                popup.gravity = Gravity.END
                popup.setOnMenuItemClickListener { items ->
                    when (items.itemId) {
                        R.id.itemChangeName -> {
                            listener?.onChangeNameClick(position, item)
                        }
                        R.id.itemDeleteGroup -> {
                            listener?.onDeleteGroupClick(position, item)
                        }
                        R.id.itemAddMember -> {
                            listener?.onAddMemberToGroup(position, item)
                        }
                        R.id.itemAssignExercise -> {
                            listener?.onAssignExercise(position, item)
                        }
                    }
                    true
                }
                popup.show()
            }
            clickWithDebounce {
                listener?.onDetailClick(position, item)
            }
        }

    }

    fun addAll(data: java.util.ArrayList<ItemDiscipleGroup>?) {
        data?.let {
            val position = items.size
            items.addAll(it)
            notifyItemRangeInserted(position, data.size)
        }
    }

    interface GroupAdapterListener {
        fun onDetailClick(position: Int, itemDiscipleGroup: ItemDiscipleGroup)
        fun onChangeNameClick(position: Int, itemDiscipleGroup: ItemDiscipleGroup)
        fun onDeleteGroupClick(position: Int, itemDiscipleGroup: ItemDiscipleGroup)
        fun onAddMemberToGroup(position: Int, itemDiscipleGroup: ItemDiscipleGroup)
        fun onAssignExercise(position: Int, itemDiscipleGroup: ItemDiscipleGroup)
    }
}