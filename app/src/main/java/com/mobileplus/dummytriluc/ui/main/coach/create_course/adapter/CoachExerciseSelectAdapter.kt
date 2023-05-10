package com.mobileplus.dummytriluc.ui.main.coach.create_course.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_exercise_select.view.*

/**
 * Created by KOHuyn on 4/5/2021
 */
class CoachExerciseSelectAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemCoachPractice>()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_exercise_select))

    fun getItemsSelected() = items.filter { it.isSelected }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgAvatarAddExercise.show(item.imagePath)
            txtNameAddExercise.text = item.title ?: "unknown"
            cbAddExercise.isChecked = item.isSelected
            cbAddExercise.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyItemChanged(position)
            }
            viewTopAddExercise.setVisibility(position != 0)
        }
    }

    override fun getItemCount(): Int = items.size
}