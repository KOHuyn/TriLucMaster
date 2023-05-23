package com.mobileplus.dummytriluc.ui.main.connect

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentConnectDeviceBinding
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.MainViewModel
import com.mobileplus.dummytriluc.ui.main.setupwifi.EspTouchActivity
import com.mobileplus.dummytriluc.ui.scanner.ScannerActivity
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.clickWithDebounce
import com.utils.ext.invisible
import com.utils.ext.postNormal
import com.utils.ext.show
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


class ConnectFragment : BaseFragmentZ<FragmentConnectDeviceBinding>() {
    override fun getLayoutBinding(): FragmentConnectDeviceBinding =
        FragmentConnectDeviceBinding.inflate(layoutInflater)

    private var animBleDisposable: Disposable? = null
    private val vm by viewModel<MainViewModel>()

    private val transceiver by lazy { ITransceiverController.getInstance() }
    private var machineEncode: String? = null

    companion object {
        private const val MACHINE_NAME_DEFAULT = "TriLucMaster"
        private const val REQUEST_BAR_CODE = 123
        private const val REQUEST_PERMISSION_CAMERA = 1233
        fun openFragment() {
            postNormal(EventNextFragmentMain(ConnectFragment::class.java, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        checkBleConnect()
        initAction()
    }

    private fun checkBleConnect() {
        addDispose(vm.rxMachineInfo.subscribe { machineInfo ->
            when (machineInfo.status) {
                1 -> {
                    transceiver.connectToMachine(machineInfo)
                    connectedToMachine(machineInfo.machineRoom ?: MACHINE_NAME_DEFAULT)
                }
                0 -> {
                    unConnectToMachine()
                    EspTouchActivity.open(requireActivity())
                }
                else -> unConnectToMachine()
            }
        }, vm.rxForceConnect.subscribe { machineEncode ->
            YesNoButtonDialog()
                .setTitle(getString(R.string.label_alert))
                .setMessage(getString(R.string.warning_force_connect))
                .setDismissWhenClick(false)
                .setTextAccept(getString(R.string.yes))
                .setTextCancel(getString(R.string.no))
                .setOnCallbackAcceptButtonListener {
                    it.dismiss()
                    vm.forceConnect(machineEncode)
                }.setOnCallbackCancelButtonListener {
                    it.dismiss()
                    unConnectToMachine()
                }
                .showDialog(parentFragmentManager)
        }, vm.isLoading.subscribe { isLoading ->
            if (isLoading) connectingToMachine(machineEncode ?: MACHINE_NAME_DEFAULT)
        })
        if (transceiver.isConnected()) {
            connectedToMachine(transceiver.getMachineInfo()?.machineRoom ?: MACHINE_NAME_DEFAULT)
        } else {
            unConnectToMachine()
        }
    }

    private fun connectedToMachine(machineName: String) {
        binding.btnConnectBle.show()
        binding.txtStateConnectDevice.text =
            String.format(loadStringRes(R.string.ble_connected_with), machineName)
        binding.btnConnectBle.text = loadStringRes(R.string.disconnect)
        animConnectBle(true)
        binding.imageConnectDevice.setImageResource(R.drawable.state_connected_device)
    }

    private fun unConnectToMachine() {
        binding.btnConnectBle.show()
        binding.txtStateConnectDevice.text =
            loadStringRes(R.string.bluetooth_is_not_connected_to_the_exercise_machine)
        binding.btnConnectBle.text = loadStringRes(R.string.connect)
        animConnectBle(false)
        binding.imageConnectDevice.setImageResource(R.drawable.state_connecting_device)
    }

    private fun connectingToMachine(machineName: String) {
        binding.imageConnectDevice.setImageResource(R.drawable.state_connecting_device)
        binding.btnConnectBle.invisible()
        animConnectBle(true)
        binding.txtStateConnectDevice.text =
            String.format(
                loadStringRes(R.string.ble_connecting_with),
                machineName
            )
    }

    private fun disconnectToMachine() {
        binding.btnConnectBle.show()
        binding.btnConnectBle.text = loadStringRes(R.string.connect)
        animConnectBle(false)
        binding.imageConnectDevice.setImageResource(R.drawable.state_connecting_device)
        binding.txtStateConnectDevice.text =
            loadStringRes(R.string.disconnected)
    }

    private fun removeFragment() {
        try {
            parentFragmentManager.fragments.map {
                if (it.tag == ConnectFragment::class.java.simpleName) {
                    parentFragmentManager.beginTransaction().remove(it).commit()
                    parentFragmentManager.popBackStack()
                }
            }
        } catch (e: IllegalStateException) {
            e.logErr()
        }
    }

    private fun animConnectBle(animate: Boolean) {
        if (animate) {
            if (animBleDisposable == null) {
                Observable.timer(500, TimeUnit.MILLISECONDS)
                    .repeat()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe {
                        binding.imageConnectDevice.let { it.isSelected = !it.isSelected }
                    }.let { animBleDisposable = it }
            }
        } else {
            animBleDisposable?.dispose()
            animBleDisposable = null
        }
    }

    private fun initAction() {
        binding.btnBackConnectBle.clickWithDebounce {
            onBackPressed()
        }
        binding.btnConnectBle.clickWithDebounce {
            if (transceiver.isConnected()) {
                transceiver.disconnect()
                disconnectToMachine()
            } else {
                openScanBarCode()
            }
        }
        binding.btnSetupWifi.clickWithDebounce {
            EspTouchActivity.open(requireActivity())
        }
        binding.cbDataSecurity.isChecked = vm.isDataSecurity
        binding.cbDataSecurity.setOnCheckedChangeListener { _, isChecked ->
            vm.isDataSecurity = isChecked
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        animBleDisposable?.dispose()
    }

    private fun isEnablePermissionCamera(): Boolean {
        var isEnable = false
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
        } else {
            isEnable = true
        }
        return isEnable
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread { openScanBarCode() }
            }
        }
    }

    private fun openScanBarCode() {
        if (isEnablePermissionCamera()) {
            startActivityForResult(
                Intent(requireContext(), ScannerActivity::class.java),
                REQUEST_BAR_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_BAR_CODE) {
                setupBarCode(data?.getStringExtra(ScannerActivity.ARG_INTENT_BAR_CODE))
            }
        }
    }

    private fun setupBarCode(stringExtra: String?) {
        if (stringExtra == null) return
        vm.connectByBarCode(stringExtra)
    }
}