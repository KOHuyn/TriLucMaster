package com.mobileplus.dummytriluc.bluetooth.ble

import android.app.*
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.os.*
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.*
import com.mobileplus.dummytriluc.service.TriLucNotification
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.postNormal
import com.utils.ext.postSticky
import org.koin.android.ext.android.inject
import java.lang.reflect.Method

/**
 *Created by KO Huyn on 6/3/2021
 */
class BluetoothLeService : Service(), BluetoothLeManager {

    companion object {
        val FLAG_NOTIFICATION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private val notification by inject<TriLucNotification>()

    private var titleNotification: String = ""

    private var device: BluetoothDevice? = null

    private var isServiceDestroy: Boolean = false

    var serviceCharacteristic: BluetoothGattCharacteristic? = null
        private set

    var isReconnect: Boolean = false

    private var lastCommandWrite: String? = null

    private var lengthMaximumMTU = 20

    fun getGatt(): BluetoothGatt? = device?.let { ConnectionManager.findGattByDevice(it) }

    fun requestConnectionPriority(connectionPriority: Int) {
        device?.let { ConnectionManager.requestConnectionPriority(it, connectionPriority) }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                broadcastUpdate(BluetoothLeManager.ACTION_GATT_SERVICE_DISCOVERED)
//                Handler(Looper.getMainLooper()).postDelayed({requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)},2000)
            }

            onConnecting = {
                titleNotification =
                    device?.name ?: device?.address ?: loadStringRes(R.string.title_ble)
                broadcastUpdate(BluetoothLeManager.ACTION_GATT_CONNECTING)
            }

            onConnected = {
                titleNotification =
                    device?.name ?: device?.address ?: loadStringRes(R.string.title_ble)
                broadcastUpdate(BluetoothLeManager.ACTION_GATT_CONNECTED)
            }

            onDisconnect = {
                refreshCache()
                broadcastUpdate(BluetoothLeManager.ACTION_GATT_DISCONNECTED)
            }

            onReconnect = {
                ConnectionManager.connect(it, this@BluetoothLeService)
            }

            onCharacteristicRead = { _, characteristic ->
            }

            onCharacteristicWrite = { _, characteristic ->
                logErr("BLUETOOTH_LE_SERVICE", String(characteristic.value))
                if (lastCommandWrite?.contains(String(characteristic.value)) == true) {
                    lastCommandWrite = null
                }
            }

            onCharacteristicWriteFail = { device, characteristic ->
//                disconnect()
//                if (lastCommandWrite != null) {
//                    broadcastUpdate(BluetoothLeManager.ACTION_WRITE_AGAIN_BLE, lastCommandWrite!!)
//                }
//                isReconnect = true
//                connect(device)
            }

            onMtuChanged = { gatt, mtu ->
                logErr("onMtuChanged:$mtu")
                lengthMaximumMTU = mtu - 3
            }

            onCharacteristicChanged = { _, characteristic ->
                broadcastUpdate(BluetoothLeManager.ACTION_DATA_AVAILABLE, characteristic)
            }

            onNotificationsEnabled = { _, characteristic ->
            }

            onNotificationsDisabled = { _, characteristic ->
            }
        }
    }

    override fun connect(device: BluetoothDevice): Boolean {
        this.device = device
        ConnectionManager.connect(device, this)
        return true
    }

    override fun disconnect() {
        if (device == null) return
        ConnectionManager.teardownConnection(device!!)
    }

    var onDisconnectListener: (() -> Unit)? = null

    override fun writeCharacteristic(
        payload: ByteArray
    ) {
        lastCommandWrite = String(payload)
        serviceCharacteristic?.let { characteristic ->
            var byteCommandSub: ByteArray
            for (i in 0..payload.size step lengthMaximumMTU) {
                byteCommandSub = if (i > payload.size - lengthMaximumMTU) {
                    payload.copyOfRange(i, payload.size)
                } else {
                    payload.copyOfRange(i, i + lengthMaximumMTU)
                }
                if (payload.size > 500) {
                    try {
                        Thread.sleep(100)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
                ConnectionManager.writeCharacteristic(device!!, characteristic, byteCommandSub)
            }
        } ?: logErr("ServiceCharacteristic is null")
    }

    override fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, isEnable: Boolean
    ) {
        if (device == null) return
        ConnectionManager.enableNotifications(device!!, characteristic)
        serviceCharacteristic = characteristic
    }

    override fun getSupportedGattService(): List<BluetoothGattService>? {
        return device?.let { ConnectionManager.servicesOnDevice(it) }
    }

    override fun refreshCache(): Boolean {
        val gatt = device?.let { ConnectionManager.findGattByDevice(it) }
        try {
            val refresh: Method? = gatt?.javaClass?.getMethod("refresh")
            if (refresh != null) {
                return refresh.invoke(gatt) as Boolean
            }

        } catch (e: Exception) {
            e.logErr()
        }
        return false
    }

    fun reconnectBle(): Boolean {
        if (device != null) {
            if (isConnected()) {
                isReconnect = false
            } else {
                isReconnect = true
                connect(device!!)
            }
        } else isReconnect = false
        return isReconnect
    }

    fun isConnected() = device?.let { ConnectionManager.isConnectedZ(it) } ?: false

    fun setupAdvertise(adapter: BluetoothAdapter?) {
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTimeout(0)
            .setConnectable(true)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
            .build()
        adapter?.bluetoothLeAdvertiser?.startAdvertising(
            advertiseSettings,
            advertiseData,
            advertiseCallback
        )
    }

    fun stopAdvertise(adapter: BluetoothAdapter?) {
        adapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            logErr("AdvertiseCallback", "onStartSuccess")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            logErr("AdvertiseCallback", "onStartFailure")
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
        when (action) {
            BluetoothLeManager.ACTION_GATT_CONNECTED -> updateCommand(BleConstants.ACTION.TURN_ON_BLE_ACTION)
            BluetoothLeManager.ACTION_GATT_CONNECTING -> updateCommand(BleConstants.ACTION.CONNECTING_BLE_ACTION)
            BluetoothLeManager.ACTION_GATT_DISCONNECTED -> updateCommand(BleConstants.ACTION.TURN_OFF_BLE_ACTION)
        }
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic?) {
        characteristic?.value?.let {
            val intent = Intent(action)
            intent.putExtra(BluetoothLeManager.EXTRA_DATA, it)
            sendBroadcast(intent)
        }
    }

    private fun broadcastUpdate(action: String, payload: String) {
        val intent = Intent(action)
        intent.putExtra(BluetoothLeManager.EXTRA_WRITE, payload)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        ConnectionManager.registerListener(connectionEventListener)
        notification.notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        titleNotification = loadStringRes(R.string.title_ble)
        updateCommand(BleConstants.ACTION.START_ACTION)
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceDestroy = true
        disconnect()
        ConnectionManager.unregisterListener(connectionEventListener)
        stopForeground(true)
        stopSelf()
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    private val mBinder: IBinder = LocalBinder()

    fun updateCommand(action: String) {
        val lIntent = Intent(this, BluetoothLeService::class.java)
        lIntent.action = action
        lIntent.putExtra(BleConstants.ARG_CLICK_FROM_NOTIFICATION, false)
        val lPendingIntent =
            PendingIntent.getService(this, 0, lIntent, FLAG_NOTIFICATION)
        try {
            lPendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    fun updateNotificationLang() {
        notification.updateNotificationLang()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == null || isServiceDestroy) {
            stopForeground(true)
            return START_NOT_STICKY
        }
        val isClickFromNotification: Boolean =
            intent.getBooleanExtra(BleConstants.ARG_CLICK_FROM_NOTIFICATION, false)
        when (intent.action) {
            BleConstants.ACTION.START_ACTION -> {
                logErr("Received start Intent ")
                startForeground(
                    BleConstants.NOTIFICATION_ID_FOREGROUND_SERVICE,
                    notification.prepareNotificationBle(
                        BleConstants.STATE_SERVICE.PREPARE,
                        titleNotification
                    )
                )
            }
            BleConstants.ACTION.TURN_ON_BLE_ACTION -> {
                notification.updateNotificationBle(
                    BleConstants.STATE_SERVICE.ON,
                    titleNotification
                )
                if (isClickFromNotification) {
                    collapseStatusBar()
                }
                logErr(BleConstants.ACTION.TURN_ON_BLE_ACTION)
            }
            BleConstants.ACTION.CONNECTING_BLE_ACTION -> {
                notification.updateNotificationBle(
                    BleConstants.STATE_SERVICE.CONNECTING,
                    titleNotification
                )
                logErr(BleConstants.ACTION.CONNECTING_BLE_ACTION)
            }
            BleConstants.ACTION.REQUEST_CONNECT_BLE_ACTION -> {
                notification.updateNotificationBle(
                    BleConstants.STATE_SERVICE.REQUEST_CONNECT,
                    titleNotification
                )
                if (isClickFromNotification) {
                    collapseStatusBar()
                    if (DummyTriLucApplication.isRunningBackground) {
                        val intentOpenMain = Intent(this, MainActivity::class.java)
                        intentOpenMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intentOpenMain)
                        postSticky(BleTurnOnBackgroundEvent())
                    } else {
                        postNormal(BleTurnOnEvent(true))
                    }
                }
                logErr(BleConstants.ACTION.REQUEST_CONNECT_BLE_ACTION)
            }
            BleConstants.ACTION.TURN_OFF_BLE_ACTION -> {
                titleNotification = loadStringRes(R.string.title_ble)
                notification.updateNotificationBle(
                    BleConstants.STATE_SERVICE.OFF,
                    titleNotification
                )
                disconnect()
                if (isClickFromNotification) {
                    onDisconnectListener?.invoke()
                    collapseStatusBar()
                }
                logErr(BleConstants.ACTION.TURN_OFF_BLE_ACTION)
            }
            else -> {
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }


    private fun collapseStatusBar() {
        val collapseStatusBar = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(collapseStatusBar)
    }

}