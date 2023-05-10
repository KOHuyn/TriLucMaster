package com.mobileplus.dummytriluc.ui.main.practice.detail.info.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.MediaPractice
import com.mobileplus.dummytriluc.databinding.ItemVideoPreviewBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.*

class VideoPreviewAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemVideoPreviewBinding>>() {

    var items = mutableListOf<MediaPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClicked: OnClickItemAdapter? = null

    fun setSelectedVideo(pos: Int) {
        items.forEach {
            it.isSelected = it.mediaThumb == items[pos].mediaThumb
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemVideoPreviewBinding> =
        BaseViewHolderZ(
            ItemVideoPreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BaseViewHolderZ<ItemVideoPreviewBinding>, position: Int) {
        val item = items[position]
        with(holder) {
            binding.imgThumbPreviewVideo.show(item.mediaThumb)
            when (item.type) {
                MediaPractice.TYPE_IMAGE -> {
                    binding.viewPlayPreviewVideo.hide()
                }
                MediaPractice.TYPE_VIDEO -> {
                    binding.viewPlayPreviewVideo.show()
                }
            }
            if (item.isSelected) {
                binding.imgThumbPreviewVideo.setBackgroundResource(R.drawable.background_stroke_white)
            } else {
                binding.imgThumbPreviewVideo.setBackgroundColorz(android.R.color.transparent)
            }
            binding.root.clickWithDebounce {
                onItemClicked?.setOnClickListener(itemView, position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

}