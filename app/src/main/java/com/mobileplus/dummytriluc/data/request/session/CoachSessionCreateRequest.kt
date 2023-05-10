package com.mobileplus.dummytriluc.data.request.session

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KOHuyn on 4/27/2021
 */
class CoachSessionCreateRequest(
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("practice_ids")
    val practiceIds: List<PracticeIdsRound>? = null,
    @Expose
    @SerializedName("user_ids")
    val userIds: List<Int>? = null,
    @Expose
    @SerializedName("class_id")
    val classId: Int? = null
) {
    data class PracticeIdsRound(
        @Expose
        @SerializedName("id")
        val id: Int? = null,
        @Expose
        @SerializedName("round")
        val round: Int? = null,
    )
}