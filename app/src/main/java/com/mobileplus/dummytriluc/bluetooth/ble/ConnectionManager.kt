/*
 * Copyright 2019 Punch Through Design LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobileplus.dummytriluc.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.mobileplus.dummytriluc.bluetooth.ble.*
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

private const val GATT_MIN_MTU_SIZE = 23

/** Maximum BLE MTU size as defined in gatt_api.h. */
private const val GATT_MAX_MTU_SIZE = 517

object ConnectionManager {

    private val TAG = ConnectionManager::class.java.simpleName

    private var listeners: MutableSet<WeakReference<ConnectionEventListener>> = mutableSetOf()

    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private var pendingOperation: BleOperationType? = null

    fun refreshOperation() {
        operationQueue.clear()
        pendingOperation = null
    }

    fun servicesOnDevice(device: BluetoothDevice): List<BluetoothGattService>? =
        deviceGattMap[device]?.services

    fun listenToBondStateChanges(context: Context) {
        context.applicationContext.registerReceiver(
            broadcastReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }

    fun registerListener(listener: ConnectionEventListener) {
        if (listeners.map { it.get() }.contains(listener)) {
            return
        }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
        logErr(TAG, "Added listener $listener, ${listeners.size} listeners total")
    }

    fun unregisterListener(listener: ConnectionEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<ConnectionEventListener>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
            logErr(TAG, "Removed listener ${it.get()}, ${listeners.size} listeners total")
        }
    }

    fun connect(device: BluetoothDevice, context: Context) {
        if (pendingOperation != null && pendingOperation !is Disconnect) {
            refreshOperation()
        }
        if (device.isConnected()) {
            logErr(TAG, "Already connected to ${device.address}!")
            deviceGattMap.remove(device)
        }
        enqueueOperation(Connect(device, context.applicationContext))
    }

    fun teardownConnection(device: BluetoothDevice) {
        refreshOperation()
        if (device.isConnected()) {
            enqueueOperation(Disconnect(device))
        } else {
            logErr(TAG, "Not connected to ${device.address}, cannot teardown connection!")
        }
    }

