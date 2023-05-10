package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.data.model.ChatType


/**
 * Created by KO Huyn on 1/6/2021.
 */
data class ChatSendRequest(
    @Expose
    @SerializedName("room_id")
    val roomId: Int,
    @Expose
    @SerializedName("message")
    val message: String = "",
    @Expose
    @SerializedName("message_type")
    val messageType: String = ChatType.MESSAGE,
    @Expose
    @SerializedName("message_object_id")
    val messageObjectId: Int? = null,
)