package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose

/**
 * Created by KOHuyn on 3/25/2021
 */
data class StatisticalCoachResponse(
    @Expose
    val total: Int? = null,
    @Expose
    val date: String? = null
)