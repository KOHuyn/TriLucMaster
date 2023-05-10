package com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemEditorExercise
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_video_selected_draft_editor.view.*

class VideoPreviewAdapter :
    RecyclerView.Adapter<BaseViewHolder>() {
    var items = mutableListOf<ItemEditorExercise>()
        set(value) {
            field = value
            selectedPosition = 0
            notifyDataSetChanged()
        }

    var onClickItem: OnClickVideoListener? = null

    var selectedPosition = 0

    var updateDataListener: UpdateDataListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_video_selected_draft_editor))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgPracticeThumbPreview.show(item.imagePath)
            backgroundBolderLess.background = if (position == selectedPosition) {
                ContextCompat.getDrawable(context, R.drawable.background_stroke_white_radius_2)
            } else null

            clickWithDebounce {
                onClickItem?.setOnClickVideoListener(item.videoPath)
                val lastSelected = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(lastSelected)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeSelectedItem() {
        try {
            if (itemCount > 1) {
                items.removeAt(selectedPosition)
                notifyItemRemoved(selectedPosition)
                selectedPosition = 0
                notifyItemChanged(selectedPosition)
                onClickItem?.setOnClickVideoListener(items[0].videoPath)
                updateDataListener?.onUpdateData(items)
            }
        } catch (e: Exception) {
            e.logErr()
        }
    }

    interface UpdateDataListener {
        fun onUpdateData(updatedData: MutableList<ItemEditorExercise>)
    }

    fun interface OnClickVideoListener {
        fun setOnClickVideoListener(path: String?)
    }

}