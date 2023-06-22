package com.mobileplus.dummytriluc.ui.main.connect

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentConnectDeviceBinding
import com.mobileplus.dummytriluc.transceiver.ConnectionState
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.command.ChangePressureCommand
import com.mobileplus.dummytriluc.transceiver.command.UpdateFirmwareCommand
import com.mobileplus.dummytriluc.transceiver.command.UpdateSoundCommand
import com.mobileplus.dummytriluc.transceiver.ext.getOrNull
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.transceiver.mode.SendCommandFrom
import com.mobileplus.dummytriluc.ui.dialog.ChangePressureDialog
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
        handleSuggestConnection()
        checkBleConnect()
        initAction()
    }

    private fun handleSuggestConnection() {
        val machineInfo = transceiver.getMachineInfo()
        if (machineInfo?.machineRoom != null && !transceiver.isConnected()) {
            YesNoButtonDialog().setTitle(getString(R.string.notification))
                .setMessage(getString(R.string.connect_old_machine, machineInfo.machineRoom))
                .setTextCancel(getString(R.string.no))
                .setTextAccept(getString(R.string.yes))
                .setOnCallbackAcceptButtonListener {
                    vm.connectByMachineCode(machineInfo.machineRoom)
                }
                .show(parentFragmentManager, "machineRoom")
        }
    }

    private fun checkBleConnect() {
        addDispose(vm.rxMachineInfo.subscribe { (isSuccess, machineInfo) ->
            if (machineInfo != null) {
                when (machineInfo.status) {
                    1 -> {
                        transceiver.connectToMachine(machineInfo.copy(updateSound = true, statusFirmware = true))
                        connectedToMachine(machineInfo.machineRoom ?: MACHINE_NAME_DEFAULT)
                    }

                    0 -> {
                        unConnectToMachine()
                        EspTouchActivity.open(requireActivity())
                    }

                    else -> unConnectToMachine()
                }
            } else {
                unConnectToMachine()
            }
        }, vm.rxForceConnect.subscribe { (message, machineEncode) ->
            YesNoButtonDialog()
                .setTitle(getString(R.string.label_alert))
                .setMessage(message)
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
        },vm.rxMessage.subscribe { toast(it) })
        handleStateTransceiver()
    }

    private fun handleStateTransceiver() {
        transceiver.onConnectionStateChange(lifecycle) { state ->
            val machineName = vm.machineInfoCache?.machineRoom ?: MACHINE_NAME_DEFAULT
            binding.layoutActionUpdateMachine.isVisible = state == ConnectionState.CONNECTED
            when (state) {
                ConnectionState.CONNECTING -> {
                    connectingToMachine(machineName)
                }
                ConnectionState.CONNECTED -> {
                    connectedToMachine(machineName)
                }
                ConnectionState.DISCONNECTED -> {
                    unConnectToMachine()
                }
                ConnectionState.NONE -> {
                    unConnectToMachine()
                }
            }
        }
        transceiver.onSendCommandStateListener(lifecycle) { state ->
            logErr(state.toString())
            val isLoading = state is SendCommandFrom.FromApp
            when (state.commandMode) {
                CommandMode.CHANGE_PRESSURE -> {
                    if (isLoading) showDialogWithMessage(
                        getString(R.string.updating_pressure),
                        true
                    ) else hideDialogWithMessage()
                }

                CommandMode.UNDEFINE -> {
                    when (state) {
                        is SendCommandFrom.FromApp -> {
                            if (state.command is UpdateSoundCommand) {
                                showDialogWithMessage(getString(R.string.updating_sound), false)
                            }
                        }
                        is SendCommandFrom.FromMachine -> {
                            if (state.data.has("update_sound_result")) {
                                hideDialogWithMessage()
                            }
                        }
                    }
                }
                CommandMode.UPDATE_FIRM_WARE -> {
                    if (isLoading) showDialogWithMessage(
                        getString(R.string.updating_firmware),
                        true
                    ) else hideDialogWithMessage()
                }
                else -> Unit
            }
            when (state) {
                is SendCommandFrom.FromApp -> Unit

                is SendCommandFrom.FromMachine -> {
                    when (state.commandMode) {
                        CommandMode.CHANGE_PRESSURE -> {
                            when (state.data.getOrNull<Boolean>("receive_success")) {
                                true -> toast(getString(R.string.change_pressure_success))
                                false -> toast(getString(R.string.change_pressure_failed))
                                null -> Unit
                            }
                        }
                        CommandMode.UPDATE_FIRM_WARE -> {
                            when (state.data.getOrNull<Boolean>("update_firmware_result")) {
                                true -> toast(getString(R.string.update_firmware_success))
                                false -> toast(getString(R.string.update_firmware_failed))
                                null -> Unit
                            }
                        }
                        CommandMode.UNDEFINE-> {
                            when (state.data.getOrNull<Boolean>("update_sound_result")) {
                                true -> toast(getString(R.string.update_sound_success))
                                false -> toast(getString(R.string.update_sound_failed))
                                null -> Unit
                            }
                        }
                        else -> Unit
                    }
                }
            }
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
        binding.btnChangePressure.clickWithDebounce {
            ChangePressureDialog().setOnPressureSelected { pressure ->
                transceiver.send(ChangePressureCommand(pressure))
            }.show(parentFragmentManager, "ChangePressureDialog")
        }
        binding.btnUpdateFirmware.clickWithDebounce {
            val needUpdateFirmware = transceiver.getMachineInfo()?.statusFirmware
            if (needUpdateFirmware == true) {
                val linkFirmware = transceiver.getMachineInfo()?.linkFirmware
                if (!linkFirmware.isNullOrBlank()) {
                    YesNoButtonDialog()
                        .setMessage(getString(R.string.alert_message_update_firmware))
                        .setOnCallbackAcceptButtonListener {
                            val updateFirmwareCommand = UpdateFirmwareCommand(linkFirmware)
                            transceiver.send(updateFirmwareCommand)
                        }.showDialog(parentFragmentManager)
                }
            } else {
                toast(getString(R.string.alert_message_firmware_up_to_date))
            }
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
        vm.connectByMachineCode(stringExtra)
    }
}