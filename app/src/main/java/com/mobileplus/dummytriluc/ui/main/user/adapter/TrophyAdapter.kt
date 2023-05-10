package com.mobileplus.dummytriluc.ui.main.user.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.utils.ext.inflateExt

class TrophyAdapter : RecyclerView.Adapter<BaseViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder
    = BaseViewHolder(parent.inflateExt(R.layout.item_trophy))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

    }

    override fun getItemCount(): Int  = 5

}