package com.mobileplus.dummytriluc.data.response

import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.setDrawableStart
import com.utils.ext.setTextColorz
import com.utils.ext.toList

data class DataChallengeDetailResponse(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("description")
    val description: String? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null,
    @Expose
    @SerializedName("cover_path")
    val coverPath: String? = null,
    @Expose
    @SerializedName("content")
    val content: String? = null,
    @Expose
    @SerializedName("intro_video")
    val introVideo: String? = null,
    @Expose
    @SerializedName("event_type")
    val eventType: String? = null,
    @Expose
    @SerializedName("event_type_data")
    val eventTypeData: String? = null,
    @Expose
    @SerializedName("challenges_type")
    val challengesType: String? = null,
    @Expose
    @SerializedName("challenges_data")
    val challengesData: String? = null,
    @Expose
    @SerializedName("hitting_type")
    val hittingType: String? = null,
    @Expose
    @SerializedName("hit_data")
    val hitData: String? = "0",
    @Expose
    @SerializedName("hit_limit")
    val hitLimit: Int? = 0,
    @Expose
    @SerializedName("time_limit")
    val timeLimit: Int? = 0,
    @Expose
    @SerializedName("position_limit")
    val positionLimit: String? = null,
    @Expose
    @SerializedName("weight")
    val weight: Int? = 0,
    @Expose
    @SerializedName("min_power")
    val minPower: Int? = 0,
    @Expose
    @SerializedName("random_delay_time")
    val randomDelayTime: Int? = 0,
    @Expose
    @SerializedName("win_type")
    val winType: String? = null,
    @Expose
    @SerializedName("win_count")
    val winCount: Int? = null,
    @Expose
    @SerializedName("reward")
    val reward: Int? = null,
    @Expose
    @SerializedName("glory_level")
    val gloryLevel: Int? = null,
    @Expose
    @SerializedName("time_type")
    val timeType: Int? = null,
    @Expose
    @SerializedName("time_start")
    val timeStart: String? = null,
    @Expose
    @SerializedName("time_end")
    val timeEnd: String? = null,
    @Expose
    @SerializedName("challenges_type_text")
    val challengesTypeText: String? = null,
    @Expose
    @SerializedName("hitting_type_text")
    val hittingTypeText: String? = null,
    @Expose
    @SerializedName("new_join")
    val newJoin: List<NewJoin>? = listOf(),
    @Expose
    @SerializedName("joined")
    val joined: Boolean? = false,
    @Expose
    @SerializedName("number_people_join")
    val numberPeopleJoin: Int? = null,
    @Expose
    @SerializedName("data_rank")
    val dataRank: DataRank? = null,
    @Expose
    @SerializedName("data_practice")
    val dataPractice: DataPracticeChallenge? = null,
    @Expose
    @SerializedName("background_color")
    val backgroundColor: String? = "#0F1838",
    @Expose
    @SerializedName("link_share")
    val linkShare: String? = null,
    @Expose
    @SerializedName("challenges_joined")
    val challengesJoined: List<ChallengesJoined>? = listOf()
) {

    companion object {
        const val POWER: String = "power"
        const val SPEED: String = "speed"
        const val ACCURACY: String = "accuracy"
        const val FREEDOM: String = "freedom"
        const val LIMIT: String = "limit"
        const val RANDOM: String = "random"
        const val SAMPLE: String = "sample"
    }

    fun getHitDataBle(): String {
        return when (getTypeHitting()) {
            TypeDataChallenge.FREEDOM_TIME,
            TypeDataChallenge.FREEDOM_PUNCH,
            TypeDataChallenge.LIMIT_PUNCH,
            TypeDataChallenge.LIMIT_TIME,
            TypeDataChallenge.RANDOM -> "0"
            TypeDataChallenge.SAMPLE -> {
                var arrPos = "0"
                if (hitData != null) {
                    try {
                        val dataArr: List<DataBluetooth> = Gson().toList(hitData)
                        arrPos = ""
                        dataArr.forEach { arrPos += it.position }
                    } catch (e: Exception) {
                        e.logErr()
                        arrPos = "0"
                    }
                }
                return arrPos
            }
        }
    }

    fun getNumberPeopleJoin(): String? =
        if (numberPeopleJoin != null) "$numberPeopleJoin ${loadStringRes(R.string.people_join)}" else null

    fun getChallengeDataType() = if (challengesType != null) {
        when (challengesType) {
            POWER -> "${hitLimit ?: 0} (${loadStringRes(R.string.punch)})"
            SPEED -> "${timeLimit ?: 0} (s)"
            ACCURACY -> "${hitLimit ?: 0} (${loadStringRes(R.string.punch)})"
            else -> "----"
        }
    } else null

    fun getTypeHitting(): TypeDataChallenge {
        return when (hittingType) {
            FREEDOM -> {
                when (challengesType) {
                    POWER ->
                        TypeDataChallenge.FREEDOM_PUNCH
                    SPEED -> TypeDataChallenge.FREEDOM_TIME
                    ACCURACY -> TypeDataChallenge.RANDOM
                    else -> TypeDataChallenge.RANDOM
                }
            }
            LIMIT -> when (challengesType) {
                POWER ->
                    TypeDataChallenge.LIMIT_PUNCH
                SPEED -> TypeDataChallenge.LIMIT_TIME
                ACCURACY -> TypeDataChallenge.RANDOM
                else -> TypeDataChallenge.RANDOM
            }
            TypeDataChallenge.RANDOM.value -> TypeDataChallenge.RANDOM
            TypeDataChallenge.SAMPLE.value -> TypeDataChallenge.SAMPLE
            else -> TypeDataChallenge.RANDOM
        }
    }

    enum class TypeDataChallenge(val value: String, val dataBle: Int) {
        FREEDOM_PUNCH(FREEDOM, 1),
        FREEDOM_TIME(FREEDOM, 1),
        LIMIT_PUNCH(LIMIT, 1),
        LIMIT_TIME(LIMIT, 1),
        RANDOM(DataChallengeDetailResponse.RANDOM, 2),
        SAMPLE(DataChallengeDetailResponse.SAMPLE, 3),
    }
}

