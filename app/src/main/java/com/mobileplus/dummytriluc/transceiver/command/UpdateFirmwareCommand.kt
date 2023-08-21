package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 22/06/2023.
 */
@Parcelize
data class UpdateFirmwareCommand(val linkFirmware: String) : IMachineCommand {
    override fun getEventName(): TransceiverEvent {
        return TransceiverEvent.PRACTICE
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["status_firmware"] = true
        params["link_firmware"] = linkFirmware
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.UPDATE_FIRM_WARE
    }
}