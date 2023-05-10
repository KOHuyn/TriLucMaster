package com.mobileplus.dummytriluc.ui.main.testconnect

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothStatus
import com.mobileplus.dummytriluc.bluetooth.CommandBle
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.MainFragment
import com.mobileplus.dummytriluc.ui.main.MainViewModel
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_devices_bluetooth.*
import kotlinx.android.synthetic.main.fragment_test_connect.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class TestConnectFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_test_connect
    private var animBleDisposable: Disposable? = null
    private var isPracticing = false
    private var isPunchDemo = false
    private val vm by viewModel<MainViewModel>()

    override fun updateUI(savedInstanceState: Bundle?) {
        showPopupGuide()
        checkBleConnect()
        initAction()
        addDispose(
            (requireActivity() as MainActivity).rxCallbackDataBle.observeOn(AndroidSchedulers.mainThread())
                .subscribe { response ->
                    val isSuccess: Boolean = response.first
                    val data = response.second
                    if (isSuccess) {
                        if (data.isNotEmpty()) {
                            vm.submitPractice(data.first())
                            nextFragmentHome()
                        }
                    } else {
                        nextFragmentHome()
                    }
                })
    }

    private fun showPopupGuide() {
        YesNoButtonDialog()
            .setTitle("Hướng dẫn dùng máy tập lần đầu")
            .setMessage("Đấm 10 cú vào điểm bất bất kì")
            .setTextAccept("Tập nào!!!")
            .setTextCancel("Bỏ qua")
            .setOnCallbackAcceptButtonListener { connectBluetooth() }
            .setOnCallbackCancelButtonListener { nextFragmentHome() }
            .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
    }

    private fun nextFragmentHome() {
        clearAllBackStack()
        postNormal(EventNextFragmentMain(MainFragment::class.java, true))
    }

    private fun checkBleConnect() {
        if (requireActivity() is MainActivity) {
            if ((requireActivity() as MainActivity).isConnectedBle) {
                txtStateConnectDeviceTest.text = loadStringRes(R.string.connected)
                animConnectBle(true)
                imageConnectDeviceTest?.setImageResource(R.drawable.state_connected_device)
            } else {
                txtStateConnectDeviceTest.text =
                    loadStringRes(R.string.bluetooth_is_not_connected_to_the_exercise_machine)
                animConnectBle(false)
                imageConnectDeviceTest?.setImageResource(R.drawable.state_connecting_device)
            }
            addDispose(
                (requireActivity() as MainActivity).rxStateConnectBle.subscribe { stateBle ->
                    when (stateBle) {
                        BluetoothStatus.CONNECTING -> {
                            btnStartPracticeTest.setBackgroundResource(R.color.clr_grey)
                            imageConnectDeviceTest?.setImageResource(R.drawable.state_connecting_device)
                            animConnectBle(true)
                            txtStateConnectDeviceTest.text = loadStringRes(R.string.connecting)
                        }
                        BluetoothStatus.CONNECTED -> {
                            btnStartPracticeTest.setBackgroundResource(R.drawable.gradient_orange)
                            imageConnectDeviceTest?.setImageResource(R.drawable.state_connected_device)
                            animConnectBle(true)
                            txtStateConnectDeviceTest.text = loadStringRes(R.string.connected)

                        }
                        BluetoothStatus.DISCONNECTED -> {
                            btnStartPracticeTest.setBackgroundResource(R.color.clr_grey)
                            animConnectBle(false)
                            imageConnectDeviceTest?.setImageResource(R.drawable.state_connecting_device)
                            txtStateConnectDeviceTest.text = loadStringRes(R.string.disconnected)
                        }
                        else -> {
                        }
                    }
                })
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
                        imageConnectDeviceTest?.let { it.isSelected = !it.isSelected }
                    }.let { animBleDisposable = it }
            }
        } else {
            animBleDisposable?.dispose()
            animBleDisposable = null
        }
    }

    private fun initAction() {
        btnBackConnectBleTest.clickWithDebounce {
            onBackPressed()
        }
        btnStartPracticeTest.clickWithDebounce {
            isPracticing = (requireActivity() as MainActivity).isConnectedBle
            if ((requireActivity() as MainActivity).isConnectedBle) {
                (requireActivity() as MainActivity).actionWriteBle(
                    String.format(
                        CommandBle.FIRST_CONNECT,
                        System.currentTimeMillis() / 1000, vm.user?.id ?: 1
                    )
                )
                isPunchDemo = true
                btnStartPracticeTest.hide()
                txtStateConnectDeviceTest.text = "Đang trong chế độ luyện tập"
            } else {
                connectBluetooth()
            }
        }
        btnSkipConnectBleTest.clickWithDebounce {
            if (isPunchDemo) {
                (requireActivity() as MainActivity).actionWriteBle(CommandBle.END)
            }
            nextFragmentHome()
        }
    }

    private fun connectBluetooth() {
        if (activity is MainActivity)
            (activity as MainActivity).showDialogRequestConnect()
        logErr("connecting")
    }

    override fun onDestroy() {
        super.onDestroy()
        animBleDisposable?.dispose()
    }
}