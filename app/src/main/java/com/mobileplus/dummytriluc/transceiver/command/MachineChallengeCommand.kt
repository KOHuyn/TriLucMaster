package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 19/06/2023.
 */
@Parcelize
data class MachineChallengeCommand(
    val challengeId: Int,
    val hitData: String? = "0",
    val hitLimit: Int? = 0,
    val timeLimit: Int? = 0,
    val positionLimit: String? = "0",
    val weight: Int? = 0,
    val minPower: Int? = 0,
    val randomDelayTime: Int? = 0,
    val challengeType: Int? = null
) : IPracticeCommand {

    override fun getIdPractice(): Long {
        return challengeId.toLong()
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["challenge_id"] = challengeId
        params["challenge_type"] = challengeType
        params["time"] = System.currentTimeMillis() / 1000
        params["hit_data"] = hitData
        params["hit_limit"] = hitLimit
        params["time_limit"] = timeLimit
        params["position_limit"] = positionLimit
        params["weight"] = weight
        params["min_power"] = minPower
        params["random_delay_time"] = randomDelayTime
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.CHALLENGE
    }
}