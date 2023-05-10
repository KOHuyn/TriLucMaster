package com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemEditorExercise
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_add_more_video.view.*

/**
 * Created by ThaiNV on 1/25/2021.
 * Copied from KO Huyn
 */
class AddMoreVideoAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    var items = mutableListOf<ItemEditorExercise>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onAllVideoSelected: OnAllVideoSelected? = null

    var onMessageCallback: OnMessageCallback? = null

    var countItemSelected = 0

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
        BaseViewHolder(parent.inflateExt(R.layout.item_add_more_video))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgPracticeThumbVideoPreview.show(item.imagePath)
            txtTitleItemVideoPreview.text = item.title
            cbSelectedVideo.isChecked = item.isSelected
            cbSelectedVideo.setOnCheckedChangeListener { _, isChecked ->
                if (item.isSelected) {
                    item.isSelected = isChecked
                    onAllVideoSelected?.setOnAllVideoSelected(
                        item, isChecked
                    )
                } else {
                    if (countItemSelected < 5) {
                        item.isSelected = isChecked
                        onAllVideoSelected?.setOnAllVideoSelected(
                            item, isChecked
                        )
                    } else {
                        onMessageCallback?.setOnMessageCallback(context.getString(R.string.you_cannot_choose_more_than_5_videos))
                        cbSelectedVideo.isChecked = false
                        onAllVideoSelected?.setOnAllVideoSelected(
                            null, isChecked
                        )
                        notifyItemChanged(position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnAllVideoSelected {
        fun setOnAllVideoSelected(item: ItemEditorExercise?, isChecked: Boolean)
    }

    fun interface OnMessageCallback {
        fun setOnMessageCallback(msg: String)
    }
}