package com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog.PositionPickerDialog.PositionPickerDialogListener
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_set_fight_point.*

class PositionPickerDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_set_fight_point
    private var listener: PositionPickerDialogListener? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        btnPosHeadLeft?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.LEFT_CHEEK.key, this)
        }

        btnPosHeadCenter?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.FACE.key, this)
        }

        btnPosHeadRight?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.RIGHT_CHEEK.key, this)
        }

        btnPosChestLeft?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.LEFT_CHEST.key, this)
        }

        btnPosChestRight?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.RIGHT_CHEST.key, this)
        }

        btnPosHipLeft?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.LEFT_ABDOMEN.key, this)
        }

        btnPosHipCenter?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.ABDOMEN.key, this)
        }

        btnPosHipBottom?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.ABDOMEN_UP.key, this)
        }

        btnPosHipRight?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.RIGHT_ABDOMEN.key, this)
        }

        btnPosLegLeft?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.LEFT_LEG.key, this)
        }

        btnPosLegRight?.clickWithDebounce {
            listener?.onClickPosition(BlePosition.RIGHT_LEG.key, this)
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