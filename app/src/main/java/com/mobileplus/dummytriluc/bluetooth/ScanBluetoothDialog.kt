package com.mobileplus.dummytriluc.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialog
import com.core.BaseViewHolder
import com.facebook.internal.Validate.hasPermission
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.*
import kotlinx.android.synthetic.main.dialog_devices_bluetooth.*
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*

class ScanBluetoothDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_devices_bluetooth

    companion object {
        private val TAG: String = ScanBluetoothDialog::class.java.simpleName
        const val ENABLE_BLUETOOTH = 1
        const val REQUEST_ENABLE_DISCOVERY = 2
        const val REQUEST_ACCESS_COARSE_LOCATION = 3
    }

    /* Broadcast receiver to listen for discovery results. */
    private val bluetoothDiscoveryResult = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                if (device.name?.contains(AppConstants.BLE_NAME) == true) {
                    adapterBle.addDeviceBluetooth(device)
                    msgScanningBle?.hide()
                }
                logErr(TAG, device.name ?: device.address)
            }
        }
    }

    /* Broadcast receiver to listen for discovery updates. */
    private val bluetoothDiscoveryMonitor = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    isShowScan = true
                    if (adapterBle.items.isEmpty()) {
                        msgScanningBle?.show()
                        msgScanningBle?.text = loadStringRes(R.string.scanning_device_recently)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    isShowScan = false
                    if (adapterBle.items.isEmpty()) {
                        msgScanningBle?.show()
                        msgScanningBle?.text = loadStringRes(R.string.error_bluetooth_device_not_found)
                    }
                }
            }
        }
    }

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val adapterBle by lazy { BluetoothDeviceAdapter() }
    private var isShowScan = true
        set(value) {
            field = value
            isScanningBle?.setVisibility(field)
            btnRetryScanBle?.setVisibility(!field)
            if (field) adapterBle.clearScanResults()
        }

    private var callbackBleListener: CallbackBleListener? = null

    private val stateConnect by lazy { (requireActivity() as MainActivity).rxStateConnectBle }

    override fun updateUI(savedInstanceState: Bundle?) {
        initBluetooth()
        btnRetryScanBle?.clickWithDebounce {
            initBluetooth()
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

    private fun initBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
            if (bluetoothAdapter.isEnabled) {
                enableDiscovery()
            } else {
                // Bluetooth isn't enabled - prompt user to turn it on
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, ENABLE_BLUETOOTH)
            }
        } else {
            toast(loadStringRes(R.string.error_bluetooth_not_available))
            dismiss()
            callbackBleListener?.dismissDialog()
        }
    }

    private fun enableDiscovery() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        startActivityForResult(intent, REQUEST_ENABLE_DISCOVERY)
    }

    private fun monitorDiscovery() {
        requireActivity().registerReceiver(
            bluetoothDiscoveryMonitor,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        )
        requireActivity().registerReceiver(
            bluetoothDiscoveryMonitor,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        )
    }

    private fun startDiscovery() {
        if (hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (bluetoothAdapter.isEnabled && !bluetoothAdapter.isDiscovering) {
                beginDiscovery()
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun beginDiscovery() {
        context?.registerReceiver(
            bluetoothDiscoveryResult,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        adapterBle.clearScanResults()
        monitorDiscovery()
        bluetoothAdapter.startDiscovery()
        logErr("startDiscovery()")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ACCESS_COARSE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    beginDiscovery()
                } else {
                    toast(loadStringRes(R.string.error_location_permission_missing))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ENABLE_BLUETOOTH -> if (resultCode == Activity.RESULT_OK) {
                enableDiscovery()
            }
            REQUEST_ENABLE_DISCOVERY -> if (resultCode == Activity.RESULT_CANCELED) {
                dismiss()
                toast(loadStringRes(R.string.error_scan_failed_application_registration_failed))
            } else {
                startDiscovery()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireActivity().unregisterReceiver(bluetoothDiscoveryMonitor)
            requireActivity().unregisterReceiver(bluetoothDiscoveryResult)
        } catch (e: IllegalArgumentException) {
            e.logErr()
        }
    }

    fun build(fm: FragmentManager): ScanBluetoothDialog {
        show(fm, TAG)
        return this
    }

    fun setOnCallbackBle(
        scanResultCallBack: (scanResult: BluetoothDevice) -> Unit,
        dismissScan: () -> Unit
    ) {
        callbackBleListener = object : CallbackBleListener {
            override fun setOnCallbackListener(scanResult: BluetoothDevice) {
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