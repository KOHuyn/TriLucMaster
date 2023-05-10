package com.mobileplus.dummytriluc.data.response

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.model.ItemPracticeDetailMain
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.toList
import java.lang.Exception

data class DetailPracticeResponse(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = "",
    @Expose
    @SerializedName("image_path")
    val imagePath: String?,
    @Expose
    @SerializedName("video_path")
    val videoPath: String? = null,
    @Expose
    @SerializedName("content")
    val content: String? = null,
    @Expose
    @SerializedName("note")
    val note: String? = null,
    @Expose
    @SerializedName("data")
    val data: String? = null,
    @Expose
    @SerializedName("start_time2")
    val startTime2: Long? = null,
    @Expose
    @SerializedName("start_time1")
    val startTime1: Long? = null,
    @Expose
    @SerializedName("end_time")
    val endTime: Long? = null,
    @Expose
    @SerializedName("status")
    val status: Int? = 0,
    @Expose
    @SerializedName("room_id")
    val roomId: Int? = null,
    @Expose
    @SerializedName("room_key_id")
    val roomKeyId: String? = null,
    @Expose
    @SerializedName("nguoi_tao")
    val userCreate: UserCreatePractice? = null,
    @Expose
    @SerializedName("level")
    val level: PairResponse? = null,
    @Expose
    @SerializedName("subject")
    val subject: PairResponse? = null,
    @Expose
    @SerializedName("created_at")
    val createAt: String? = null,
    @Expose
    @SerializedName("link_share")
    val linkShare: String? = null,
    @Expose
    @SerializedName("training_number")
    val trainingNumber: Int? = 0,
    @Expose
    @SerializedName("cap_do_luyen_tap")
    val levelPractice: MutableList<LevelPractice>? = mutableListOf(),
    @Expose
    @SerializedName("media")
    val media: MutableList<MediaPractice>? = mutableListOf(),
    @Expose
    @SerializedName("video_path_origin")
    val videoPathOrigin: String? = null,
) {
    fun getCreatedAt(): String? {
        return if (createAt == null) null else DateTimeUtil.convertDate(
            createAt,
            DateTimeUtil.DATE_SERVER_UTC,
            "dd-MM-yyyy"
        )
    }

    fun getHighScores(): MutableMap<String, Float> {
        val items = mutableMapOf<String, Float>()
        if (data != null) {
            try {
                val dataBle: List<DataBluetooth> =
                    Gson().toList(data)
                dataBle.forEach { blePunch ->
                    if (blePunch.position != null) {
                        if (items.containsKey(blePunch.position)) {
                            if (items[blePunch.position] ?: 0f < blePunch.force ?: 0f) {
                                items[blePunch.position] = blePunch.force ?: 0f
                            }
                        } else {
                            items[blePunch.position] = blePunch.force ?: 0f
                        }
                    }
                }
            } catch (e: Exception) {
                e.logErr()
            }
        }
        return items
    }
}


data class DetailPracticeFolderResponse(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("subject_id")
    val subjectId: Int? = null,
    @Expose
    @SerializedName("level_id")
    val levelId: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("content")
    val content: String? = null,
    @Expose
    @SerializedName("image_path")
    val imagePath: String? = null,
    @Expose
    @SerializedName("type")
    val type: String? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("nguoi_tao")
    val userCreated: UserCreatePractice? = null,
    @Expose
    @SerializedName("level")
    val level: PairResponse? = null,
    @Expose
    @SerializedName("subject")
    val subject: PairResponse? = null
) {
    fun getSubjectFolder() = if (subject?.title != null) String.format(loadStringRes(R.string.value_of_subject),subject.title) else null
    fun getLevelFolder() = if (level?.title != null) String.format(loadStringRes(R.string.value_of_level),level.title) else null
    fun getCreatedAtFolder(): String? {
        val date = if (createdAt == null) null else DateTimeUtil.convertDate(
            createdAt,
            DateTimeUtil.DATE_SERVER_UTC,
            "dd-MM-yyyy"
        )
        return if (date != null) String.format(loadStringRes(R.string.value_of_date_created),date) else null
    }

}

data class LevelPractice(
    @Expose
    @SerializedName("level")
    val level: String? = "",
    @Expose
    @SerializedName("value")
    val value: Int? = 0,
    var isClicked: Boolean = false,
)

data class MediaPractice(
    @Expose
    @SerializedName("media_thumb")
    val mediaThumb: String? = null,
    @Expose
    @SerializedName("media_path")
    val mediaPath: String? = null,
    @Expose
    @SerializedName("media_path_origin")
    val mediaPathOrigin: String? = null,
    @Expose
    @SerializedName("type")
    val type: String? = null,
    var isSelected: Boolean = false
) {
    companion object {
        const val TYPE_VIDEO = "video"
        const val TYPE_IMAGE = "image"
    }
}

data class UserCreatePractice(
    @Expose
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    @SerializedName("level")
    val level: Int? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null,
    @Expose
    @SerializedName("trang_thai_bai_su")
    val statusReceiveMaster: Int? = null,
)
