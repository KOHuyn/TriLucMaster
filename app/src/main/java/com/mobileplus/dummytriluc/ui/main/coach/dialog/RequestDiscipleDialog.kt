package com.mobileplus.dummytriluc.ui.main.coach.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleWaiting
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_request_disciple.*

class RequestDiscipleDialog(private val requestDetail: ItemDiscipleWaiting) : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_request_disciple
    override fun isFullWidth(): Boolean {
        return true
    }

    var listener: AcceptRejectListener? = null
    override fun updateUI(savedInstanceState: Bundle?) {
        txtTitleGroupDialog?.text =
            String.format(
                loadStringRes(R.string.format_title_request_disciple),
                requestDetail.userCreated?.fullName
            )
        txtNameMemberGroup?.setTextNotNull(requestDetail.userCreated?.fullName)
        tvID?.text = String.format(loadStringRes(R.string.format_id), requestDetail.studentId)
        txtContentDeleteDialog?.setTextNotNull(requestDetail.message)
        tvConfirm?.clickWithDebounce {
            dismiss()
            listener?.onAcceptClickListener(this)
        }
        tvReject?.clickWithDebounce {
            dismiss()
            listener?.onRejectClickListener(this)
        }
        imgCancelAddGroupDialog?.clickWithDebounce {
            dismiss()
        }
    }

    interface AcceptRejectListener {
        fun onAcceptClickListener(dialog: DialogFragment)
        fun onRejectClickListener(dialog: DialogFragment)
    }
}