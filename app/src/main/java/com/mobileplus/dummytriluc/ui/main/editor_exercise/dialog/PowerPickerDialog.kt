package com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.setValue
import com.utils.KeyboardUtils
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_set_power.*

class PowerPickerDialog(private val numberDefault: Int = 0) : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_set_power
    private var listener: PowerPickerDialogListener? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        edtPowerEditExercise?.setValue(if (numberDefault == 0) "" else numberDefault.toString())
        edtPowerEditExercise.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                KeyboardUtils.forceShowKeyboard(v)
            } else {
                KeyboardUtils.hideKeyboardInDialogFragment(v)
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (edtPowerEditExercise != null) {
                if (numberDefault != 0) {
                    edtPowerEditExercise.setSelection(edtPowerEditExercise.text?.length ?: 0)
                }
                KeyboardUtils.forceShowKeyboard(edtPowerEditExercise)
            }
        }, 100)
        txtAcceptEditExercise.clickWithDebounce {
            val edtValuePower = edtPowerEditExercise.text.toString()
            if (edtValuePower.isBlank()) {
                listener?.onFillPower(0, this)
            } else {
                if (edtValuePower.toInt() > 200) {
                    toast(getString(R.string.require_force))
                } else {
                    listener?.onFillPower(edtValuePower.toInt(), this)
                }
            }
        }

        txtSkipEditExercise?.clickWithDebounce {
            listener?.onFillPower(0, this)
        }
    }

    fun build(fm: FragmentManager): PowerPickerDialog {
        show(fm, PowerPickerDialog::class.java.simpleName)
        return this
    }

    fun onFillPowerCallback(callback: (power: Int, dialog: PowerPickerDialog) -> Unit) {
        activity?.let { KeyboardUtils.hideKeyboard(it) }
        listener = PowerPickerDialogListener(callback)
    }

    private fun interface PowerPickerDialogListener {
        fun onFillPower(newPower: Int, dialog: PowerPickerDialog)
    }
}