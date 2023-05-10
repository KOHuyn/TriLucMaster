package com.mobileplus.dummytriluc.data.model

data class ItemLesson(
    val progress: Int = 0,
    val titleLesson: String = "",
    val timeLesson: String = "",
    val numberPunches: Int? = null,
    val numberKicks: Int? = null,
    val numberPower: Int? = null,
    val numberCal: Int? = null
)