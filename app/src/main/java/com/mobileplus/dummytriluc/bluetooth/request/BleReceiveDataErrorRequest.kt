package com.mobileplus.dummytriluc.bluetooth.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BleReceiveDataErrorRequest(
    @Expose
    val total: Int? = null,
    @Expose
    @SerializedName("error")
    val errors: List<Int>? = null,
    @Expose
    @SerializedName("count_error")
    val countError: Int? = null
)