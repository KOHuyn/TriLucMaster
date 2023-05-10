package com.mobileplus.dummytriluc.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

/**
 *Created by KO Huyn on 6/3/2021
 */
interface BluetoothLeManager {
    fun connect(device: BluetoothDevice): Boolean
    fun disconnect()
    fun writeCharacteristic(
        payload: ByteArray
    )

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, isEnable: Boolean
    )

    fun getSupportedGattService():List<BluetoothGattService>?

    fun refreshCache():Boolean

    companion object {
        const val ACTION_GATT_CONNECTED = "com.mobileplus.dummytriluc.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_CONNECTING = "com.mobileplus.dummytriluc.ACTION_GATT_CONNECTING"
        const val ACTION_GATT_DISCONNECTED = "com.mobileplus.dummytriluc.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICE_DISCOVERED =
            "com.mobileplus.dummytriluc.ACTION_GATT_SERVICE_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.mobileplus.dummytriluc.ACTION_DATA_AVAILABLE"
        const val ACTION_WRITE_AGAIN_BLE = "com.mobileplus.dummytriluc.ACTION_WRITE_AGAIN_BLE"
        const val EXTRA_DATA = "com.mobileplus.dummytriluc.bluetooth.EXTRA_DATA"
        const val EXTRA_WRITE = "com.mobileplus.dummytriluc.bluetooth.EXTRA_WRITE"
    }
}