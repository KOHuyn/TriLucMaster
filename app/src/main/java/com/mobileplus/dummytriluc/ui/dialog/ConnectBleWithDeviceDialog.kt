package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_connect_ble_with_device.*

class ConnectBleWithDeviceDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_connect_ble_with_device

    override fun isFullWidth(): Boolean = true

    private var connectBleCallback: ConnectBleCallback? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        btnDismissBleDialog.clickWithDebounce {
            dismiss()
            connectBleCallback?.connect(false)
        }
        btnConnectBleDialog.clickWithDebounce {
            dismiss()
            connectBleCallback?.connect(true)
        }
        btnSkipConnectBleDialog.clickWithDebounce {
            dismiss()
            connectBleCallback?.connect(false)
        }
    }

    fun show(fm: FragmentManager): ConnectBleWithDeviceDialog {
        show(fm, this::class.java.simpleName)
        return this
    }

    fun onConnectBleCallback(connect: (Boolean) -> Unit) {
        connectBleCallback = ConnectBleCallback { connect.invoke(it) }
    }

    private fun interface ConnectBleCallback {
        fun connect(isConnect: Boolean)
    }

}