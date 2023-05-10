package com.mobileplus.dummytriluc.ui.main.practice.detail.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemPracticeDetailMain
import com.mobileplus.dummytriluc.databinding.ItemPracticeDetailMainBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility

class PracticeDetailMainAdapter :
    RecyclerView.Adapter<BaseViewHolderZ<ItemPracticeDetailMainBinding>>() {

    var items = mutableListOf<ItemPracticeDetailMain>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onClickItem: OnClickItemAdapter? = null

    fun setOnSelectedItem(pos: Int) {
        items.forEach {
            it.isSelected = it.id == items[pos].id
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemPracticeDetailMainBinding> =
        BaseViewHolderZ(
            ItemPracticeDetailMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: BaseViewHolderZ<ItemPracticeDetailMainBinding>,
        position: Int
    ) {
        val item = items[position]
        with(holder) {
            binding.root.clickWithDebounce {
                setOnSelectedItem(position)
                onClickItem?.setOnClickListener(binding.root, position)
            }
            if (item.isSelected) {
                binding.root.setBackgroundColor(
                    ResourcesCompat.getColor(
                        itemView.resources,
                        R.color.clr_bg_title_dark,
                        null
                    )
                )
            } else {
                binding.root.setBackgroundColor(
                    ResourcesCompat.getColor(
                        itemView.resources,
                        R.color.clr_tab,
                        null
                    )
                )
            }

            binding.txtCurrDatePracticeDetailMain.run {
                text = item.getCurrDate()
                setVisibility(item.getCurrDate() != null)
            }
            binding.txtCurrTimePracticeDetailMain.run {
                text = item.getTimehhmm()
                setVisibility(item.getTimehhmm() != null)
            }
            binding.viewPunchPracticeDetailMain.setVisibility(item.totalPunch != null)
            item.totalPunch?.let { binding.txtValuePunchesPracticeDetailMain.text = it.toString() }
            binding.viewPowerPracticeDetailMain.setVisibility(item.totalPower != null)
            item.totalPower?.let { binding.txtValuePowerPracticeDetailMain.text = it.toString() }
            binding.viewTimePracticeDetailMain.setVisibility(item.getTimePractice() != null)
            item.getTimePractice()?.let { binding.txtValueTimePracticeDetailMain.text = it }
        }
    }

    override fun getItemCount(): Int = items.size
}