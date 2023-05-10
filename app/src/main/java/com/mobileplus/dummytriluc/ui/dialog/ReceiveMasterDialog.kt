package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.View
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.KeyboardUtils
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_receive_master.*
import java.util.*

class ReceiveMasterDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_receive_master

    var onSendReceiveMaster: OnSendReceiveMaster? = null

    var nameMaster: String? = null

    override fun isFullWidth(): Boolean {
        return true
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        txtHeaderReceiveMaster.setTextNotNull(
            if (nameMaster == null) null else String.format(
                loadStringRes(R.string.say_something_with),
                nameMaster?.toUpperCase(Locale.getDefault())
            )
        )
        imgCancelReceiveMaster.clickWithDebounce { dismiss() }
        sendReceiveMaster.clickWithDebounce {
            KeyboardUtils.hideKeyboard(requireActivity())
            if (edtMessageReceiveMaster.text.toString().isBlank()) {
                toast(getString(R.string.valid_content_send_to_master))
            } else {
                onSendReceiveMaster?.setOnSendReceiveMaster(edtMessageReceiveMaster.text.toString())
                dismiss()
            }
        }
        edtMessageReceiveMaster.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                view?.let { KeyboardUtils.hideKeyboardInDialogFragment(it) }
            }
        }
    }

    fun interface OnSendReceiveMaster {
        fun setOnSendReceiveMaster(msg: String)
    }
}