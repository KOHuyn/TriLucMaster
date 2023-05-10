package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.mobileplus.dummytriluc.data.model.ItemChart

data class DataPowerResponse(
    @Expose
    val chart: MutableList<ItemPower> = mutableListOf(),
    @Expose
    val time: String
)

data class ItemPower(
    @Expose
    val title: String,
    @Expose
    val score: Int,
    @Expose
    val detail: MutableList<ItemChart> = mutableListOf(),
)