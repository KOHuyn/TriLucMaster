package com.mobileplus.dummytriluc.transceiver.command

import android.os.Parcelable
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 13/07/2023.
 */
@Parcelize
data class MachineSessionActionCommand(val action: SessionAction) : IMachineCommand {

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["action"] = action.code
        if (action is SessionAction.ChooseUser) {
            params["user_id"] = action.userId
        }
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.SESSION
    }

}

sealed class SessionAction(val code: Int) : Parcelable {
    @Parcelize
    object Next : SessionAction(0)

    @Parcelize
    object Previous : SessionAction(1)

    @Parcelize
    object Pause : SessionAction(2)

    @Parcelize
    object Resume : SessionAction(3)

    @Parcelize
    object End : SessionAction(4)

    @Parcelize
    object Reload : SessionAction(5)

    @Parcelize
    data class ChooseUser(val userId: Int) : SessionAction(6)
}