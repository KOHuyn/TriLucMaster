package com.mobileplus.dummytriluc.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialog
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BleConstants.BLUETOOTH_TRILUC_UUID
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.*
import kotlinx.android.synthetic.main.dialog_devices_bluetooth.*
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*
import java.util.*

/**
 *Created by KO Huyn on 5/30/2021
 */
class ScanBluetoothLeDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_devices_bluetooth

    companion object {
        private val TAG: String = ScanBluetoothLeDialog::class.java.simpleName
    }

    private var durationScanning: Long = 30000L

    private val scanner by lazy { bluetoothAdapter.bluetoothLeScanner }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val adapterBle by lazy { BluetoothDeviceAdapter() }
    private var isShowScan = true
        set(value) {
            field = value
            activity?.runOnUiThread {
                isScanningBle?.setVisibility(field)
                btnRetryScanBle?.setVisibility(!field)
                if (field) adapterBle.clearScanResults()
            }
        }

    private var callbackBleListener: CallbackBleListener? = null

    private val stateConnect by lazy { (requireActivity() as MainActivity).rxStateConnectBle }

    override fun updateUI(savedInstanceState: Bundle?) {
        scanningBle()
        btnRetryScanBle?.clickWithDebounce {
            scanningBle()
        }
        with(rcvDeviceBluetooth) {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = adapterBle
        }
        adapterBle.onClickItem = OnClickItemAdapter { _, position ->
            callbackBleListener?.setOnCallbackListener(adapterBle.items[position])
            dismiss()
        }
        addDispose(stateConnect.subscribe {
            if (it == BluetoothStatus.CONNECTED) {
                dismiss()
            }
        })
        imgCancelScanBle.clickWithDebounce {
            dismiss()
            callbackBleListener?.dismissDialog()
        }
    }

    private fun updateScanUI(isScanning: Boolean, isRestartDevice: Boolean = false) {
        if (isScanning) {
            isShowScan = true
            if (adapterBle.items.isEmpty()) {
                msgScanningBle?.show()
                msgScanningBle?.text = loadStringRes(R.string.scanning_device_recently)
            }
        } else {
            isShowScan = false
            if (adapterBle.items.isEmpty()) {
                msgScanningBle?.show()
                msgScanningBle?.text =
                    if (isRestartDevice) loadStringRes(R.string.error_bluetooth_restart)
                    else loadStringRes(R.string.error_bluetooth_device_not_found)
            }
        }
    }

    private fun scanningBle() {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(BLUETOOTH_TRILUC_UUID)))
            .build()
        scanner.startScan(listOf(scanFilter), settings, scanCallback)
        updateScanUI(true)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isShowScan) {
                stopScan()
            }
        }, durationScanning)
    }

    private fun stopScan(isRestartDevice: Boolean = false) {
        scanner.stopScan(scanCallback)
        updateScanUI(false, isRestartDevice)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            stopScan(errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            try {
                val device = result.device
                logErr(device.name ?: device.address)
                adapterBle.addDeviceBluetooth(device)
            } catch (e: SecurityException) {
                toast(getString(R.string.error_bluetooth_cannot_start))
            } finally {
                msgScanningBle?.hide()
            }
        }
    }

    fun setDurationScanning(ms: Long): ScanBluetoothLeDialog {
        this.durationScanning = ms
        return this
    }

    fun build(fm: FragmentManager): ScanBluetoothLeDialog {
        show(fm, TAG)
        return this
    }

    fun setOnCallbackBle(
        scanResultCallBack: (scanResult: BluetoothDevice) -> Unit,
        dismissScan: () -> Unit
    ) {
        callbackBleListener = object : CallbackBleListener {
            override fun setOnCallbackListener(scanResult: BluetoothDevice) {
                if (isShowScan) {
                    stopScan()
                }
                scanResultCallBack(scanResult)
            }

            override fun dismissDialog() {
                dismissScan()
            }
        }
    }

    private interface CallbackBleListener {
        fun setOnCallbackListener(scanResult: BluetoothDevice)
        fun dismissDialog()
    }

    inner class BluetoothDeviceAdapter : RecyclerView.Adapter<BaseViewHolder>() {

        var items = mutableListOf<BluetoothDevice>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        var onClickItem: OnClickItemAdapter? = null

        fun addDeviceBluetooth(device: BluetoothDevice) {
            items.withIndex()
                .firstOrNull { it.value.address == device.address }?.let {
                    items[it.index] = device
                    notifyItemChanged(it.index)
                } ?: run {
                items.add(device)
                notifyItemInserted(items.size - 1)
            }
        }

        fun clearScanResults() {
            items.clear()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            BaseViewHolder(parent.inflateExt(R.layout.item_bluetooth_device))

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            val item = items[position]
            with(holder.itemView) {
                txtDeviceBleDialog.text = item.name?.trim() ?: item.address
                clickWithDebounce { onClickItem?.setOnClickListener(this, holder.adapterPosition) }
            }
        }
    }
}
