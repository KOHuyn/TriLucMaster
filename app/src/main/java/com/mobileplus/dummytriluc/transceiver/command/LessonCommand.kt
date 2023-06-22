package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

/**
 * Created by KO Huyn on 23/05/2023.
 * ex:
 * LessonCommand(
lessonId = listOf(dataDetail?.id ?: -1),
round = 2,
fullName = "",
level = JSONObject().apply {
put("name", "level1")
put("value", 0.75)
}, avgPower = avgResponse?.avgPower ?: 50,
avgHit = avgResponse?.avgHit ?: 20
)
 */
//@Parcelize
//data class LessonCommand(
//    val lessonId: List<Int>,
//    val round: Int,
//    val fullName: String,
////    val level: JSONObject,
//    val avgPower: Int,
//    val avgHit: Int
//) : IMachineCommand {
//
//    override fun params(): HashMap<String, Any?> {
//        val params = hashMapOf<String, Any?>()
//        params["time"] = System.currentTimeMillis() / 1000
//        params["lession_id"] = lessonId
//        params["round"] = round
//        params["fullname"] = fullName
////        params["level"] = level
//        params["avg_power"] = avgPower
//        params["avg_hit"] = avgHit
//        return params
//    }
//
//    override fun getCommandMode(): CommandMode {
//        return CommandMode.LESSON
//    }
//}