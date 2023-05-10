package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.service.MessageService
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil

/**
 * Created by KOHuyn on 2/22/2021
 */
data class NotificationObjService(
    @Expose
    var id: Int = -1,
    @Expose
    @SerializedName("link_id")
    var linkId: Int? = -1,
    @Expose
    var type: String = "",
    @Expose
    var title: String = "",
    @Expose
    var content: String = "",
    @Expose
    var status: Int = 1,
    @Expose
    @SerializedName("data_json")
    var itemChat: ItemChatRoom? = null,
    @Expose
    @SerializedName("created_at")
    var createdAt: String? = null
) {
    fun getDateCreated() = if (createdAt != null) DateTimeUtil.convertDate(
        createdAt!!,
        "yyyy-MM-dd hh:mm:ss",
        "hh:mm dd/MM/yyyy"
    ) else null

    fun isRead() = status != 1
}