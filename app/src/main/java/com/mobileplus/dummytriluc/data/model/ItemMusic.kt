package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KO Huyn on 19/06/2023.
 */
data class ItemMusic(
    @Expose @SerializedName("id") val id: Int? = null,
    @Expose @SerializedName("name") val name: String? = null,
    @Expose @SerializedName("data") val data: String? = null,
    @Expose @SerializedName("duration") val duration: Double? = null,
)