package com.mobileplus.dummytriluc.ui.main.coach.session.saved_list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachSessionSaved
import com.mobileplus.dummytriluc.databinding.ItemCoachSessionSavedListBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility

/**
 * Created by KOHuyn on 4/27/2021
 */
class CoachSessionSavedListAdapter :
    RecyclerView.Adapter<BaseViewHolderZ<ItemCoachSessionSavedListBinding>>() {

    var items = mutableListOf<ItemCoachSessionSaved>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isDelete: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isClickableItem: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onDeleteListener: OnDeleteItemListener? = null

    var onClickItem: OnClickItemAdapter? = null

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
    ): BaseViewHolderZ<ItemCoachSessionSavedListBinding> =
        BaseViewHolderZ(
            ItemCoachSessionSavedListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: BaseViewHolderZ<ItemCoachSessionSavedListBinding>,
        position: Int
    ) {
        val item = items[position]
        with(holder) {
            binding.txtTitleCoachSessionSaved.text = item.title ?: "unknown"
            val countPractice =
                String.format(
                    itemView.context.getString(R.string.value_exercise),
                    item.practices?.size ?: 0
                )
            binding.txtCountCoachSessionSaved.text = countPractice
            binding.btnDeleteCoachSessionSaved.setVisibility(isDelete)
            binding.btnDeleteCoachSessionSaved.clickWithDebounce {
                onDeleteListener?.setOnDeleteListener(holder.adapterPosition)
            }
            binding.root.clickWithDebounce {
                if (isClickableItem) {
                    onClickItem?.setOnClickListener(
                        itemView,
                        holder.adapterPosition
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnDeleteItemListener {
        fun setOnDeleteListener(index: Int)
    }
}