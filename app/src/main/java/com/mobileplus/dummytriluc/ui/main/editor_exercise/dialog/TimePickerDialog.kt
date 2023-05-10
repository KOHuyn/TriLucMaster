package com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_set_time_picker.*

class TimePickerDialog : BaseDialog() {
    private var listener: TimePickerDialogListener? = null
    private var m = 0
    private var s = 0
    override fun getLayoutId(): Int = R.layout.dialog_set_time_picker
    override fun updateUI(savedInstanceState: Bundle?) {
        val arrMinute = arrayListOf<Int>()
        val arrSecond = arrayListOf<Int>()
        for (i in 0..5) {
            arrMinute.add(i)
        }
        for (i in 0..59) {
            arrSecond.add(i)
        }
        wheelMinuteTimer.data = arrMinute
        wheelSecondTimer.data = arrSecond
        Handler(Looper.getMainLooper()).postDelayed({
            wheelMinuteTimer.setSelectedItemPosition(arrMinute.indexOf(m), true)
            wheelSecondTimer.setSelectedItemPosition(arrSecond.indexOf(s), true)
        }, 100)

        wheelMinuteTimer.setOnItemSelectedListener { _, _, pos ->
            m = arrMinute[pos]
        }
        wheelSecondTimer.setOnItemSelectedListener { _, _, pos ->
            s = arrSecond[pos]
        }
        txtConfirmTimer?.clickWithDebounce {
            dismiss()
            listener?.onConfirmClick(m, s)
        }
        txtSkipTimer?.clickWithDebounce {
            dismiss()
        }
    }

    fun setTimeDefault(miliSecond: Int): TimePickerDialog {
        val second = miliSecond / 10
        m = second / 60
        s = second % 60
        return this
    }

    fun build(fm: FragmentManager): TimePickerDialog {
        show(fm, this::class.java.simpleName)
        return this
    }

    fun setCallbackTimerListener(callback: (minute: Int, second: Int) -> Unit) {
        listener = TimePickerDialogListener(callback)
    }

    private fun interface TimePickerDialogListener {
        fun onConfirmClick(minute: Int, second: Int)
    }
}