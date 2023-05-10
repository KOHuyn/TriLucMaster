package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    @Expose
    var email: String? = null,
    @Expose
    var password: String? = null,
    @Expose
    var uuid: String? = null,
    @SerializedName("token_push")
    @Expose
    var tokenPush: String? = null,
    @Expose
    var platform: String= "ANDROID",
)