package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemInvitePeople
import com.mobileplus.dummytriluc.ui.main.challenge.adapter.InvitePeopleAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.clickWithDebounce
import com.utils.ext.onTextChanged
import kotlinx.android.synthetic.main.dialog_invite_people.*

class InvitePeopleDialog constructor(private val isInviteChallenge: Boolean = false) :
    BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_invite_people

    override fun isFullWidth(): Boolean = true

    private val adapterInvite by lazy { InvitePeopleAdapter(isChoicePeople = true) }

    var onCallbackPeopleInvite: OnCallbackPeopleInvite? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        rcvInvitePeopleDialog.run {
            layoutManager =
                LinearLayoutManager(requireContext())
            adapter = adapterInvite
            setHasFixedSize(false)
            addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.space_8).toInt()))
        }
        fakeData()
        edtSearchInviteDialog.onTextChanged { search -> }
        btnSubmitInvitePeopleDialog.clickWithDebounce {
            if (adapterInvite.isCheckedInviteItem()) {
                if (isInviteChallenge) {
                    if (adapterInvite.isCheckOneItem()) {
                        onCallbackPeopleInvite?.setOnCallbackPeopleInvite(adapterInvite.getItemSelected())
                        dismiss()
                    } else {
                        toast("Bạn chỉ được chọn 1 người để thách đấu")
                    }
                } else {
                    onCallbackPeopleInvite?.setOnCallbackPeopleInvite(adapterInvite.getItemSelected())
                    dismiss()
                }
            } else {
                toast("Bạn chưa chọn ai cả")
            }
        }
    }

    private fun fakeData() {
        val items = mutableListOf<ItemInvitePeople>()
        items.add(
            ItemInvitePeople(
                "https://lh3.googleusercontent.com/proxy/XTfW97c7hO-xTCvOkBacWnlv5IYzHv_p9mSrwR-A0NTMwqVuq_qpv-NmZzpoulDG2tUWlfvrbvIeKYxEHNr4-bEF6ycJg_xQuXVYtfUswU_gb2CWqvGsFy2_dECZKA",
                "CUNG LÊ",
                300896
            )
        )
        items.add(
            ItemInvitePeople(
                "https://file.hstatic.net/1000045032/file/fm-e1433941678273.jpg",
                "BÁCH LÊ",
                300895
            )
        )
        adapterInvite.items = items
    }

    fun interface OnCallbackPeopleInvite {
        fun setOnCallbackPeopleInvite(data: MutableList<ItemInvitePeople>)
    }
}