package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 22/05/2023.
 */
@Parcelize
data class MachineFreePunchCommand(val avgPower: Int, val avgHit: Int) : IPracticeCommand {
    override fun getIdPractice(): Long {
        return 1
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["avg-power"] = avgPower
        params["avg-hit"] = avgHit
        params["time"] = System.currentTimeMillis() / 1000
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.FREE_FIGHT
    }
}