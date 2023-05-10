package com.mobileplus.dummytriluc.ui.main.connect

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothStatus
import com.mobileplus.dummytriluc.databinding.FragmentConnectDeviceBinding
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.MainViewModel
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

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(ConnectFragment::class.java, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        checkBleConnect()
        initAction()
    }

    private fun checkBleConnect() {
        val bleDevice = (activity as MainActivity).bleDevice
        if (requireActivity() is MainActivity) {
            if ((requireActivity() as MainActivity).isConnectedBle) {
                val nameBle = bleDevice?.name ?: bleDevice?.address
                binding.txtStateConnectDevice.text =
                    String.format(loadStringRes(R.string.ble_connected_with), nameBle)
                binding.btnConnectBle.text = loadStringRes(R.string.disconnect)
                animConnectBle(true)
                binding.imageConnectDevice?.setImageResource(R.drawable.state_connected_device)
            } else {
                binding.txtStateConnectDevice.text =
                    loadStringRes(R.string.bluetooth_is_not_connected_to_the_exercise_machine)
                binding.btnConnectBle.text = loadStringRes(R.string.connect)
                animConnectBle(false)
                binding.imageConnectDevice?.setImageResource(R.drawable.state_connecting_device)
            }
            addDispose(
                (requireActivity() as MainActivity).rxStateConnectBle
                    .subscribe { stateBle ->
                        val nameBle =
                            (activity as MainActivity).bleDevice?.name
                                ?: (activity as MainActivity).bleDevice?.address ?: ""
                        when (stateBle) {
                            BluetoothStatus.CONNECTING -> {
                                binding.imageConnectDevice?.setImageResource(R.drawable.state_connecting_device)
                                binding.btnConnectBle.invisible()
                                animConnectBle(true)
                                binding. txtStateConnectDevice.text =
                                    String.format(
                                        loadStringRes(R.string.ble_connecting_with),
                                        nameBle
                                    )
                            }
                            BluetoothStatus.CONNECTED -> {
                                binding.imageConnectDevice?.setImageResource(R.drawable.state_connected_device)
                                binding.btnConnectBle.show()
                                animConnectBle(true)
                                binding.btnConnectBle.setBackgroundResource(R.drawable.gradient_orange)
                                binding.btnConnectBle.text = loadStringRes(R.string.disconnect)
                                binding.txtStateConnectDevice.text =
                                    String.format(
                                        loadStringRes(R.string.ble_connected_with),
                                        nameBle
                                    )
                                Handler(Looper.getMainLooper()).post { removeFragment() }
                            }
                            BluetoothStatus.DISCONNECTED -> {
                               binding. btnConnectBle.show()
                               binding. btnConnectBle.text = loadStringRes(R.string.connect)
                                animConnectBle(false)
                                binding.imageConnectDevice?.setImageResource(R.drawable.state_connecting_device)
                                binding.txtStateConnectDevice.text = loadStringRes(R.string.disconnected)
                            }
                            else -> {
                            }
                        }
                    })
        }
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
                        binding.imageConnectDevice?.let { it.isSelected = !it.isSelected }
                    }.let { animBleDisposable = it }
            }
        } else {
            animBleDisposable?.dispose()
            animBleDisposable = null
        }
    }

    private fun initAction() {
        binding. btnBackConnectBle.clickWithDebounce {
            onBackPressed()
        }
        binding.btnConnectBle.clickWithDebounce {
            connectBluetooth()
        }
        binding.cbDataSecurity.isChecked = vm.isDataSecurity
        binding.cbDataSecurity.setOnCheckedChangeListener { _, isChecked ->
            vm.isDataSecurity = isChecked
        }
    }


    private fun connectBluetooth() {
        if (requireActivity() is MainActivity)
            (requireActivity() as MainActivity).connectBluetooth()
    }

    override fun onDestroy() {
        super.onDestroy()
        animBleDisposable?.dispose()
    }
}