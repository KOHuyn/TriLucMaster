package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose

data class DataPunchResponse(
    @Expose
    val chart: MutableList<ItemPunch> = mutableListOf(),
    @Expose
    val time: String
)

data class ItemPunch(
    @Expose
    val title: String,
    @Expose
    val score: Int
)