package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.data.response.PairResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner

data class SaveExerciseRequest(
    @Expose
    @SerializedName("title")
    var title: String? = null,
    @Expose
    @SerializedName("status")
    var status: Int? = null,
    @Expose
    @SerializedName("end_time")
    var endTime: Long? = null,
    @Expose
    @SerializedName("level_practice")
    var levelPractice: String? = null,
    @Expose
    @SerializedName("content")
    var content: String? = null,
    @Expose
    @SerializedName("image_path")
    var imagePath: String? = null,
    @Expose
    @SerializedName("start_time2")
    var startTime2: Long? = null,
    @Expose
    @SerializedName("level_require")
    var levelRequire: Int? = null,
    @Expose
    @SerializedName("media")
    var media: List<Media>? = null,
    @Expose
    @SerializedName("start_time1")
    var startTime1: Long? = null,
    @Expose
    @SerializedName("video_path")
    var videoPath: String? = null,
    @Expose
    @SerializedName("subject_id")
    var subjectId: Int? = null,
    @Expose
    @SerializedName("data")
    var data: String? = null,
    @Expose
    @SerializedName("note")
    var note: String? = null,
    @Expose
    @SerializedName("more")
    val moreInformation: MoreInformation = MoreInformation()
) {

    companion object {
        const val STATUS_ACTIVE = 1
        const val STATUS_INACTIVE = 2
    }

    data class Media(
        @Expose
        @SerializedName("path")
        val path: String? = null,
        @Expose
        @SerializedName("type")
        val type: String = "video"
    )

    data class MoreInformation(
        @Expose
        var idParent: Int? = null,
        @Expose
        var listDataVideoTimeline: MutableList<DataVideoTimeLine?> = mutableListOf(),
        @Expose
        var totalTimeDuration: Int? = null,
        @Expose
        var imagePath: String? = null,
        @Expose
        var subject: PairResponse? = null,
        @Expose
        var level: PairResponse? = null,
        @Expose
        var levelPractice: MutableList<LevelPractice>? = mutableListOf(),
    )


    class DataVideoTimeLine(
        @Expose
        var force: Float? = null,
        @Expose
        var onTarget: Int? = null,
        @Expose
        var position: String? = null,
        @Expose
        var time: Float? = null
    ){
        fun getIcon(): Int = when (position) {
            BlePosition.FACE.key -> R.drawable.ic_head_center
            BlePosition.LEFT_CHEEK.key -> R.drawable.ic_head_left
            BlePosition.RIGHT_CHEEK.key -> R.drawable.ic_head_right
            BlePosition.LEFT_CHEST.key -> R.drawable.ic_chest_left
            BlePosition.RIGHT_CHEST.key -> R.drawable.ic_chest_right
            BlePosition.ABDOMEN.key -> R.drawable.ic_hip_center
            BlePosition.ABDOMEN_UP.key -> R.drawable.ic_hip_bottom
            BlePosition.LEFT_ABDOMEN.key -> R.drawable.ic_hip_left
            BlePosition.RIGHT_ABDOMEN.key -> R.drawable.ic_hip_right
            BlePosition.LEFT_LEG.key -> R.drawable.ic_leg_left
            BlePosition.RIGHT_LEG.key -> R.drawable.ic_leg_right
            else -> R.drawable.ic_head_center
        }
    }
}