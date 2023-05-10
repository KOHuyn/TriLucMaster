package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CoachRegisterRequest(
    @Expose
    @SerializedName("full_name")
    var fullName: String? = null,
    @Expose
    @SerializedName("email")
    var email: String? = null,
    @Expose
    @SerializedName("phone")
    var phone: String? = null,
)