package com.mobileplus.dummytriluc.ui.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.LastPractice
import com.mobileplus.dummytriluc.databinding.ItemLessonBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

class LessonHomeAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemLessonBinding>>() {

    var items = mutableListOf<LastPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemLessonBinding> =
        BaseViewHolderZ(
            ItemLessonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BaseViewHolderZ<ItemLessonBinding>, position: Int) {
        val item = items[position]
        with(holder) {
//            prgLesson.progress = item.progress
            binding.txtLabelNumberOfPunch.text = loadStringRes(R.string.punch)
            binding.txtLabelPower.text = loadStringRes(R.string.power)
            binding.txtNameLesson.text = item.title
            binding.txtTimeLesson.text = item.getTimeLesson()
            binding.txtValuePunches.text = item.sumTotalPunch.toString()
            binding.txtValuePower.text = item.sumTotalPower.toString()
            if (item.sumCalories.isNullOrBlank()) {
                binding.txtValueCal.text = "0"
            } else {
                binding.txtValueCal.text = item.sumCalories
            }
        }
    }
}