package com.mobileplus.dummytriluc.transceiver.command

/**
 * Created by KO Huyn on 22/05/2023.
 */
data class ConnectCommand(val machineRoom: String) : ICommand {

    override fun getEventName(): String {
        return "subscribe"
    }

    override fun params(): HashMap<String, Any?> {
        return hashMapOf("machine_id" to machineRoom)
    }
}