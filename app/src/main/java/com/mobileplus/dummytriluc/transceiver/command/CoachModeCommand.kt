package com.mobileplus.dummytriluc.transceiver.command

/**
 * Created by KO Huyn on 23/05/2023.
 */
object CoachModeCommand : ICommand {
    override fun getEventName(): String {
        return "PRACTICE"
    }

    override fun params(): HashMap<String, Any?> {
        return hashMapOf("mode" to 1, "time" to System.currentTimeMillis() / 1000)
    }
}