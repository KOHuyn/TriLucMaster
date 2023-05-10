package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CreateCourseRequest(
    @Expose
    @SerializedName("title")
    var title: String? = null,
    @Expose
    @SerializedName("status")
    var status: Int? = null,
    @Expose
    @SerializedName("content")
    var content: String? = null,
//    @Expose
//    @SerializedName("note")
//    var note: String? = null,
    @Expose
    @SerializedName("image_path")
    var imagePath: String? = null,
    @Expose
    @SerializedName("subject_id")
    var subjectId: Int? = null,
    @Expose
    @SerializedName("practice_ids")
    var practiceIds: List<Int> = emptyList(),
){
    companion object {
        const val STATUS_ACTIVE = 1
        const val STATUS_INACTIVE = 2
    }

}