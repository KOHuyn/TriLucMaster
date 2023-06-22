package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 23/05/2023.
 */
@Parcelize
object FinishCommand : IMachineCommand {
    override fun params(): HashMap<String, Any?> {
        return hashMapOf()
    }
    override fun getCommandMode(): CommandMode {
        return CommandMode.FINISH
    }
}