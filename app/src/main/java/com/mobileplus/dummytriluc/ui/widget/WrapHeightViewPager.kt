package com.mobileplus.dummytriluc.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class WrapHeightViewPager:ViewPager{

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasure: Int) {
        var heightMeasureSpec = heightMeasure
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
        // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            // super has to be called in the beginning so the child views can be initialized.
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            var height = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                val h = child.measuredHeight
                if (h > height) height = h
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        // super has to be called again so the new specs are treated as exact measurements
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}