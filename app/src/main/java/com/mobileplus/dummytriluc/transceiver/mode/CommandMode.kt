package com.mobileplus.dummytriluc.transceiver.mode

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by KO Huyn on 19/06/2023.
 */
@Parcelize
enum class CommandMode(val mode: Int):Parcelable {
    UNDEFINE(0),
    REGISTER(9),
    FREE_FIGHT(5),
    ACCORDING_LED(4),
    LESSON(6),
    FIRST_CONNECT(8),
    CONNECT(0),
    END(3),
    FINISH(2),
    COACH(1),
    CHALLENGE(7),
    SESSION(10),
    UPDATE_FIRM_WARE(11),
    DISCONNECT(12),
    CHANGE_PRESSURE(13),
    PLAY_WITH_MUSIC(14),
    WEAK_UP(15),
    LESSON_NEXT(16),
    RELAX(17),
    RELAX_WIFE(18),
    RELAX_HUSBAND(19),
    RELAX_LOVE_ENEMY(20),
    RELAX_EX(21),
    RELAX_BOSS(22);

    fun isShowTimeOut(): Boolean {
        return listOf(FREE_FIGHT, ACCORDING_LED).contains(this)
    }

    companion object {
        fun getOrNull(mode: Int?): CommandMode? {
            return values().find { it.mode == mode }
        }
    }
}