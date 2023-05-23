package com.mobileplus.dummytriluc.transceiver.command

import org.json.JSONObject

/**
 * Created by KO Huyn on 23/05/2023.
 */
data class LessonCommand(
    val lessonId: List<Int>,
    val round: Int,
    val fullName: String,
    val level: JSONObject,
    val avgPower: Int,
    val avgHit: Int
) : ICommand {
    override fun getEventName(): String {
        return "PRACTICE"
    }

    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["mode"] = 6
        params["time"] = System.currentTimeMillis() / 1000
        params["lession_id"] = lessonId
        params["round"] = round
        params["fullname"] = fullName
        params["level"] = level
        params["avg_power"] = avgPower
        params["avg_hit"] = avgHit
        return params
    }
}