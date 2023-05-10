package com.mobileplus.dummytriluc.ui.main.coach.session.exercise.adapter

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.ui.main.coach.session.callback.OnValueChangeListener
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.widget.ItemMoveCallback
import com.mobileplus.dummytriluc.ui.widget.StartDragListener
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setBackgroundColorz
import com.utils.ext.setVisibility
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_coach_session_exercise.view.*
import java.util.*


/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionExerciseAdapter : RecyclerView.Adapter<BaseViewHolder>(),
    ItemMoveCallback.ItemTouchHelperContract {

    var isEnableChangeData: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var startDragListener: StartDragListener? = null

    var items = mutableListOf<ItemCoachPractice>()
        set(value) {
            field = value
            onValueChangeListener.onNext(field.isNotEmpty())
            notifyDataSetChanged()
        }

    var onValueChangeListener: PublishSubject<Boolean> = PublishSubject.create()

    fun getAllIds(): List<Int> {
        return items.mapNotNull { it.id }
    }

    fun getIdsRound(): List<Pair<Int?, Int?>> {
        return items.map { Pair(it.id, it.countRepeat) }
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

    var onDeleteChange: OnDeleteChange? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_session_exercise))

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            when (item.id) {
                1 -> {
                    imgCoachSessionExercise.show(R.drawable.img_thumb_free_fight)
                }
                2 -> {
                    imgCoachSessionExercise.show(R.drawable.img_thumb_according_to_led)
                }
                else -> {
                    imgCoachSessionExercise.show(item.imagePath)
                }
            }
            txtTitleCoachSessionExercise.text = item.title
            txtCountCoachSession.text = "x${item.countRepeat}"
            viewMinusCoachSession.setVisibility(isEnableChangeData)
            viewPlusCoachSession.setVisibility(isEnableChangeData)
            btnDeleteCoachSessionExercise.setVisibility(isEnableChangeData)
            btnDragCoachSessionExercise.setVisibility(isEnableChangeData)
            viewMinusCoachSession.setOnClickListener {
                if (isEnableChangeData) {
                    if (item.countRepeat <= 1) item.countRepeat = 1 else item.countRepeat--
                    txtCountCoachSession.text = "x${item.countRepeat}"
                }
            }
            viewPlusCoachSession.setOnClickListener {
                if (isEnableChangeData) {
                    item.countRepeat++
                    txtCountCoachSession.text = "x${item.countRepeat}"
                }
            }
            btnDeleteCoachSessionExercise.clickWithDebounce {
                if (isEnableChangeData) {
                    delete(holder.adapterPosition)
                    onDeleteChange?.setOnDeleteChange()
                    onValueChangeListener.onNext(items.isNotEmpty())
                }
            }
            btnDragCoachSessionExercise.setOnTouchListener { v, event ->
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
        viewHolder.itemView.setBackgroundColorz(R.color.clr_bg_title_dark)
    }

    override fun onRowClear(viewHolder: BaseViewHolder) {
        viewHolder.itemView.setBackgroundColorz(R.color.clr_tab)
    }

    fun interface OnDeleteChange {
        fun setOnDeleteChange()
    }
}