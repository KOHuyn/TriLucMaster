package com.mobileplus.dummytriluc.data.response
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("content")
    val content: String? = null,
    @Expose
    @SerializedName("image")
    val image: String? = null,
    @Expose
    @SerializedName("summary")
    val summary: String? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("link_share")
    val linkShare: String? = null,
)