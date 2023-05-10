package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KO Huyn on 1/19/2021.
 */
class ItemDiscipleWaiting(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("student_id")
    val studentId: Int? = null,
    @Expose
    @SerializedName("master_id")
    val masterId: Int? = null,
    @Expose
    @SerializedName("message")
    val message: String? = null,
    @Expose
    @SerializedName("status")
    val status: Int? = null,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @Expose
    @SerializedName("time_ago")
    val timeAgo: String? = null,
    @Expose
    @SerializedName("nguoi_tao")
    val userCreated: UserCreatedDisciple? = null
)

data class UserCreatedDisciple(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null
)