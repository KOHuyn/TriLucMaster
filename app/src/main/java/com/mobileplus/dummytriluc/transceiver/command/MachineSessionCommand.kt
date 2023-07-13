package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 13/07/2023.
 */
@Parcelize
data class MachineSessionCommand(val idSession: Long) : IPracticeCommand {
    override fun getIdPractice(): Long {
        return idSession
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["id"] = idSession
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.SESSION
    }
}