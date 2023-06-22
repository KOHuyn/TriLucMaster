package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 20/06/2023.
 */
@Parcelize
data class ChangePressureCommand(val pressure: Int) : IMachineCommand {
    override fun getEventName(): TransceiverEvent {
        return TransceiverEvent.PRACTICE
    }

    override fun params(): HashMap<String, Any?> {
        return hashMapOf("p" to pressure.toString())
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.CHANGE_PRESSURE
    }
}