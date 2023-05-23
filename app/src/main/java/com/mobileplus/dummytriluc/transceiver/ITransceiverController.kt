package com.mobileplus.dummytriluc.transceiver

import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.data.TransceiverChannel

/**
 * Created by KO Huyn on 16/05/2023.
 */
interface ITransceiverController {
    fun startup()
    fun disconnect()
    fun isConnected(): Boolean
    fun send(cmd: ICommand)
    fun onTransceiverEventStateListener(listener: (TransceiverEvent) -> Unit)
    fun onEventMachineSend(listener: (data: JsonObject) -> Unit)
    fun registerChannel(channel: TransceiverChannel,vararg data: Pair<String, String>)
    fun connectToMachine(machineInfo: MachineInfo)
    fun getMachineInfo(): MachineInfo?

    companion object {
        fun getInstance(): ITransceiverController = TransceiverControllerImpl.getInstance()
    }
}