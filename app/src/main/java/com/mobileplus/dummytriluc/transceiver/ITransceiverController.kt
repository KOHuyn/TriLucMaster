package com.mobileplus.dummytriluc.transceiver

import androidx.lifecycle.Lifecycle
import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.observer.ISubjectMachine

/**
 * Created by KO Huyn on 16/05/2023.
 */
interface ITransceiverController : ISubjectMachine {
    fun startup()
    fun disconnect()
    fun isConnected(): Boolean
    fun send(cmd: ICommand)
    fun onTransceiverEventStateListener(listener: (event: TransceiverEvent) -> Unit)
    fun onConnectionStateChange(lifecycle: Lifecycle, listener: (state: ConnectionState) -> Unit)
    fun connectToMachine(machineInfo: MachineInfo)
    fun getMachineInfo(): MachineInfo?

    fun onPingChange(listener: (ping: Int, rssi: Int) -> Unit)

    companion object {
        fun getInstance(): ITransceiverController = TransceiverControllerImpl.getInstance()
    }
}