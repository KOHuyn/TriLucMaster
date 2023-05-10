package com.mobileplus.dummytriluc.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SocialLoginRequest(
    @Expose
    var type: String?= null,
    @Expose
    var uuid: String?= null,
    @SerializedName("token_push")
    @Expose
    var tokenPush: String?= null,
    @SerializedName("social_token")
    @Expose
    var socialToken: String?= null,
    @Expose
    var platform: String = "ANDROID",
)