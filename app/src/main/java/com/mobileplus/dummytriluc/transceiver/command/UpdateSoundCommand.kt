package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 22/06/2023.
 */
@Parcelize
object UpdateSoundCommand :IMachineCommand {
    override fun getEventName(): TransceiverEvent {
        return TransceiverEvent.PRACTICE
    }

    override fun params(): HashMap<String, Any?> {
        return hashMapOf()
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.UNDEFINE
    }

}