package com.mobileplus.dummytriluc.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.main.news.adapter.NewsFeedAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

data class ItemNewsFeed(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("object_type")
    val objectType: String? = null,
    @Expose
    @SerializedName("object_id")
    val objectId: Int? = null,
    @Expose
    @SerializedName("status")
    val status: Int? = null,
    @Expose
    @SerializedName("order")
    val order: Int? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("data")
    val data: JsonObject? = null
) {
    fun getType(): Int {
        return when (objectType) {
            "news" -> NewsFeedAdapter.TYPE_NEWS_FEED
            "challenges" -> NewsFeedAdapter.TYPE_NEWS_CHALLENGE
            else -> NewsFeedAdapter.TYPE_NEWS_FEED
        }
    }
}

data class DataNewsChallenge(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("challenges_type")
    val challengesType: String? = null,
    @Expose
    @SerializedName("win_count")
    val winCount: Int? = null,
    @Expose
    @SerializedName("image")
    val image: String? = null,
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("time_ago")
    val timeAgo: String? = null
) {
    fun getWinCountType(): String? = when (challengesType) {
        "power" -> "${winCount ?: 0} (${loadStringRes(R.string.punch)})"
        "speed" -> "${winCount ?: 0} (s)"
        "accuracy" -> "${winCount ?: 0} (%)"
        else -> null
    }
}


data class DataNewsFeed(
    @SerializedName("summary")
    @Expose
    val summary: String? = null,
    @SerializedName("image")
    @Expose
    val image: String? = null,
    @SerializedName("time_ago")
    @Expose
    val timeAgo: String? = null,
    @SerializedName("created_at")
    @Expose
    val createdAt: String? = null,
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null
)