package com.mobileplus.dummytriluc.transceiver.command

import android.os.Parcelable
import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode

/**
 * Created by KO Huyn on 16/05/2023.
 */
interface ICommand : Parcelable {
    fun getEventName(): TransceiverEvent
    fun params(): HashMap<String, Any?>
    fun getCommandMode(): CommandMode
    fun log(): String {
        return "${getEventName()}: => ${
            params().toList().joinToString(",") { (key, value) -> "$key($value)" }
        }"
    }
}