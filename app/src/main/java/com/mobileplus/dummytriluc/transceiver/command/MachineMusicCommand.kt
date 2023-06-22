package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 18/06/2023.
 */
@Parcelize
data class MachineMusicCommand(val avgPower: Int, val avgHit: Int, val musicId: Int) :
    IPracticeCommand {
    override fun getIdPractice(): Long {
        return 2
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["avg-power"] = avgPower
        params["avg-hit"] = avgHit
        params["time"] = System.currentTimeMillis() / 1000
        params["music_id"] = musicId
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.PLAY_WITH_MUSIC
    }
}