package com.mobileplus.dummytriluc.ui.main.setupwifi

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import com.mobileplus.dummytriluc.R
import java.net.InetAddress

/**
 * Created by KO Huyn on 21/05/2023.
 */
open class EspTouchActivityAbs : AppCompatActivity() {

    protected class StateResult {
        var message: CharSequence? = null
        var enable = true
        var permissionGranted = false
        var wifiConnected = false
        var is5G = false
        var address: InetAddress? = null
        var ssid: String? = null
        var ssidBytes: ByteArray? = null
        var bssid: String? = null
    }

    protected fun checkState(): StateResult {
        val result = StateResult()
        checkPermission(result)
        if (!result.enable) {
            return result
        }
        checkLocation(result)
        if (!result.enable) {
            return result
        }
        checkWifi(result)
        return result
    }

    private fun checkPermission(result: StateResult): StateResult? {
        result.permissionGranted = true
        val locationGranted = (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        if (!locationGranted) {
            val splits = getString(R.string.esptouch_message_permission).split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            require(splits.size == 2) { "Invalid String @RES esptouch_message_permission" }
            val ssb = SpannableStringBuilder(splits[0])
            ssb.append('\n')
            val clickMsg = SpannableString(splits[1])
            val clickSpan = ForegroundColorSpan(-0xffdd01)
            clickMsg.setSpan(clickSpan, 0, clickMsg.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            ssb.append(clickMsg)
            result.message = ssb
            result.permissionGranted = false
            result.enable = false
        }
        return result
    }

    private fun checkLocation(result: StateResult): StateResult? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val manager = getSystemService(LocationManager::class.java)
            val enable = manager != null && LocationManagerCompat.isLocationEnabled(manager)
            if (!enable) {
                result.message = getString(R.string.esptouch_message_location)
                result.enable = false
                return result
            }
        }
        return result
    }

    private fun checkWifi(result: StateResult): StateResult {
        result.wifiConnected = false
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val connected: Boolean = TouchNetUtil.isWifiConnected(wifiManager)
        if (!connected) {
            result.message = getString(R.string.esptouch_message_wifi_connection)
            return result
        }
        val ssid: String = TouchNetUtil.getSsidString(wifiInfo)
        val ipValue = wifiInfo.ipAddress
        if (ipValue != 0) {
            result.address = TouchNetUtil.getAddress(wifiInfo.ipAddress)
        } else {
            result.address = TouchNetUtil.getIPv4Address()
            if (result.address == null) {
                result.address = TouchNetUtil.getIPv6Address()
            }
        }
        result.wifiConnected = true
        result.message = ""
        result.is5G = TouchNetUtil.is5G(wifiInfo.frequency)
        if (result.is5G) {
            result.message = getString(R.string.esptouch_message_wifi_frequency)
        }
        result.ssid = ssid
        result.ssidBytes = TouchNetUtil.getRawSsidBytesOrElse(wifiInfo, ssid.toByteArray())
        result.bssid = wifiInfo.bssid
        result.enable = result.wifiConnected
        return result
    }
}
