package com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog.PositionPickerDialog.PositionPickerDialogListener
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.mobileplus.dummytriluc.ui.utils.extensions.BodyPosition
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_set_fight_point.*

class PositionPickerDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_set_fight_point
    private var listener: PositionPickerDialogListener? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        btnPosLeft?.clickWithDebounce {
            listener?.onClickPosition(BodyPosition.LEFT_PUNCH.key, this)
        }

        btnPosCenter?.clickWithDebounce {
            listener?.onClickPosition(BodyPosition.CENTER_PUNCH.key, this)
        }

        btnPosRight?.clickWithDebounce {
            listener?.onClickPosition(BodyPosition.RIGHT_PUNCH.key, this)
        }

        btnPosBottom?.clickWithDebounce {
            listener?.onClickPosition(BodyPosition.HOOK_PUNCH.key, this)
        }
    }

    fun build(fm: FragmentManager): PositionPickerDialog {
        show(fm, PositionPickerDialog::class.java.simpleName)
        return this
    }

    fun setPositionCallbackListener(callback: (pos: String?, dialog: PositionPickerDialog) -> Unit) {
        listener = PositionPickerDialogListener(callback)
    }

    private fun interface PositionPickerDialogListener {
        fun onClickPosition(position: String?, dialog: PositionPickerDialog)
    }
}