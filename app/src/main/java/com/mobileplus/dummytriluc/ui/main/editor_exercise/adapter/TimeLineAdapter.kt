package com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.SaveExerciseRequest
import com.mobileplus.dummytriluc.ui.utils.VideoUtils
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.widget.PowerPerSecond
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.invisible
import com.utils.ext.show
import kotlinx.android.synthetic.main.view_power_per_second.view.*
import kotlin.math.roundToInt

/**
 * Created by ThaiNV on 1/28/2021.
 */
class TimeLineAdapter :
    RecyclerView.Adapter<BaseViewHolder>() {
    companion object {
        const val SCALE_SMALL = 1
        const val SCALE_NORMAL = 2
        const val SCALE_LARGE = 3
    }

    var currentZoom = SCALE_NORMAL

    var items: MutableList<SaveExerciseRequest.DataVideoTimeLine?> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: PowerPerSecond.PowerPerSecondListener? = null

    fun zoomIn() {
        if (currentZoom < SCALE_LARGE) {
            currentZoom++
        }
        notifyDataSetChanged()
    }

    fun zoomOut() {
        if (currentZoom > SCALE_SMALL) {
            currentZoom--
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent.inflateExt(R.layout.view_power_per_second))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        val isAvailable = item != null
        with(holder.itemView) {
            val lp = layoutParams as RecyclerView.LayoutParams
            lp.marginStart = if (position == 0) {
                context.resources.getDimension(R.dimen._10sdp).roundToInt()
            } else {
                0
            }
            lp.marginEnd = if (position == items.lastIndex) {
                context.resources.getDimension(R.dimen._36sdp).roundToInt()
            } else {
                0
            }

            lp.width = when (currentZoom) {
                SCALE_SMALL -> context.resources.getDimension(R.dimen._50sdp).roundToInt()
                SCALE_NORMAL -> context.resources.getDimension(R.dimen._75sdp).roundToInt()
                SCALE_LARGE -> context.resources.getDimension(R.dimen._100sdp).roundToInt()
                else -> context.resources.getDimension(R.dimen._75sdp).roundToInt()
            }
            layoutParams = lp
            setOnLongClickListener {
                if (isAvailable) {
                    listener?.onDeleteRequest(position)
                }
                true
            }

            clickWithDebounce {
                if (!isAvailable) {
                    listener?.onAddRequest(position)
                }
            }

            if (position % 10 == 0) {
                tvTimeLine.show()
                tvTimeLine.text =
                    VideoUtils.convertDate((position / 10))
            } else {
                tvTimeLine.invisible()
            }

            tvTime.text = VideoUtils.getTime(position)
            if (isAvailable) {
                tvPower.show()
                imagePosition.show()
            } else {
                tvPower.invisible()
                imagePosition.invisible()
            }

            tvPower?.run {
                text = item?.force?.roundToInt().toString()
                clickWithDebounce {
                    if (isAvailable) {
                        listener?.onPowerClick(position)
                    }
                }
            }
            imagePosition?.run {
                item?.getIcon()?.let { imagePosition.show(it) }
                clickWithDebounce {
                    listener?.onPositionClick(position)
                }
            }
        }
    }
}