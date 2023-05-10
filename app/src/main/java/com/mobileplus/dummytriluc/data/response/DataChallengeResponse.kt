package com.mobileplus.dummytriluc.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.data.model.BaseItemChallenge
import com.mobileplus.dummytriluc.data.model.ItemAppellation
import com.mobileplus.dummytriluc.data.model.ItemChallengeAchievement


/**
 * Created by KO Huyn on 12/22/2020.
 */
data class DataChallengeResponse(
    @Expose
    @SerializedName("MY_REWARD")
    val myReward: MutableList<ItemAppellation>? = mutableListOf(),
    @Expose
    @SerializedName("CHALLENGES_JOIN")
    val challengesJoin: MutableList<ItemChallengeAchievement>? = mutableListOf(),
    @Expose
    @SerializedName("CHALLENGES_PUBLISH")
    val challengePublish: MutableList<ItemChallengeAchievement>? = mutableListOf(),
) {
    fun isEmpty(): Boolean {
        return myReward?.isEmpty() ?: true && challengesJoin?.isEmpty() ?: true && challengePublish?.isEmpty() ?: true
    }
}