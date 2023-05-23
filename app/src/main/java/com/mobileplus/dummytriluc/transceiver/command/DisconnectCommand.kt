package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.data.model.MachineInfo

/**
 * Created by KO Huyn on 22/05/2023.
 */
data class DisconnectCommand(
    val machineInfo: MachineInfo?,
    val userId: Int?,
    val isFromApp: Boolean
) : ICommand {

    override fun getEventName(): String {
        return "unsubscribe"
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["machine_id"] = machineInfo?.machineRoom
        if (isFromApp) {
            params["user_id"] = userId?.toString()
            params["mode"] = 12.toString()
            params["sendTo"] = "MACHINE"
            params["event"] = "PRACTICE"
        }
        return params
    }

}