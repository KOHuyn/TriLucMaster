package com.mobileplus.dummytriluc.ui.main.practice.folder.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemPracticeFolder
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_practice_folder_multi.view.*
import kotlinx.android.synthetic.main.item_practice_folder_single.view.*

/**
 * Created by KOHuyn on 3/11/2021
 */
class PracticeFolderAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemPracticeFolder>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onClickFolder: OnClickFolder? = null

    companion object {
        const val TYPE_SINGLE = 1
        const val TYPE_MULTI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == ItemPracticeFolder.TYPE_FOLDER) TYPE_MULTI else TYPE_SINGLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == TYPE_MULTI) BaseViewHolder(parent.inflateExt(R.layout.item_practice_folder_multi))
        else BaseViewHolder(parent.inflateExt(R.layout.item_practice_folder_single))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        when (getItemViewType(position)) {
            TYPE_SINGLE -> {
                with(holder.itemView) {
                    imgFolderSinglePractice.show(item.imgPath)
                    txtFolderTitleSinglePractice.setTextNotNull(item.title)
                    txtFolderContentSinglePractice.setTextNotNull(item.content)
                    clickWithDebounce { onClickFolder?.setOnClickFolder(item, TYPE_SINGLE) }
                }
            }
            TYPE_MULTI -> {
                with(holder.itemView) {
                    imgFolderMultiPractice.show(item.imgPath)
                    imgFolderMultiPractice1.show(item.imgPath)
                    imgFolderMultiPractice2.show(item.imgPath)
                    txtFolderTitleMultiPractice.setTextNotNull(item.title)
                    txtFolderContentMultiPractice.setTextNotNull(item.content)
                    clickWithDebounce { onClickFolder?.setOnClickFolder(item, TYPE_MULTI) }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnClickFolder {
        fun setOnClickFolder(item: ItemPracticeFolder, type: Int)
    }
}