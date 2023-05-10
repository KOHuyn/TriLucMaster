package com.mobileplus.dummytriluc.ui.main.practice.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemContent
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_practice_filter_multi.view.*
import kotlinx.android.synthetic.main.item_practice_filter_single.view.*


class PracticeMoreAdapter :
    RecyclerView.Adapter<BaseViewHolder>() {

    companion object {
        const val TYPE_SINGLE = 1
        const val TYPE_MULTI = 2
    }

    var clickItem: OnClickPracticeFilter? = null
    var longClickItem: OnLongClickPracticeFilter? = null

    var items = mutableListOf<ItemPracticeItemContent>()
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

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            ItemPracticeItemContent.TYPE_PRACTICE -> TYPE_SINGLE
            ItemPracticeItemContent.TYPE_FOLDER -> TYPE_MULTI
            else -> TYPE_SINGLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        when (viewType) {
            TYPE_SINGLE -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_single))
            TYPE_MULTI -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_multi))
            else -> BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_single))
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        when (getItemViewType(position)) {
            TYPE_SINGLE -> {
                with(holder.itemView) {
                    imgPracticeThumbFilter.show(item.img)
                    txtTitleItemPracticeFilter.text = item.title
                    txtNameSubjectPracticeFilter.setTextNotNull(item.subject?.title)
                    clickWithDebounce { clickItem?.setOnClickPracticeFilter(item, TYPE_SINGLE) }
                    setOnLongClickListener {
                        longClickItem?.setOnClickPracticeFilter(
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
                    imgFolderPracticeFilterThumb.show(item.img)
                    imgFolderPracticeFilterThumbClone1.show(item.img)
                    imgFolderPracticeFilterThumbClone2.show(item.img)
                    txtFolderTitleItemPracticeFilter.text = item.title
                    txtNameSubjectPracticeFolderFilter.setTextNotNull(item.subject?.title)
                    clickWithDebounce { clickItem?.setOnClickPracticeFilter(item, TYPE_MULTI) }
                    setOnLongClickListener {
                        longClickItem?.setOnClickPracticeFilter(
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

    fun interface OnClickPracticeFilter {
        fun setOnClickPracticeFilter(item: ItemPracticeItemContent, type: Int)
    }

    fun interface OnLongClickPracticeFilter {
        fun setOnClickPracticeFilter(item: ItemPracticeItemContent, type: Int, pos: Int)
    }
}