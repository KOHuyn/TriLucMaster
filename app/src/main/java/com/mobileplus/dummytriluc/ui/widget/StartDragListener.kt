package com.mobileplus.dummytriluc.ui.widget

import androidx.recyclerview.widget.RecyclerView

/**
 * Created by KOHuyn on 4/21/2021
 */
interface StartDragListener {
    fun requestDrag(viewHolder: RecyclerView.ViewHolder)
}