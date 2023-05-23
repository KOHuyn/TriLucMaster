package com.mobileplus.dummytriluc.transceiver.command

/**
 * Created by KO Huyn on 23/05/2023.
 */
data class PracticeLedPunchCommand(val avgPower: Int, val avgHit: Int) : IPracticeCommand {
    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["mode"] = 4
        params["avg-power"] = avgPower
        params["avg-hit"] = avgHit
        params["time"] = System.currentTimeMillis() / 1000
        return params
    }
}