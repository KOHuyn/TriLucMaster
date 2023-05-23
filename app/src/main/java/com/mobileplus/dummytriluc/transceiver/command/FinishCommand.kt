package com.mobileplus.dummytriluc.transceiver.command

/**
 * Created by KO Huyn on 23/05/2023.
 */
object FinishCommand : IPracticeCommand {
    override fun params(): HashMap<String, Any?> {
        return hashMapOf("mode" to 2)
    }
}