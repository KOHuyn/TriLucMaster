package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

/**
 * Created by KO Huyn on 20/06/2023.
 */
@Parcelize
data class MachineLessonCommand(
    val lessonId: List<Int>,
    val courseId: Int?,
    val isPlayWithVideo: Boolean,
    val round: Int,
    val level: Pair<String, Int>?,
    val avgHit: Int,
    val avgPower: Int
) : IPracticeCommand {
    override fun getIdPractice(): Long {
        return lessonId.firstOrNull()?.toLong() ?: -1
    }
    override fun params(): HashMap<String, Any?> {
        val params = hashMapOf<String, Any?>()
        params["lession_id"] = lessonId
        params["round"] = round
        params["level"] = level?.let {
            JSONObject().apply {
                val (level, value) = level
                put("level", level)
                put("value", value)
            }
        }
        params["is_play_with_video"] = isPlayWithVideo
        params["time"] = System.currentTimeMillis() / 1000
        params["avg_power"] = avgPower
        params["avg_hit"] = avgHit
        params["course_id"] = courseId
        return params
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.LESSON
    }

}