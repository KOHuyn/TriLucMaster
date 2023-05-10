package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ItemDiscipleGroup(
    @Expose
    var id: Int? = null,
    @SerializedName("title")
    @Expose
    var name: String? = null,
    @Expose
    @SerializedName("room_id")
    val roomId:Int?= null,
    @Expose
    @SerializedName("room_key_id")
    var roomKeyId: String? = null,
    @Expose
    @SerializedName("thanh_vien_count")
    var discipleCount: Int? = null,
    var isChecked: Boolean = false
)