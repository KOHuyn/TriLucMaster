package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DataSubmitPracticeResponse(
    @Expose
    @SerializedName("is_complete_level")
    val isCompleteLevel: Int? = null,
    @Expose
    @SerializedName("result_level_value")
    val resultLevelValue: String? = null,
    @Expose
    @SerializedName("score")
    val score: Double? = null,
    @Expose
    @SerializedName("result_id")
    val resultId: Int? = null
)