package com.mobileplus.dummytriluc.ui.main.coach.session.practitioner.adapter

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.core.OnItemClick
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.ui.main.coach.session.callback.OnValueChangeListener
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.widget.ItemMoveCallback
import com.mobileplus.dummytriluc.ui.widget.StartDragListener
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_coach_session_practitioner.view.*
import java.util.*

/**
 * Created by KOHuyn on 4/22/2021
 */
class CoachSessionPractitionerAdapter : RecyclerView.Adapter<BaseViewHolder>(),
    ItemMoveCallback.ItemTouchHelperContract {

    var isEnableChangeData: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var isClickableItem: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var items = mutableListOf<ItemDisciple>()
        set(value) {
            field = value
            onValueChangeListener?.setOnValueChangeListener(field.isNotEmpty())
            notifyDataSetChanged()
        }

    var startDragListener: StartDragListener? = null

    var onDeleteChange: OnDeleteChange? = null

    var onValueChangeListener: OnValueChangeListener? = null

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

    fun getAllIds(): List<Int> {
        return items.mapNotNull { it.studentId }
    }

    fun findUserById(id: Int?): ItemDisciple? {
        if (id == null) return null
        return items.find { it.studentId == id }
    }

    private fun findPosPlayerCurrent(): Int? {
        val itemPlayer = items.find { it.isChoosePractice }
        return if (itemPlayer != null) items.indexOf(itemPlayer) else null
    }

    fun nextPlayer(action: (ItemDisciple?) -> Unit) {
        if (items.isEmpty()) {
            action(null)
            return
        }
        var currPlayerPos = findPosPlayerCurrent()
        if (currPlayerPos != null) {
            if (currPlayerPos < items.size - 1) {
                currPlayerPos++
                selectedItemPosition(currPlayerPos)
            } else {
                currPlayerPos = 0
                selectedItemPosition(currPlayerPos)
            }
        }
        action(items[currPlayerPos!!])
    }

    fun prevPlayer(action: (ItemDisciple?) -> Unit) {
        if (items.isEmpty()) {
            action(null)
            return
        }
        var currPlayerPos = findPosPlayerCurrent()
        if (currPlayerPos != null) {
            if (currPlayerPos > 0) {
                currPlayerPos--
                selectedItemPosition(currPlayerPos)
            } else {
                currPlayerPos = 0
                selectedItemPosition(currPlayerPos)
            }
        }
        action(items[currPlayerPos!!])
    }

    fun selectedItemPosition(pos: Int) {
        if (items.isEmpty()) return
        items.map { item -> item.isChoosePractice = item.studentId == items[pos].studentId }
        notifyDataSetChanged()
    }

    var onClickItem: OnItemClick<ItemDisciple>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_session_practitioner))

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
//            background = ResourcesCompat.getDrawable(
//                resources,
//                if (item.isChoosePractice) R.drawable.bg_selected_current_session_practitioner else R.color.clr_tab,
//                null
//            )

            imgAvatarCoachSessionPractitioner.show(item.avatarPath)
            txtNameCoachSessionPractitioner.text = item.fullName
            btnDeleteCoachSessionPractitioner.setVisibility(isEnableChangeData)
            btnDragCoachSessionPractitioner.setVisibility(isEnableChangeData)
            btnDeleteCoachSessionPractitioner.clickWithDebounce {
                if (isEnableChangeData) {
                    delete(holder.adapterPosition)
                    onDeleteChange?.setOnDeleteChange()
                    onValueChangeListener?.setOnValueChangeListener(items.isNotEmpty())
                }
            }
            clickWithDebounce {
                if (!isEnableChangeData && isClickableItem) {
                    onClickItem?.onItemClickListener(item, holder.adapterPosition)
                }
//                selectedItemPosition(holder.adapterPosition)
            }
            btnDragCoachSessionPractitioner.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN && isEnableChangeData) {
                    startDragListener?.requestDrag(holder)
                }
                false
            }
            setOnLongClickListener {
                if (isEnableChangeData) {
                    startDragListener?.requestDrag(holder)
                    true
                } else false
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(viewHolder: BaseViewHolder) {
        viewHolder.itemView.background = ResourcesCompat.getDrawable(
            viewHolder.itemView.resources,
            if (items[viewHolder.adapterPosition].isChoosePractice) R.drawable.bg_drag_current_session_practitioner
            else R.color.clr_bg_title_dark,
            null
        )
    }

    override fun onRowClear(viewHolder: BaseViewHolder) {
        viewHolder.itemView.background = ResourcesCompat.getDrawable(
            viewHolder.itemView.resources,
            if (items[viewHolder.adapterPosition].isChoosePractice) R.drawable.bg_selected_current_session_practitioner
            else R.color.clr_tab,
            null
        )
    }

    fun interface OnDeleteChange {
        fun setOnDeleteChange()
    }
}