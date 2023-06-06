package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KO Huyn on 05/06/2023.
 */
data class CreateTargetRequest(
    @Expose
    @SerializedName("target_unit")
    val targetUnit: String? = null,
    @Expose
    @SerializedName("target_type")
    val targetType: String? = null,
    @Expose
    @SerializedName("target_point")
    val targetPoint: String? = null,
)