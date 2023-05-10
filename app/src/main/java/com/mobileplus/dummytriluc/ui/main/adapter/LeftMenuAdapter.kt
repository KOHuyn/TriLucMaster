package com.mobileplus.dummytriluc.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.databinding.ItemMenuLeftBinding
import com.utils.ext.clickWithDebounce

class LeftMenuAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemMenuLeftBinding>>() {

    var onClickItem: OnClickItem? = null

    var items = mutableListOf<ItemLeftMenu>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemMenuLeftBinding> =
        BaseViewHolderZ(
            ItemMenuLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BaseViewHolderZ<ItemMenuLeftBinding>, position: Int) {
        val item = items[position]
        with(holder) {
            binding.iconLeftMenu.setImageResource(item.icon)
            binding.labelLeftMenu.text = item.title
            binding.root.clickWithDebounce { onClickItem?.clickItemLeftMenu(item.typeMenu) }
//            if (item.isChecked){
//
//            }
        }
    }

    override fun getItemCount(): Int = items.size

    data class ItemLeftMenu(
        @DrawableRes val icon: Int,
        val title: String,
        val typeMenu: LeftMenu,
        var isChecked: Boolean = false
    )

    fun interface OnClickItem {
        fun clickItemLeftMenu(type: LeftMenu)
    }

    enum class LeftMenu { FRIEND, MESSAGE, MY_GROUP, ASSIGNMENTS, EDIT_VIDEO, COACH_MODE, CHANGE_PASSWORD, INFORMATION, DEVELOP_BLE }


}