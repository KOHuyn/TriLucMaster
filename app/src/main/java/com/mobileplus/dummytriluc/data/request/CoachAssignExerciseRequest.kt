package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KOHuyn on 3/15/2021
 */
data class CoachAssignExerciseRequest(
    @Expose
    @SerializedName("practice_id")
    var practiceId: String? = null,
    @Expose
    @SerializedName("assign_object_id")
    var assignObjectId: Int? = null,
    @Expose
    @SerializedName("assign_type")
    var assignType: String? = null,
) {
    companion object {
        const val CLASS = "class"
        const val STUDENT = "student"
        const val FOLDER = "folder"
    }
}