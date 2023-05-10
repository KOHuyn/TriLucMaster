package com.mobileplus.dummytriluc.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.mobileplus.dummytriluc.R
import com.utils.ext.clickWithDebounce

/**
 * Created by ThaiNV on 1/26/2021.
 */
class ViewSeekBar : RelativeLayout {
    var v: View? = null
    var c: Context? = null
    var progress: Int = 5
    var maxProgress = 10
    var min: Int = 0
    var max: Int = 100

    var level: String? = null
    private var tvLevel: TextView? = null
    var seekBar: CustomSeekBar? = null

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

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    fun init(
        cont: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) {
        c = cont
        v = LayoutInflater.from(cont).inflate(R.layout.view_seek_bar, this, true)

        if (attrs != null) {
            val t = context.obtainStyledAttributes(attrs, R.styleable.ViewSeekBar)
            try {
                level = t.getString(R.styleable.ViewSeekBar_level)
//            min = t.getInt(R.styleable.ViewSeekBar_min, 0)
//            progress = t.getInt(R.styleable.ViewSeekBar_current, 0)
//            max = t.getInt(R.styleable.ViewSeekBar_max, 100)
//            maxProgress = t.getInt(R.styleable.ViewSeekBar_maxProgress, 10)
            } finally {
                t.recycle()
            }
        }
        initView()
    }

    private fun initView() {
        v?.run {
            tvLevel = findViewById(R.id.tvLevel)
            seekBar = findViewById(R.id.csb)
        }
    }

    fun generate(
        min: Int,
        maxValue: Int,
        maxProgress: Int,
        progress: Int,
        level: String,
        updateListener: OnUpdateValue,
    ) {

        this.min = min
        this.max = maxValue
        this.maxProgress = maxProgress
        this.progress = progress
        this.level = level
        tvLevel?.text = level
        seekBar?.generate(min, maxValue, maxProgress, progress, updateListener)
    }

    interface OnUpdateValue {
        fun onUpdate(progress: Int)
    }

    class CustomSeekBar : RelativeLayout {
        var v: View? = null
        var current: Int = 5
        var maxProgress = 7
        var min: Int = 50
        var max: Int = 120

        private var tvStart: TextView? = null
        private var tvEnd: TextView? = null
        private var seekBar: SeekBar? = null
        var tvValue: TextView? = null

        var updateListener: OnUpdateValue? = null

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
            v = LayoutInflater.from(context).inflate(R.layout.custom_seek_bar, this, true)

            tvStart = findViewById(R.id.tvStart)
            tvEnd = findViewById(R.id.tvEnd)
            seekBar = findViewById(R.id.seekBar)
            tvValue = findViewById(R.id.tvValue)

            val t = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar)
            try {
                current = t.getInt(R.styleable.CustomSeekBar_current, 5)
                maxProgress = t.getInt(R.styleable.CustomSeekBar_maxProgress, 7)
                min = t.getInt(R.styleable.CustomSeekBar_min, 50)
                max = t.getInt(R.styleable.CustomSeekBar_max, 120)
            } finally {
                t.recycle()
            }
            updateView()
        }

        @SuppressLint("SetTextI18n")
        private fun updateView() {
            tvStart?.text = "$min%"
            tvEnd?.text = "$max%"

            seekBar?.run {
                max = maxProgress
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        tvValue?.visibility = View.INVISIBLE
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        tvValue?.visibility = View.VISIBLE
                        seekBar?.let { updateValue(it) }
                    }
                })
                resumeState()
                val realValue = this.progress * 10 + this@CustomSeekBar.min
                updateListener?.onUpdate(realValue)
                tvValue?.text = "${realValue}%"
                Handler(Looper.getMainLooper()).postDelayed({
                    val bounds = this.thumb.bounds
                    val half = if (tvValue != null) (tvValue!!.measuredWidth / 4) else 15
                    tvValue?.translationX = (this.left + bounds.left - half).toFloat()
                }, 300)
            }
        }

        fun updateValue(seekBar: SeekBar) {
            current = seekBar.progress
            val realValue = seekBar.progress * 10 + min
            updateListener?.onUpdate(realValue)
            tvValue?.text = "${realValue}%"
            val bounds = seekBar.thumb.bounds
            val half = if (tvValue != null) (tvValue!!.measuredWidth / 4) else 15
            tvValue?.translationX = (seekBar.left + bounds.left - half).toFloat()
        }

        fun getRealValue(): Int {
            if (seekBar != null) return seekBar!!.progress * 10 + min else return 0
        }

        fun generate(
            min: Int,
            maxValue: Int,
            maxProgress: Int,
            progress: Int,
            updateListener: OnUpdateValue
        ) {
            this.min = min
            this.max = maxValue
            this.maxProgress = maxProgress
            this.current = progress
            this.updateListener = updateListener
            updateView()
        }

        fun resumeState() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                seekBar?.setProgress(current, true)
            } else {
                tvValue?.visibility = View.INVISIBLE
            }
        }
    }
}
