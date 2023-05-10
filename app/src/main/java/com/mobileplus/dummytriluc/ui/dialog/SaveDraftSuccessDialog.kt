package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_save_draft_success.*


/**
 * Created by KO Huyn on 1/4/2021.
 */
class SaveDraftSuccessDialog : BaseDialog() {

    private var callbackConfirm: CallbackConfirmDialog? = null

    private var titleSaved = "---"

    override fun isFullWidth(): Boolean {
        return true
    }

    override fun getLayoutId(): Int = R.layout.dialog_save_draft_success

    override fun updateUI(savedInstanceState: Bundle?) {
        txtTitleVideoSaved?.text = titleSaved
        btnConfirmSaveDraft.clickWithDebounce {
            dismiss()
            callbackConfirm?.setOnCallbackConfirmDialog()
        }
    }

    fun setCallbackConfirm(callback: (Unit) -> Unit): SaveDraftSuccessDialog {
        callbackConfirm = CallbackConfirmDialog {
            callback.invoke(Unit)
        }
        return this
    }

    fun setTitleSaved(title: String): SaveDraftSuccessDialog {
        this.titleSaved = title
        return this
    }

    fun setCancelableDialog(isCancelable: Boolean): SaveDraftSuccessDialog {
        this.isCancelable = isCancelable
        return this
    }

    fun show(fm: FragmentManager) {
        show(fm, this::class.java.simpleName)
    }


    private fun interface CallbackConfirmDialog {
        fun setOnCallbackConfirmDialog()
    }
}