package com.mobileplus.dummytriluc.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager

class ViewPagerDynamic : ViewPager {
    private var mCurrentView: View? = null

    var isSwipePage: Boolean = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasure: Int) {
        var heightMeasureSpec = heightMeasure
        if (mCurrentView == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        var height = 0
        mCurrentView!!.measure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val h: Int = mCurrentView!!.measuredHeight
        if (h > height) height = h
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun measureCurrentView(currentView: View?) {
        mCurrentView = currentView
        requestLayout()
    }

    fun measureFragment(view: View?): Int {
        if (view == null) return 0
        view.measure(0, 0)
        return view.measuredHeight
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isSwipePage) super.onTouchEvent(event) else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isSwipePage) super.onInterceptTouchEvent(event) else false
    }
}