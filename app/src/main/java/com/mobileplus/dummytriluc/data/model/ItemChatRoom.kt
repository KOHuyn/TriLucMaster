package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by KO Huyn on 1/12/2021.
 */
data class ItemChatRoom(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("message")
    val message: String? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("room_id")
    val roomId: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("image")
    val image: String? = null,
    @Expose
    @SerializedName("room_key_id")
    val roomKeyId: String? = null,
    @Expose
    @SerializedName("object_type")
    val objectType: String? = null,
    @Expose
    @SerializedName("object_id")
    val objectId: Int? = null,
    @Expose
    @SerializedName("time_ago")
    val timeAgo: String? = null
) {
    companion object {
        const val TYPE_CLASS = "CLASS"
        const val TYPE_PRACTICE = "PRACTICE"
    }
}