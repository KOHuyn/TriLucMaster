package com.mobileplus.dummytriluc.ui.main.coach.create_course.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setBackgroundColorz
import kotlinx.android.synthetic.main.item_coach_draft_single.view.*

/**
 * Created by KOHuyn on 4/5/2021
 */
class CoachCreateCourseAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemCoachPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun delete(position: Int) {
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
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_draft_single))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgCoachDraftThumb.show(item.imagePath)
            txtTitleCoachDraft.setTextNotNull(item.title)
            txtTitleCoachDraft.setBackgroundColorz(R.color.clr_tab)
            btnDeleteCoachDraft.clickWithDebounce { delete(holder.adapterPosition) }
        }
    }

    override fun getItemCount(): Int = items.size
}