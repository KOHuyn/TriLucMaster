package com.mobileplus.dummytriluc.data.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

interface BaseItemChallenge {
    fun typeItemChallenge(): Int
}

data class ItemChallengeAchievement(
    @Expose
    val id:Int,
    @Expose
    @SerializedName("avatar_path")
    val icon: String?,
    @Expose
    val title: String?,
    @Expose
    val description: String?,
) : BaseItemChallenge {
    override fun typeItemChallenge(): Int = TypeChallenge.TYPE_ACHIEVEMENT
}

data class ItemChallengeMatch(
    @Expose
    val avatar: String,
    @Expose
    val title: String,
    @Expose
    val typeMatch: String,
    @Expose
    val author: String,
    @Expose
    val isAccept: Boolean,
) : BaseItemChallenge {
    override fun typeItemChallenge(): Int = TypeChallenge.TYPE_MATCH
    fun getAcceptMatch(): String =
        if (isAccept) loadStringRes(R.string.accepted) else loadStringRes(R.string.not_yet_accepted)

    fun getColorAccept(): String = if (isAccept) "#00BD13" else "#BD0000"
}

object TypeChallenge {
    const val TYPE_ACHIEVEMENT = 1
    const val TYPE_MATCH = 2
}