package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose

/**
 * Created by KOHuyn on 2/25/2021
 */
data class NotificationDetailApiResponse(
    @Expose
    val id: Int? = null,
    @Expose
    val title: String? = null,
    @Expose
    val content: String? = null,
)