package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose

/**
 * Created by ThaiNV on 12/7/2020.
 */
data class ItemChart(
    @Expose
    val title: String? = null,
    @Expose
    val score: Float? = 5F,
    @Expose
    val max: Float? = 10f,
    @Expose
    val color: String? = null
) {
    val scoreChart: Float
        get() = if (score != null) {
            if (score < 0F) 0F else score
        } else 0F
    val titleZ: String get() = title ?: "--"
    val colorZ: String get() = color ?: "#0F1838"
}