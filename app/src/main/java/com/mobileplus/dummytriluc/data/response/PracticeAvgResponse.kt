package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PracticeAvgResponse(
    @Expose
    @SerializedName("avg_power")
    val avgPower: Int? = null,
    @Expose
    @SerializedName("avg_hit")
    val avgHit: Int? = null,
)