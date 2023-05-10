package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_practice_folder.view.*
import kotlinx.android.synthetic.main.item_practice_item_content.view.*

class CoachAdapter :
    RecyclerView.Adapter<BaseViewHolder>() {

    companion object {
        const val TYPE_SINGLE = 1
        const val TYPE_MULTI = 2
    }

    var items = mutableListOf<ItemCoachPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onClickItem: OnItemClick? = null

    var onLongClickItem: OnLongClickPractice? = null

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

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            ItemCoachPractice.TYPE_PRACTICE -> CoachAssignExerciseAdapter.TYPE_SINGLE
            ItemCoachPractice.TYPE_FOLDER -> CoachAssignExerciseAdapter.TYPE_MULTI
            else -> CoachAssignExerciseAdapter.TYPE_SINGLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        when (viewType) {
            TYPE_SINGLE -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_item_content))
            TYPE_MULTI -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_folder))
            else -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_item_content))
        }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        when (val typeView = getItemViewType(position)) {
            TYPE_SINGLE -> {
                with(holder.itemView) {
                    imgPracticeThumb.show(item.imagePath)
                    txtTitleItemPractice.text = item.title
                    txtNameSubjectPractice.setTextNotNull(item.subject?.title)
                    clickWithDebounce { onClickItem?.setOnItemClick(item, typeView) }
                    setOnLongClickListener {
                        onLongClickItem?.setOnClickPractice(
                            item,
                            TYPE_SINGLE,
                            holder.adapterPosition
                        )
                        true
                    }
                }
            }
            TYPE_MULTI -> {
                with(holder.itemView) {
                    imgFolderPracticeThumb.show(item.imagePath)
                    imgFolderPracticeThumbClone1.show(item.imagePath)
                    imgFolderPracticeThumbClone2.show(item.imagePath)
                    txtNameSubjectPracticeFolder.setTextNotNull(item.subject?.title)
                    txtFolderTitleItemPractice.text = item.title
                    clickWithDebounce { onClickItem?.setOnItemClick(item, typeView) }
                    setOnLongClickListener {
                        onLongClickItem?.setOnClickPractice(
                            item,
                            TYPE_MULTI,
                            holder.adapterPosition
                        )
                        true
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnItemClick {
        fun setOnItemClick(item: ItemCoachPractice, type: Int)
    }

    fun interface OnLongClickPractice {
        fun setOnClickPractice(item: ItemCoachPractice, type: Int, pos: Int)
    }
}