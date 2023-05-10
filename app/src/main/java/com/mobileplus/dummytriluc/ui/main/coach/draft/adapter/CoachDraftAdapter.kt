package com.mobileplus.dummytriluc.ui.main.coach.draft.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_coach_draft_single.view.*

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachDraftAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var clickItem: OnClickCoachDraft? = null

    var longClickItem: OnLongClickCoachDraft? = null

    var onDeleteItem: OnDeleteCoach? = null

    var items = mutableListOf<ItemCoachPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isDeleteItem: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isClickableItem: Boolean = true
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

    fun updateName(position: Int, name: String) {
        items[position].title = name
        notifyItemChanged(position)
    }

    fun removeFolder(position: Int, folderId: Int) {
        if (items[position].folderId != folderId) {
            delete(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_draft_single))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            btnDeleteCoachDraft.setVisibility(isDeleteItem)
            imgCoachDraftThumb.show(item.imagePath)
            txtTitleCoachDraft.text = item.title
            btnDeleteCoachDraft.clickWithDebounce {
                onDeleteItem?.setOnDeleteCoach(it, holder.adapterPosition)
            }
            setOnLongClickListener {
                longClickItem?.setOnLongClickDraft(item, holder.adapterPosition)
                true
            }
            clickWithDebounce {
                if (isClickableItem) {
                    clickItem?.setOnClickDraft(item)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnDeleteCoach {
        fun setOnDeleteCoach(view: View, position: Int)
    }

    fun interface OnClickCoachDraft {
        fun setOnClickDraft(item: ItemCoachPractice)
    }

    fun interface OnLongClickCoachDraft {
        fun setOnLongClickDraft(item: ItemCoachPractice, position: Int)
    }
}