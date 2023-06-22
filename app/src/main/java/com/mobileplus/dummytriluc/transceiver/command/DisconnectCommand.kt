package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 22/05/2023.
 */
@Parcelize
data class DisconnectCommand(
    val machineInfo: MachineInfo?,
    val userId: Int?,
    val isFromApp: Boolean
) : ICommand {

    override fun getEventName(): TransceiverEvent {
        return TransceiverEvent.UNSUBSCRIBE
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["machine_id"] = machineInfo?.machineRoom
        if (isFromApp) {
            params["user_id"] = userId?.toString()
            params["sendTo"] = "MACHINE"
            params["event"] = "PRACTICE"
        }
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.DISCONNECT
    }

}