    fun readCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() && characteristic.isReadable()) {
            enqueueOperation(CharacteristicRead(device, characteristic.uuid))
        } else if (!characteristic.isReadable()) {
            logErr(TAG, "Attempting to read ${characteristic.uuid} that isn't readable!")
        } else if (!device.isConnected()) {
            logErr(TAG, "Not connected to ${device.address}, cannot perform characteristic read")
        }
    }

    fun writeCharacteristic(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> {
                logErr(TAG, "Characteristic ${characteristic.uuid} cannot be written to")
                return
            }
        }
        if (device.isConnected()) {
            enqueueOperation(CharacteristicWrite(device, characteristic.uuid, writeType, payload))
        } else {
            logErr(TAG, "Not connected to ${device.address}, cannot perform characteristic write")
        }
    }

    fun readDescriptor(device: BluetoothDevice, descriptor: BluetoothGattDescriptor) {
        if (device.isConnected() && descriptor.isReadable()) {
            enqueueOperation(DescriptorRead(device, descriptor.uuid))
        } else if (!descriptor.isReadable()) {
            logErr(TAG, "Attempting to read ${descriptor.uuid} that isn't readable!")
        } else if (!device.isConnected()) {
            logErr(TAG, "Not connected to ${device.address}, cannot perform descriptor read")
        }
    }

    fun writeDescriptor(
        device: BluetoothDevice,
        descriptor: BluetoothGattDescriptor,
        payload: ByteArray
    ) {
        if (device.isConnected() && (descriptor.isWritable() || descriptor.isCccd())) {
            enqueueOperation(DescriptorWrite(device, descriptor.uuid, payload))
        } else if (!device.isConnected()) {
            logErr(TAG, "Not connected to ${device.address}, cannot perform descriptor write")
        } else if (!descriptor.isWritable() && !descriptor.isCccd()) {
            logErr(TAG, "Descriptor ${descriptor.uuid} cannot be written to")
        }
    }

    fun enableNotifications(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() &&
            (characteristic.isIndicatable() || characteristic.isNotifiable())
        ) {
            enqueueOperation(EnableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            logErr(TAG, "Not connected to ${device.address}, cannot enable notifications")
        } else if (!characteristic.isIndicatable() && !characteristic.isNotifiable()) {
            logErr(
                TAG,
                "Characteristic ${characteristic.uuid} doesn't support notifications/indications"
            )
        }
    }

    fun disableNotifications(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() &&
            (characteristic.isIndicatable() || characteristic.isNotifiable())
        ) {
            enqueueOperation(DisableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            logErr(TAG, "Not connected to ${device.address}, cannot disable notifications")
        } else if (!characteristic.isIndicatable() && !characteristic.isNotifiable()) {
            logErr(
                TAG,
                "Characteristic ${characteristic.uuid} doesn't support notifications/indications"
            )
        }
    }

    fun requestMtu(device: BluetoothDevice, mtu: Int) {
        if (device.isConnected()) {
            enqueueOperation(MtuRequest(device, mtu.coerceIn(GATT_MIN_MTU_SIZE, GATT_MAX_MTU_SIZE)))
        } else {
            logErr(TAG, "Not connected to ${device.address}, cannot request MTU update!")
        }
    }

    fun requestConnectionPriority(device: BluetoothDevice, connectionPriority: Int) {
        if (device.isConnected()) {
            enqueueOperation(ConnectionPriorityRequest(device, connectionPriority))
        } else {
            logErr(
                TAG,
                "Not connected to ${device.address}, cannot request connectionPriority update!"
            )
        }
    }

    // - Beginning of PRIVATE functions

    @Synchronized
    private fun enqueueOperation(operation: BleOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        logErr(TAG, "End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    private fun callbackDisconnect(device: BluetoothDevice) {
        listeners.forEach { it.get()?.onDisconnect?.invoke(device) }
    }

    fun callbackReconnect(device: BluetoothDevice) {
        listeners.forEach { it.get()?.onReconnect?.invoke(device) }
    }

    /**
     * Perform a given [BleOperationType]. All permission checks are performed before an operation
     * can be enqueued by [enqueueOperation].
     */
    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            logErr(TAG, "doNextOperation() called when an operation is pending! Aborting.")
            return
        }

        val operation = operationQueue.poll() ?: run {
            logErr(TAG, "Operation queue empty, returning")
            return
        }
        pendingOperation = operation

        // Handle Connect separately from other operations that require device to be connected
        if (operation is Connect) {
            with(operation) {
                logErr(TAG, "Connecting to ${device.address}")
                listeners.forEach { it.get()?.onConnecting?.invoke() }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
                } else {
                    device.connectGatt(context, false, callback)
                }
            }
            return
        }

        // Check BluetoothGatt availability for other operations
        val gatt = deviceGattMap[operation.device]
            ?: this@ConnectionManager.run {
                logErr(
                    TAG,
                    "Not connected to ${operation.device.address}! Aborting $operation operation."
                )
                signalEndOfOperation()
                return
            }

        // TODO: Make sure each operation ultimately leads to signalEndOfOperation()
        // TODO: Refactor this into an BleOperationType abstract or extension function
        when (operation) {
            is Disconnect -> with(operation) {
                logErr(TAG, "Disconnecting from ${device.address}")
                gatt.close()
                deviceGattMap.remove(device)
                listeners.forEach { it.get()?.onDisconnect?.invoke(device) }
                signalEndOfOperation()
            }
            is CharacteristicWrite -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    characteristic.writeType = writeType
                    characteristic.value = payload
                    gatt.writeCharacteristic(characteristic)
                } ?: this@ConnectionManager.run {
                    logErr(TAG, "Cannot find $characteristicUuid to write to")
                    signalEndOfOperation()
                }
            }
            is CharacteristicRead -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    gatt.readCharacteristic(characteristic)
                } ?: this@ConnectionManager.run {
                    logErr(TAG, "Cannot find $characteristicUuid to read from")
                    signalEndOfOperation()
                }
            }
            is DescriptorWrite -> with(operation) {
                gatt.findDescriptor(descriptorUuid)?.let { descriptor ->
                    descriptor.value = payload
                    gatt.writeDescriptor(descriptor)
                } ?: this@ConnectionManager.run {
                    logErr(TAG, "Cannot find $descriptorUuid to write to")
                    signalEndOfOperation()
                }
            }
            is DescriptorRead -> with(operation) {
                gatt.findDescriptor(descriptorUuid)?.let { descriptor ->
                    gatt.readDescriptor(descriptor)
                } ?: this@ConnectionManager.run {
                    logErr(TAG, "Cannot find $descriptorUuid to read from")
                    signalEndOfOperation()
                }
            }
            is EnableNotifications -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
                    val payload = when {
                        characteristic.isIndicatable() ->
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        characteristic.isNotifiable() ->
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        else ->
                            error("${characteristic.uuid} doesn't support notifications/indications")
                    }

                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!gatt.setCharacteristicNotification(characteristic, true)) {
                            logErr(
                                TAG,
                                "setCharacteristicNotification failed for ${characteristic.uuid}"
                            )
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = payload
                        gatt.writeDescriptor(cccDescriptor)
                    } ?: this@ConnectionManager.run {
                        logErr(TAG, "${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@ConnectionManager.run {
                    logErr(TAG, "Cannot find $characteristicUuid! Failed to enable notifications.")
                    signalEndOfOperation()
                }
            }
            is DisableNotifications -> with(operation) {
                gatt.findCharacteristic(characteristicUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!gatt.setCharacteristicNotification(characteristic, false)) {
                            logErr(
                                TAG,
                                "setCharacteristicNotification failed for ${characteristic.uuid}"
                            )
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(cccDescriptor)
                    } ?: this@ConnectionManager.run {
                        logErr(TAG, "${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@ConnectionManager.run {
                    logErr(TAG, "Cannot find $characteristicUuid! Failed to disable notifications.")
                    signalEndOfOperation()
                }
            }
            is MtuRequest -> with(operation) {
                gatt.requestMtu(mtu)
            }

            is ConnectionPriorityRequest -> with(operation) {
                logErr(TAG, "requestConnectionPriority")
                gatt.requestConnectionPriority(connectionPriority)
                signalEndOfOperation()
            }
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    logErr(TAG, "onConnectionStateChange: connected to $deviceAddress")
                    deviceGattMap[gatt.device] = gatt
                    listeners.forEach { it.get()?.onConnected?.invoke() }
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    logErr(TAG, "onConnectionStateChange: disconnected from $deviceAddress")
                    teardownConnection(gatt.device)
                }
            } else {
                logErr(
                    TAG,
                    "onConnectionStateChange: status $status encountered for $deviceAddress!"
                )
                if (pendingOperation is Connect) {
                    signalEndOfOperation()
                    callbackReconnect(gatt.device)
                } else {
                    teardownConnection(gatt.device)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (gatt != null)
                with(gatt) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        logErr(TAG, "Discovered ${services.size} services for ${device.address}.")
                        printGattTable()
                        requestMtu(device, GATT_MAX_MTU_SIZE)
                        listeners.forEach { it.get()?.onConnectionSetupComplete?.invoke(this) }
                    } else {
                        logErr(TAG, "Service discovery failed due to status $status")
                        teardownConnection(gatt.device)
                    }
                }

            if (pendingOperation is Connect) {
                signalEndOfOperation()
            }

        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            logErr(TAG, "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
            listeners.forEach { it.get()?.onMtuChanged?.invoke(gatt, mtu) }

            if (pendingOperation is MtuRequest) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        logErr(TAG, "Read characteristic $uuid | value: ${value.toHexString()}")
                        listeners.forEach {
                            it.get()?.onCharacteristicRead?.invoke(
                                gatt.device,
                                this
                            )
                        }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        logErr(TAG, "Read not permitted for $uuid!")
                    }
                    else -> {
                        logErr(TAG, "Characteristic read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicRead) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        logErr(TAG, "Wrote to characteristic $uuid | value: ${String(value)}")
                        listeners.forEach {
                            it.get()?.onCharacteristicWrite?.invoke(
                                gatt.device,
                                this
                            )
                        }
                    }
                    else -> {
                        listeners.forEach {
                            it.get()?.onCharacteristicWriteFail?.invoke(
                                gatt.device,
                                this
                            )
                        }
                        logErr(TAG, "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicWrite) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                i++
                logErr(TAG, "Characteristic $uuid changed | $i value: ${String(value)}")
                listeners.forEach { it.get()?.onCharacteristicChanged?.invoke(gatt.device, this) }
            }
        }

        var i = 0

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            with(descriptor) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        logErr(TAG, "Read descriptor $uuid | value: ${value.toHexString()}")
                        listeners.forEach { it.get()?.onDescriptorRead?.invoke(gatt.device, this) }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        logErr(TAG, "Read not permitted for $uuid!")
                    }
                    else -> {
                        logErr(TAG, "Descriptor read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is DescriptorRead) {
                signalEndOfOperation()
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            with(descriptor) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        logErr(TAG, "Wrote to descriptor $uuid | value: ${value.toHexString()}")

                        if (isCccd()) {
                            onCccdWrite(gatt, value, characteristic)
                        } else {
                            listeners.forEach {
                                it.get()?.onDescriptorWrite?.invoke(
                                    gatt.device,
                                    this
                                )
                            }
                        }
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        logErr(TAG, "Write not permitted for $uuid!")
                    }
                    else -> {
                        logErr(TAG, "Descriptor write failed for $uuid, error: $status")
                    }
                }
            }

            if (descriptor.isCccd() &&
                (pendingOperation is EnableNotifications || pendingOperation is DisableNotifications)
            ) {
                signalEndOfOperation()
            } else if (!descriptor.isCccd() && pendingOperation is DescriptorWrite) {
                signalEndOfOperation()
            }
        }

        private fun onCccdWrite(
            gatt: BluetoothGatt,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic
        ) {
            val charUuid = characteristic.uuid
            val notificationsEnabled =
                value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ||
                        value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
            val notificationsDisabled =
                value.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)

            when {
                notificationsEnabled -> {
                    logErr(TAG, "Notifications or indications ENABLED on $charUuid")
                    listeners.forEach {
                        it.get()?.onNotificationsEnabled?.invoke(
                            gatt.device,
                            characteristic
                        )
                    }
                }
                notificationsDisabled -> {
                    logErr(TAG, "Notifications or indications DISABLED on $charUuid")
                    listeners.forEach {
                        it.get()?.onNotificationsDisabled?.invoke(
                            gatt.device,
                            characteristic
                        )
                    }
                }
                else -> {
                    logErr(TAG, "Unexpected value ${value.toHexString()} on CCCD of $charUuid")
                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            with(intent) {
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val previousBondState =
                        getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
                    val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val bondTransition = "${previousBondState.toBondStateDescription()} to " +
                            bondState.toBondStateDescription()
                    logErr(TAG, "${device?.address} bond state changed | $bondTransition")
                }
            }
        }

        private fun Int.toBondStateDescription() = when (this) {
            BluetoothDevice.BOND_BONDED -> "BONDED"
            BluetoothDevice.BOND_BONDING -> "BONDING"
            BluetoothDevice.BOND_NONE -> "NOT BONDED"
            else -> "ERROR: $this"
        }
    }

    private fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)
    fun isConnectedZ(device: BluetoothDevice) = deviceGattMap.containsKey(device)

    fun findGattByDevice(device: BluetoothDevice): BluetoothGatt? =
        if (deviceGattMap.containsKey(device)) deviceGattMap[device] else null

}
