package com.mobileplus.dummytriluc.ui.main.develop_ble

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.CommandBle
import com.mobileplus.dummytriluc.ui.dialog.ConnectBleWithDeviceDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.connect.ConnectFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.clearSpace
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.setValue
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.value
import kotlinx.android.synthetic.main.fragment_develop_ble.*

/**
 * Created by KOHuyn on 5/8/2021
 */
class DevelopBleFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_develop_ble

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(DevelopBleFragment::class.java, true))
        }
    }

    val listAutoCompleteTextView = mutableListOf<String>("2", "3")

    private val rxMsgNotifiBle by lazy { (requireActivity() as MainActivity).rxResponseDataBle }

    private fun actionLongWrite(command: String): Boolean {
        return if (activity is MainActivity) {
            (activity as MainActivity).actionWriteBle(command.plus("~"))
        } else {
            toast(loadStringRes(R.string.feature_not_available))
            false
        }
    }

    private val response = StringBuilder()

    private fun actionWriteBle(command: String): Boolean {
        return if (activity is MainActivity) {
            (activity as MainActivity).actionWriteBle(command)
        } else {
            toast(loadStringRes(R.string.feature_not_available))
            false
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.select_dialog_item,
            listAutoCompleteTextView
        )
        edtWriteCommandBle.run {
            this.setAdapter(adapter)
            this.threshold = 1
            this.setTextColor(Color.BLACK)
        }
        btnBackDevBle.clickWithDebounce { onBackPressed() }
        btnDevBleWriteData.clickWithDebounce {

            if (edtWriteCommandBle.value.isBlank()) {
                toast("Bạn chưa nhập câu lệnh")
            } else {
                if (validConnect()) {
                    if (actionWriteBle(edtWriteCommandBle.value)) {
                        toast("Ghi dữ liệu thành công")
                        if (listAutoCompleteTextView.contains(edtWriteCommandBle.value)) {
                            listAutoCompleteTextView.add(edtWriteCommandBle.value)
                            adapter.add(edtWriteCommandBle.value)
                            adapter.notifyDataSetChanged()
                        }

                    } else {
                        toast("Ghi dữ liệu thất bại")
                    }
                }
            }
        }
        btnDevBleLongWriteData.clickWithDebounce {
            if (edtWriteCommandBle.value.isBlank()) {
                toast("Bạn chưa nhập câu lệnh")
            } else {
                if (validConnect()) {
                    if (actionLongWrite(edtWriteCommandBle.value)) {
                        toast("Ghi dữ liệu thành công")
                        if (!listAutoCompleteTextView.contains(edtWriteCommandBle.value)) {
                            listAutoCompleteTextView.add(edtWriteCommandBle.value)
                            adapter.add(edtWriteCommandBle.value)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        toast("Ghi dữ liệu thất bại")
                    }
                }
            }
        }

        btnDevBleClearAll.clickWithDebounce {
            edtWriteCommandBle.editableText.clear()
            edtDevBleResponse.editableText.clear()
        }

        addDispose(rxMsgNotifiBle.subscribe {
            response.append(it)
            var dataBleJson = ""
            if (response.toString().contains("~")) {
                val dataBle = response.toString().clearSpace()
                response.clear()
                try {
                    logErr("dataBle:$dataBle")
                    dataBleJson = dataBle.replace("~", "")
                    logErr("dataBleJson:$dataBleJson")
                    edtDevBleResponse.setValue(dataBleJson)
                } catch (e: Exception) {
                    e.logErr()
                    toast(loadStringRes(R.string.data_practice_not_available))
                }
                actionWriteBle(CommandBle.END)
            }
        })
    }

    private fun validConnect(): Boolean {
        if (!(requireActivity() as MainActivity).isConnectedBle) {
            ConnectBleWithDeviceDialog()
                .show(parentFragmentManager)
                .onConnectBleCallback { connect ->
                    if (connect) {
                        ConnectFragment.openFragment()
                    } else {
                        toast(loadStringRes(R.string.you_are_not_connected_to_bluetooth_to_use_this_feature))
                    }
                }
        }
        return (requireActivity() as MainActivity).isConnectedBle
    }
}