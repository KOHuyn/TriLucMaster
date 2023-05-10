package com.mobileplus.dummytriluc.data.model

import androidx.annotation.ColorInt

data class ItemProgressPower(
    val progress: Int,
    @ColorInt val colorHex: Int,
    val namePosition: String
)