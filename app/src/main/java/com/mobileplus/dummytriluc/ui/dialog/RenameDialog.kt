package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.setValue
import com.utils.KeyboardUtils
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_group_disciple.*

class RenameDialog : BaseDialog() {
    private var title: String? = null
    private var description: String? = null
    private var listener: ModifyGroupNameDialogListener? = null
    private var renameOld: String? = null

    override fun getLayoutId(): Int = R.layout.dialog_group_disciple

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        txtTitleGroupDialog?.setTextNotNull(title)
        txtLabelEditTextGroup?.setTextNotNull(description)
        if (renameOld != null) {
            edtNameGroupDialog.setValue(renameOld!!)
        }

        btnAcceptGroupDialog?.clickWithDebounce {
            val newName = edtNameGroupDialog.text.toString()
            if (newName.isBlank()) {
                toast(getString(R.string.you_have_not_entered_the_group_name))
            } else {
                listener?.onAcceptClick(newName)
                dismiss()
            }
        }
        btnSkipGroupDialog?.clickWithDebounce {
            dismiss()
        }
        edtNameGroupDialog.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                KeyboardUtils.forceShowKeyboard(v)
            } else {
                KeyboardUtils.hideKeyboardInDialogFragment(v)
            }
        }
    }

    class Builder {
        private val renameDialog by lazy { RenameDialog() }
        fun setTitle(title: String): Builder {
            renameDialog.title = title
            return this
        }

        fun setDescription(description: String): Builder {
            renameDialog.description = description
            return this
        }

        fun setRenameOld(nameOld: String): Builder {
            renameDialog.renameOld = nameOld
            return this
        }

        fun build(fragmentManager: FragmentManager): RenameDialog {
            renameDialog.show(fragmentManager, this::class.java.simpleName)
            return renameDialog
        }
    }


    fun setListenerCallback(newName: (String) -> Unit) {
        listener = ModifyGroupNameDialogListener {
            newName.invoke(it)
        }
    }

    private fun interface ModifyGroupNameDialogListener {
        fun onAcceptClick(newName: String)
    }

}