data class DataPracticeChallenge(
    @Expose
    @SerializedName("start_time2")
    val start_time2: Long?,
    @Expose
    @SerializedName("video_path")
    val video_path: String?,
    @Expose
    @SerializedName("data")
    val data: String?,
)

data class ChallengesJoined(
    @Expose
    @SerializedName("challenges_id")
    val challengesId: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("point")
    val point: Int? = null,
    @Expose
    @SerializedName("point_bonus")
    val pointBonus: Int? = null,
    @Expose
    @SerializedName("yesterday_rank")
    val yesterdayRank: Any? = null,
    @Expose
    @SerializedName("rank")
    val rank: Int? = null,
    @Expose
    @SerializedName("rank_up")
    val rankUp: Int? = null,
    @Expose
    @SerializedName("user")
    val user: UserInfoChallenge? = null
)

data class DataRank(
    @Expose
    @SerializedName("point")
    val point: Int? = null,
    @Expose
    @SerializedName("sub_point")
    val subPoint: Int? = null,
    @Expose
    @SerializedName("time_duration")
    val timeDuration: Long? = null,
    @Expose
    @SerializedName("video_path")
    val videoPath: String? = null,
    @Expose
    @SerializedName("status")
    val status: Int? = null,
    @Expose
    @SerializedName("win_status")
    val winStatus: Any? = null,
    @Expose
    @SerializedName("win_rank")
    val winRank: Int? = null,
    @Expose
    @SerializedName("note")
    val note: String? = null,
    @Expose
    @SerializedName("position")
    val position: List<Position>? = listOf(),
    @Expose
    @SerializedName("my_rank")
    val myRank: Int? = null,
    @Expose
    @SerializedName("my_rank_up")
    val myRankUp: Int? = null
) {
    fun setRankTop(txt: TextView) {
        if (myRank == 1) {
            txt.text = ""
        } else {
            txt.text = String.format("#%d", myRank ?: 0)
        }
        when (myRank) {
            1 -> {
                txt.setBackgroundResource(R.drawable.background_top1_ranking)
            }
            else -> {
                txt.setBackgroundResource(R.drawable.gradient_orange)
            }
        }
    }

    fun setRankingUp(txt: TextView) {
        when {
            myRankUp == 0 -> {
                txt.run {
                    text = " "
                    setDrawableStart(R.drawable.ic_rank_normal)
                }
            }
            myRankUp ?: 0 > 0 -> {
                txt.run {
                    text = "+$myRankUp"
                    setDrawableStart(R.drawable.ic_rank_up)
                    setTextColorz(R.color.clr_green)
                }
            }
            myRankUp ?: 0 < 0 -> {
                txt.run {
                    text = "$myRankUp"
                    setDrawableStart(R.drawable.ic_rank_down)
                    setTextColorz(R.color.clr_red)
                }
            }
        }
    }
}

data class NewJoin(
    @Expose
    @SerializedName("challenges_id")
    val challengesId: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("point")
    val point: Int? = null,
    @Expose
    @SerializedName("yesterday_rank")
    val yesterdayRank: Any? = null,
    @Expose
    @SerializedName("user")
    val user: User? = null
)


data class Position(
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("color")
    val color: String? = "#0F1838",
    @Expose
    @SerializedName("score")
    val score: Int? = null
)


data class User(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null
)


data class UserInfoChallenge(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val fullName: String? = null,
    @Expose
    @SerializedName("avatar_path")
    val avatarPath: String? = null
)