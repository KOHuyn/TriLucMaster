package com.mobileplus.dummytriluc.ui.main.power.adapter

import android.R.color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemProgressPower
import com.mobileplus.dummytriluc.ui.widget.ProgressBarAnimation
import com.utils.ext.applyColorFilter
import com.utils.ext.inflateExt
import com.utils.ext.setBackgroundColorz
import com.utils.ext.setTextColorz
import kotlinx.android.synthetic.main.item_progress_power.view.*


class ProgressPowerAdapter(private val isChallenge: Boolean = false) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemProgressPower>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_progress_power))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            if (isChallenge) {
                txtIntroductionProgressPower.setTextColorz(R.color.clr_tab)
            } else {
                txtIntroductionProgressPower.setTextColorz(R.color.white)
            }
            txtIntroductionProgressPower.text = item.namePosition
            viewDotProgressPower.background.applyColorFilter(item.colorHex)
            progressProgressPower.progress = item.progress
            progressProgressPower.startAnimation(
                ProgressBarAnimation(
                    progressProgressPower,
                    0f,
                    item.progress.toFloat()
                ).apply { duration = 1000 })
            val layers = progressProgressPower.progressDrawable as LayerDrawable
            layers.getDrawable(1).applyColorFilter(item.colorHex)
        }
    }
}