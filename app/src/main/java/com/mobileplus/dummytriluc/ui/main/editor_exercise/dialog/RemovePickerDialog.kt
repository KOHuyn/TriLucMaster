package com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_remove_fight.*

class RemovePickerDialog : BaseDialog() {

    private var listener: RemovePickerDialogListener? = null
    override fun getLayoutId(): Int = R.layout.dialog_remove_fight

    override fun updateUI(savedInstanceState: Bundle?) {
        txtDeleteFight?.clickWithDebounce {
            dismiss()
            listener?.onDeleteFight()
        }
        txtSkipFight?.clickWithDebounce {
            dismiss()
        }
    }

    fun build(fm: FragmentManager): RemovePickerDialog {
        show(fm, this::class.java.simpleName)
        return this
    }

    fun setOnRemoveClick(callback: (Unit) -> Unit) {
        listener = RemovePickerDialogListener {
            callback.invoke(Unit)
        }
    }

    private fun interface RemovePickerDialogListener {
        fun onDeleteFight()
    }
}