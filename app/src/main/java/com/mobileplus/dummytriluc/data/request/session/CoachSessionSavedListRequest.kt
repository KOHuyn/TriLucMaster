package com.mobileplus.dummytriluc.data.request.session

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KOHuyn on 4/27/2021
 */
data class CoachSessionSavedListRequest(
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("practice_ids")
    val practiceIds: List<PracticeIdsRound> = emptyList()
){
    data class PracticeIdsRound(
        @Expose
        @SerializedName("id")
        val id: Int? = null,
        @Expose
        @SerializedName("round")
        val round: Int? = null,
    )
}