package com.mobileplus.dummytriluc.ui.main.coach.group.detail.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.BaseItemCoachGroup
import com.mobileplus.dummytriluc.data.model.ItemCoachGroupLesson
import com.mobileplus.dummytriluc.data.model.ItemCoachGroupTime
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.setVisibleViewWhen
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.*
import kotlinx.android.synthetic.main.item_coach_group_lesson.view.*
import kotlinx.android.synthetic.main.item_coach_group_time.view.*

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachGroupLessonAdapter :
    RecyclerView.Adapter<BaseViewHolder>() {

    var clickListener: OnClickLessonListener? = null

    var items = mutableListOf<BaseItemCoachGroup>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

//    var isDelete: Boolean = true
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }

    override fun getItemViewType(position: Int): Int {
        return items[position].getType()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        if (viewType == BaseItemCoachGroup.TYPE_TIME) BaseViewHolder(parent.inflateExt(R.layout.item_coach_group_time)) else
            BaseViewHolder(parent.inflateExt(R.layout.item_coach_group_lesson))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (getItemViewType(position) == BaseItemCoachGroup.TYPE_TIME) {
            with(holder.itemView) {
                txtTimeCoachGroup.text = (items[position] as ItemCoachGroupTime).time
            }
        } else {
            val item = items[position] as ItemCoachGroupLesson
            with(holder.itemView) {
                when (getItemViewType(position)) {
                    BaseItemCoachGroup.TYPE_SESSION -> {
                        txtCategoryCoachGroup.text = loadStringRes(R.string.session)
                        txtCategoryCoachGroup.setBackgroundColorz(R.color.clr_green_news)
                    }
                    BaseItemCoachGroup.TYPE_PRACTICE -> {
                        txtCategoryCoachGroup.text = loadStringRes(R.string.exercise)
                        txtCategoryCoachGroup.setBackgroundColorz(R.color.clr_primary)
                    }
                    BaseItemCoachGroup.TYPE_FOLDER -> {
                        txtCategoryCoachGroup.text = loadStringRes(R.string.folder)
                        txtCategoryCoachGroup.setBackgroundColorz(R.color.clr_primary)
                    }
                }

                setBackgroundColor(Color.parseColor(if (getItemViewType(position) == BaseItemCoachGroup.TYPE_SESSION) "#000824" else "#0F1838"))
                setVisibleViewWhen(
                    txtCountExerciseCoachGroupLesson,
                    txtDiscipleCoachGroupLesson
                ) { getItemViewType(position) == BaseItemCoachGroup.TYPE_SESSION }
//                btnDeleteCoachGroupLesson.setVisibility(isDelete)
//                btnDeleteCoachGroupLesson.clickWithDebounce {
//                    clickListener?.setOnDeleteLessonListener(
//                        position,
//                        item
//                    )
//                }
                imgCoachGroupLesson.show(item.imagePath)
                txtTitleCoachGroupLesson.setTextNotNull(item.title)
                txtDescriptionCoachGroupLesson.setTextNotNull(item.content)
                txtDateCoachGroupLesson.setTextNotNull(item.getHHmmCreate())
                txtCountExerciseCoachGroupLesson.text = item.getExerciseCount()
                txtDiscipleCoachGroupLesson.text = item.getUserCountt()
                clickWithDebounce {
                    clickListener?.setOnClickLessonListener(
                        position, item
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    interface OnClickLessonListener {
        fun setOnClickLessonListener(position: Int, item: ItemCoachGroupLesson)
        fun setOnDeleteLessonListener(position: Int, item: ItemCoachGroupLesson)
    }
}