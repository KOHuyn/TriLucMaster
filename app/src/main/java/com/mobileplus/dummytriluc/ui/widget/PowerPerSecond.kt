package com.mobileplus.dummytriluc.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce

/**
 * Created by ThaiNV on 1/27/2021.
 */
class PowerPerSecond : LinearLayout {

    var v: View? = null

    var tvPower: TextView? = null
    var imagePosition: ImageView? = null
    var tvTime: TextView? = null
    var tvTimeLine: TextView? = null

    constructor(context: Context) : super(context) {
        init(context, null, -1)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs, -1)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    fun init(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) {
        v = LayoutInflater.from(context).inflate(R.layout.view_power_per_second, this, true)

        tvTime = findViewById(R.id.tvTime)
        tvTimeLine = findViewById(R.id.tvTimeLine)
        imagePosition = findViewById(R.id.imagePosition)
        tvPower = findViewById(R.id.tvPower)

    }


    fun generate(
        power: String,
        drawable: Int,
        time: String,
        listener: PowerPerSecondListener,
        timeLine: String?,
        position: Int
    ) {
        tvPower?.run {
            text = power
            clickWithDebounce {
                listener.onPowerClick(position)
            }
        }
        tvTime?.text = time
        imagePosition?.run {
            show(drawable)
            clickWithDebounce {
                listener.onPositionClick(position)
            }
        }
        timeLine?.let { tvTimeLine?.text = timeLine }

    }


    interface PowerPerSecondListener {
        fun onPowerClick(position: Int)
        fun onPositionClick(position: Int)
        fun onDeleteRequest(position: Int)
        fun onAddRequest(position: Int)
    }


}