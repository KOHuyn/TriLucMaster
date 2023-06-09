package com.mobileplus.dummytriluc.transceiver.observer

import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse

/**
 * Created by KO Huyn on 09/06/2023.
 */
interface IObserverMachine {
    fun onEventMachineSendData(data: List<BluetoothResponse>)
}