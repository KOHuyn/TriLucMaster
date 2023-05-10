package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name")
    @Expose
    var fullName: String? = null,
    @Expose
    var email: String? = null,
    @Expose
    var password: String? = null,
    @SerializedName("re_password")
    @Expose
    var rePassword: String? = null,
    @Expose
    var uuid: String? = null,
    @SerializedName("token_push")
    @Expose
    var tokenPush: String? = null,
    @Expose
    var platform: String = "ANDROID",
)