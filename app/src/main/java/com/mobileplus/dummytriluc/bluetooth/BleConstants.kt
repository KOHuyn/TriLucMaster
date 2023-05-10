package com.mobileplus.dummytriluc.bluetooth

/**
 * Created by KOHuyn on 5/13/2021
 */
object BleConstants {
    const val NOTIFICATION_ID_FOREGROUND_SERVICE = 8466503
    const val ARG_CLICK_FROM_NOTIFICATION = "arg_click_notification"
    const val BLUETOOTH_TRILUC_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb"
    object ACTION {
        const val START_ACTION = "bluetooth.action.start"
        const val TURN_ON_BLE_ACTION = "bluetooth.action.on"
        const val CONNECTING_BLE_ACTION = "bluetooth.action.connecting"
        const val REQUEST_CONNECT_BLE_ACTION = "bluetooth.action.request_connect"
        const val TURN_OFF_BLE_ACTION = "bluetooth.action.off"
        const val MAIN_ACTION = "bluetooth.action.main"
    }

    object STATE_SERVICE {
        const val ON = 10
        const val OFF = 20
        const val CONNECTING = 25
        const val REQUEST_CONNECT = 28
        const val PREPARE = 30
        const val DISABLE_CONNECT = 40
        const val NOT_INIT = 0
    }
}