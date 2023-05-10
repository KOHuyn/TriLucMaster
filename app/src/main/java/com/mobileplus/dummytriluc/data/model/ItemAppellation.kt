package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ItemAppellation(
    @Expose
    val title: String,
    @Expose
    @SerializedName("avatar_path")
    val icon: String
)