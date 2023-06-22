package com.mobileplus.dummytriluc.transceiver.command

import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 20/06/2023.
 */
@Parcelize
data class MachineLessonCommand(
    val lessonId: Int,
    val positionData: String,
    val positionDelayData: String,
    val avgHit: Int,
    val avgPower: Int
) : IRecordCommand {
    override fun getIdType(): Int {
        return lessonId
    }

    override fun params(): HashMap<String, Any?> {
        //TODO
        return hashMapOf()
    }

    override fun getCommandMode(): CommandMode {
        return CommandMode.LESSON
    }

}