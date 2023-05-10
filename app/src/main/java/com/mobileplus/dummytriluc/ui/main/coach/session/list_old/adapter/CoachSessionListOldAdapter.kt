package com.mobileplus.dummytriluc.ui.main.coach.session.list_old.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.core.OnItemClick
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachSessionOld
import com.mobileplus.dummytriluc.ui.main.coach.session.saved_list.adapter.CoachSessionSavedListAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_coach_session_old_list.view.*

class CoachSessionListOldAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemCoachSessionOld>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onDeleteListener: OnDeleteItemListener? = null

    var onItemClick: OnItemClick<ItemCoachSessionOld>? = null

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
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_session_old_list))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            txtTitleCoachSessionOld.text = item.title
            btnDeleteCoachSessionOld.clickWithDebounce {
                onDeleteListener?.setOnDeleteListener(holder.adapterPosition)
            }
            clickWithDebounce { onItemClick?.onItemClickListener(item, holder.adapterPosition) }
            txtCountCoachSessionOld.setTextNotNull(item.getAllPractice())
            txtCountDiscipleCoachSessionOld.setTextNotNull(item.getAllUser())
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnDeleteItemListener {
        fun setOnDeleteListener(index: Int)
    }
}