package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil


/**
 * Created by KO Huyn on 1/6/2021.
 */

data class ItemChat(
    @Expose
    @SerializedName("id")
    var id: Long? = null,
    @Expose
    @SerializedName("message")
    var message: String? = "",
    @Expose
    @SerializedName("type")
    var type: String? = null,
    @Expose
    @SerializedName("object_id")
    var objectId: Int? = null,
    @Expose
    @SerializedName("status")
    var status: Int? = 0,
    @Expose
    @SerializedName("created_at")
    var createdAt: String? = null,
    @Expose
    @SerializedName("user_id")
    var userId: Int? = null,
    @Expose
    @SerializedName("nguoi_tao")
    var userChat: UserChat? = null,
    @Expose
    @SerializedName("dummy_result")
    var dummyResult: DummyResult? = null,
    var isSend: Boolean = false,
    var sendStatus: ChatSendStatus = ChatSendStatus.SEND_SUCCESS
) {
    fun getTimeStampSend() =
        if (createdAt != null) DateTimeUtil.convertDateToTimeStamp(createdAt!!) else 0

    fun getTimeDDMMYYYY() = if (createdAt != null) DateTimeUtil.convertDate(
        createdAt!!,
        "yyyy-MM-dd HH:mm:ss",
        "dd/MM/yyyy"
    ) else ""

    fun convertTimeStampToCreatedAt(timeStamp: Long): String {
        return DateTimeUtil.convertTimeStampToDate(
            timeStamp,
            "yyyy-MM-dd HH:mm:ss"
        )
    }

    fun getHHMM() = if (createdAt != null) DateTimeUtil.convertDate(
        createdAt!!,
        "yyyy-MM-dd HH:mm:ss",
        "HH:mm"
    ) else ""
}

data class UserChat(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = "",
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null
)

data class DummyResult(
    @Expose
    @SerializedName("id")
    var id: Int? = 0,
    @Expose
    @SerializedName("practice_id")
    var practiceId: Int? = null,
    @Expose
    @SerializedName("mode")
    var mode: Int? = null,
    @Expose
    @SerializedName("dummy_id")
    var dummyId: Int? = null,
    @Expose
    @SerializedName("start_time1")
    var startTime1: Long? = 0,
    @Expose
    @SerializedName("start_time2")
    var startTime2: Long? = null,
    @Expose
    @SerializedName("video_path")
    var videoPath: String? = null,
    @Expose
    @SerializedName("video_thumb")
    var videoThumb: String? = null,
    @Expose
    @SerializedName("data")
    var dataVideo: String? = null
)

object ChatType {
    const val MESSAGE = "message"
    const val USER_VIDEO = "user_video"
    const val EXERCISE = "exercise"
    const val USER_PROFILE = "user_profile"
}

enum class ChatSendStatus { IS_LOADING, SEND_SUCCESS, SEND_ERROR }