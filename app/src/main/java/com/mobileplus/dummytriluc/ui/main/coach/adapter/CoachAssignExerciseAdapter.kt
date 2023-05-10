package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.core.OnItemClick
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_practice_filter_multi.view.*
import kotlinx.android.synthetic.main.item_practice_filter_single.view.*

/**
 * Created by KOHuyn on 3/15/2021
 */
class CoachAssignExerciseAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    companion object {
        const val TYPE_SINGLE = 1
        const val TYPE_MULTI = 2
    }

    var onSelectedFolder: OnSelectedFolder? = null

    var onClickItem: OnClickItem? = null

    var items = mutableListOf<ItemCoachPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            ItemCoachPractice.TYPE_PRACTICE -> TYPE_SINGLE
            ItemCoachPractice.TYPE_FOLDER -> TYPE_MULTI
            else -> TYPE_SINGLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        when (viewType) {
            TYPE_SINGLE -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_single))
            TYPE_MULTI -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_multi))
            else -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_single))
        }

    fun getIdsSelected(): List<Int> {
        return items.filter { it.isSelected }.mapNotNull { it.id }
    }

    fun getItemsSelected(): List<ItemCoachPractice> {
        return items.filter { it.isSelected }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        when (getItemViewType(position)) {
            TYPE_SINGLE -> {
                with(holder.itemView) {
                    when (item.id) {
                        1 -> {
                            imgPracticeThumbFilter.show(R.drawable.img_thumb_free_fight)
                        }
                        2 -> {
                            imgPracticeThumbFilter.show(R.drawable.img_thumb_according_to_led)
                        }
                        else -> {
                            imgPracticeThumbFilter.show(item.imagePath)
                        }
                    }
                    txtTitleItemPracticeFilter.text = item.title
                    txtNameSubjectPracticeFilter.setTextNotNull(item.subject?.title)
                    viewSelectedPracticeSingleFilter.setVisibility(item.isSelected)
                    clickWithDebounce {
                        item.isSelected = !item.isSelected
                        onClickItem?.setOnClickItem(item, item.isSelected)
                        notifyItemChanged(position)
                    }
                }
            }
            TYPE_MULTI -> {
                with(holder.itemView) {
                    imgFolderPracticeFilterThumb.show(item.imagePath)
                    imgFolderPracticeFilterThumbClone1.show(item.imagePath)
                    imgFolderPracticeFilterThumbClone2.show(item.imagePath)
                    txtFolderTitleItemPracticeFilter.text = item.title
                    txtNameSubjectPracticeFolderFilter.setTextNotNull(item.subject?.title)
                    viewSelectedPracticeMultiFilter.setVisibility(item.isSelected)
                    clickWithDebounce {
                        onSelectedFolder?.setOnSelectedFolder(item)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnSelectedFolder {
        fun setOnSelectedFolder(item: ItemCoachPractice)
    }

    fun interface OnClickItem {
        fun setOnClickItem(item: ItemCoachPractice, isSelected: Boolean)
    }
}