package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.TransceiverEvent

/**
 * Created by KO Huyn on 22/05/2023.
 */
interface IMachineCommand : ICommand {
    override fun getEventName(): TransceiverEvent {
        return TransceiverEvent.PRACTICE
    }
}