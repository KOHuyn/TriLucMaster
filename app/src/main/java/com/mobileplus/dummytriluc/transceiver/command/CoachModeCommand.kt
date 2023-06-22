package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 23/05/2023.
 */
@Parcelize
object CoachModeCommand : IPracticeCommand {

    override fun getIdPractice(): Long {
        return AppConstants.INTEGER_DEFAULT.toLong()
    }

    override fun params(): HashMap<String, Any?> {
        return hashMapOf("time" to System.currentTimeMillis() / 1000)
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.COACH
    }
}