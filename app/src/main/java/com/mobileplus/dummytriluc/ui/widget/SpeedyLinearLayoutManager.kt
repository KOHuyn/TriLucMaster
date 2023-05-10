package com.mobileplus.dummytriluc.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by ThaiNV on 1/29/2021.
 */

class SpeedyLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(
        context,
        orientation,
        reverseLayout
    ) {

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int
    ) {
        val linearSmoothScroller: LinearSmoothScroller =
            object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
                }
            }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    companion object {
        private const val MILLISECONDS_PER_INCH = 1500f //default is 25f (bigger = slower)
    }
}