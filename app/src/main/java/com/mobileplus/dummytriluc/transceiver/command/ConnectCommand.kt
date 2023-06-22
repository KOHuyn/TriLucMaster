package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 22/05/2023.
 */
@Parcelize
data class ConnectCommand(val machineRoom: String) : ICommand {

    override fun getEventName(): TransceiverEvent {
        return TransceiverEvent.SUBSCRIBE
    }

    override fun params(): HashMap<String, Any?> {
        return hashMapOf("machine_id" to machineRoom)
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.CONNECT
    }
}