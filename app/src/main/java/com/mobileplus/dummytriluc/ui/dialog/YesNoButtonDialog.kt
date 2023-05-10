package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog.AcceptButtonListener
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import kotlinx.android.synthetic.main.dialog_yes_no_button.*

class YesNoButtonDialog : BaseDialog() {

    companion object {
        const val TAG = "YesNoButtonDialog"
    }

    private var title: String = loadStringRes(R.string.label_alert)
    private var content: String? = null
    private var acceptButton: String = loadStringRes(R.string.label_accept)
    private var cancelButton: String = loadStringRes(R.string.cancel)
    private var acceptListener: AcceptButtonListener? = null
    private var cancelListener: CancelButtonListener? = null
    private var isDismissClick: Boolean = true
    private var isCancelButton: Boolean = true
    override fun getLayoutId(): Int = R.layout.dialog_yes_no_button

    override fun updateUI(savedInstanceState: Bundle?) {
        txtTitleGroupDialog.text = title
        txtContentDialog.text = content
        tvCancel.text = cancelButton
        btnAccept.text = acceptButton

        btnAccept.setOnClickListener {
            if (isDismissClick) {
                dismiss()
            }
            acceptListener?.onAcceptClick(this)
        }
        tvCancel.isVisible = isCancelButton
        tvCancel.setOnClickListener {
            if (isDismissClick) {
                dismiss()
            }
            cancelListener?.onCancelClick(this)
        }
    }

    fun showDialog(fragmentManager: FragmentManager, tag: String = TAG): YesNoButtonDialog {
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val prevFragment: Fragment? = fragmentManager.findFragmentByTag(tag)
        try {
            prevFragment?.let { transaction.remove(prevFragment) }
            transaction.addToBackStack(null)
            show(transaction, tag)
        } catch (e: IllegalStateException) {
            e.logErr()
        }
        return this
    }

    fun setTitle(title: String): YesNoButtonDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): YesNoButtonDialog {
        this.content = message
        return this
    }

    fun setTextCancel(cancelText: String): YesNoButtonDialog {
        this.cancelButton = cancelText
        return this
    }

    fun setTextAccept(acceptText: String): YesNoButtonDialog {
        this.acceptButton = acceptText
        return this
    }

    fun setDismissWhenClick(isDismiss: Boolean): YesNoButtonDialog {
        this.isDismissClick = isDismiss
        return this
    }

    fun setShowCancelButton(isCancel: Boolean): YesNoButtonDialog {
        this.isCancelButton = isCancel
        return this
    }

    fun setOnCallbackAcceptButtonListener(accept: (YesNoButtonDialog) -> Unit): YesNoButtonDialog {
        acceptListener = AcceptButtonListener {
            accept.invoke(it)
        }
        return this
    }

    fun setOnCallbackCancelButtonListener(cancel: (YesNoButtonDialog) -> Unit): YesNoButtonDialog {
        cancelListener = CancelButtonListener {
            cancel.invoke(it)
        }
        return this
    }

    fun interface AcceptButtonListener {
        fun onAcceptClick(dialog: YesNoButtonDialog)
    }

    fun interface CancelButtonListener {
        fun onCancelClick(dialog: YesNoButtonDialog)
    }

}