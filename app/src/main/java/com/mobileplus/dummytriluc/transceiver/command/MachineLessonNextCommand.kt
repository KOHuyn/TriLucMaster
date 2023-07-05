package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 05/07/2023.
 */
@Parcelize
data class MachineLessonNextCommand(val step: Int) : IMachineCommand {
    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["step"] = step
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.LESSON_NEXT
    }
}
