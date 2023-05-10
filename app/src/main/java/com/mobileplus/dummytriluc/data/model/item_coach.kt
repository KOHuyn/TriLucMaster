package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.PairResponse
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

/**
 * Created by KOHuyn on 3/12/2021
 */

interface BaseItemCoachGroup {
    fun getType(): Int

    companion object {
        const val TYPE_TIME = 1
        const val TYPE_PRACTICE = 2
        const val TYPE_SESSION = 3
        const val TYPE_FOLDER = 4
    }
}

data class ItemCoachGroupTime(val time: String) : BaseItemCoachGroup {
    override fun getType(): Int {
        return BaseItemCoachGroup.TYPE_TIME
    }
}

data class ItemCoachGroupLesson(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("image_path")
    val imagePath: String? = null,
    @Expose
    val title: String? = null,
    @Expose
    val content: String? = null,
    @Expose
    val type: String? = null,
    @Expose
    @SerializedName("assign_id")
    val assignId: Int? = null,
    @Expose
    @SerializedName("practice_id")
    val practiceId: Int? = null,
    @Expose
    @SerializedName("practices_count")
    val practicesCount: String? = null,
    @Expose
    @SerializedName("users_count")
    val usersCount: String? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    var isSelected: Boolean = false
) : BaseItemCoachGroup {
    companion object {
        const val TYPE_PRACTICE = "practice"
        const val TYPE_FOLDER = "folder"
        const val TYPE_SESSION = "session"
    }

    fun getUserCountt(): String? = if (usersCount == null) null else String.format(
        loadStringRes(R.string.value_exerciser),
        usersCount
    )

    fun getExerciseCount(): String? = if (practicesCount == null) null else String.format(
        loadStringRes(R.string.value_exercise),
        practicesCount
    )

    fun getDateCreate(): String? = if (createdAt != null) DateTimeUtil.convertDate(
        createdAt,
        DateTimeUtil.DATE_SERVER_UTC,
        DateTimeUtil.DATE_CALENDAR
    ) else null

    fun getHHmmCreate(): String? = if (createdAt != null) DateTimeUtil.convertDate(
        createdAt,
        DateTimeUtil.DATE_SERVER_UTC,
        "HH:mm"
    ) else null

    override fun getType(): Int {
        return when (type) {
            TYPE_PRACTICE -> BaseItemCoachGroup.TYPE_PRACTICE
            TYPE_FOLDER -> BaseItemCoachGroup.TYPE_FOLDER
            TYPE_SESSION -> BaseItemCoachGroup.TYPE_SESSION
            else -> BaseItemCoachGroup.TYPE_PRACTICE
        }
    }
}

data class ItemCoachPractice(
    @Expose
    val id: Int? = null,
    @Expose
    @SerializedName("image_path")
    val imagePath: String? = null,
    @Expose
    var title: String? = null,
    @Expose
    val type: String? = null,
    @Expose
    val content: String? = null,
    @Expose
    val subject: PairResponse? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("folder_id")
    val folderId: Int? = null,
    @Expose
    @SerializedName("video_path_origin")
    val videoPathOrigin: String? = null,
    var isSelected: Boolean = false,
    @Expose
    @SerializedName("round")
    var countRepeat: Int = 1
) {
    companion object {
        const val TYPE_PRACTICE = "practice"
        const val TYPE_FOLDER = "folder"
    }
}

data class ItemCoachDraftFolder(
    @Expose
    val id: Int? = null,
    @Expose
    val name: String? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("parent_id")
    val parentId: Int? = null,
)

data class ItemCoachSessionSaved(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("practices")
    val practices: List<Practices>? = emptyList()
) {
    data class Practices(
        @Expose
        @SerializedName("session_practice_list_id")
        val sessionPracticeListId: Int? = null,
        @Expose
        @SerializedName("practice_id")
        val practiceId: Int? = null,
    )
}

data class ItemCoachSessionOld(
    @Expose
    val id: Int? = null,
    @Expose
    val title: String? = null,
    @Expose
    @SerializedName("practices_count")
    val practicesCount: Int? = null,
    @Expose
    @SerializedName("users_count")
    val usersCount: Int? = null,
) {
    fun getAllUser(): String? = if (usersCount != null) String.format(
        loadStringRes(R.string.value_of_student), usersCount
    ) else null

    fun getAllPractice(): String? = if (practicesCount != null) String.format(
        loadStringRes(R.string.value_exercise),
        practicesCount
    ) else null
}
//
//data class ItemCoachGroupSimple(
//    @Expose
//    val id: Int? = null,
//    @Expose
//    val title: String? = null,
//    @Expose
//    @SerializedName("thanh_vien_count")
//    val memberCount: Int? = null,
//    var isSelected: Boolean = false
//)