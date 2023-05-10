package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PairResponse(
    @Expose
    @SerializedName("id")
    val id:Int,
    @Expose
    @SerializedName("title")
    val title:String,
)