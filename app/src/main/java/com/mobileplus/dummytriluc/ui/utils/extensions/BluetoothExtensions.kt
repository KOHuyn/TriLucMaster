package com.mobileplus.dummytriluc.ui.utils.extensions

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.utils.ext.invisible
import com.utils.ext.show
import kotlinx.android.synthetic.main.layout_human.view.*
import java.util.*
import java.util.concurrent.TimeUnit


object PowerLevel {
    const val LOW = 0
    const val MEDIUM = 500
    const val HIGH = 1000
}

enum class BlePosition(val key: String, @StringRes val titleRes: Int) {
    LEFT_CHEEK("1", R.string.left_cheek),
    FACE("2", R.string.face),
    RIGHT_CHEEK("3", R.string.right_cheek),
    LEFT_CHEST("4", R.string.left_chest),
    RIGHT_CHEST("5", R.string.right_chest),
    ABDOMEN_UP("6", R.string.abdomen_up),
    LEFT_ABDOMEN("7", R.string.left_abdomen),
    ABDOMEN("8", R.string.abdomen),
    RIGHT_ABDOMEN("9", R.string.right_abdomen),
    LEFT_LEG("a", R.string.left_leg),
    RIGHT_LEG("b", R.string.right_leg),
    ;

    val title: String get() = loadStringRes(titleRes)
}

object BlePositionUtils {

    fun setCallbackBleDataForce(layoutHuman: View, data: DataBluetooth) {
        with(layoutHuman) {
            layoutPositionScore.show()
            when (data.position) {
                BlePosition.FACE.key, BlePosition.LEFT_CHEEK.key, BlePosition.RIGHT_CHEEK.key -> {
                    positionHead.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BlePosition.LEFT_CHEST.key -> {
                    positionLeftHand.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BlePosition.RIGHT_CHEST.key -> {
                    positionRightHand.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BlePosition.ABDOMEN_UP.key, BlePosition.LEFT_ABDOMEN.key, BlePosition.ABDOMEN.key, BlePosition.RIGHT_ABDOMEN.key -> {
                    positionAbdomen.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BlePosition.LEFT_LEG.key -> {
                    positionLegLeft.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BlePosition.RIGHT_LEG.key -> {
                    positionLegRight.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                else -> {
                }
            }
        }
    }

    fun setPosColorHuman(layoutHuman: View, score: Int, typePos: BlePosition) {
        with(layoutHuman) {
            when {
                PowerLevel.LOW <= score && score < PowerLevel.MEDIUM -> {
                    when (typePos) {
                        BlePosition.FACE -> {
                            humanHead.setImageResource(R.drawable.human_head_green)
                        }
                        BlePosition.LEFT_CHEST -> {
                            humanChestLeft.setImageResource(R.drawable.human_chest_left_green)
                        }
                        BlePosition.RIGHT_CHEST -> {
                            humanChestRight.setImageResource(R.drawable.human_chest_right_green)
                        }
                        BlePosition.ABDOMEN -> {
                            humanAbdomen.setImageResource(R.drawable.human_abdomen_green)
                        }
                        BlePosition.LEFT_LEG -> {
                            humanLegLeft.setImageResource(R.drawable.human_leg_left_green)
                        }
                        BlePosition.RIGHT_LEG -> {
                            humanLegRight.setImageResource(R.drawable.human_leg_right_green)
                        }
                        else -> {
                            logErr("setPosColorHuman ERROR TYPE")
                        }
                    }
                }
                PowerLevel.MEDIUM < score && score < PowerLevel.HIGH -> {
                    when (typePos) {
                        BlePosition.FACE -> {
                            humanHead.setImageResource(R.drawable.human_head_orange)
                        }
                        BlePosition.LEFT_CHEST -> {
                            humanChestLeft.setImageResource(R.drawable.human_chest_left_orange)
                        }
                        BlePosition.RIGHT_CHEST -> {
                            humanChestRight.setImageResource(R.drawable.human_chest_right_orange)
                        }
                        BlePosition.ABDOMEN -> {
                            humanAbdomen.setImageResource(R.drawable.human_abdomen_orange)
                        }
                        BlePosition.LEFT_LEG -> {
                            humanLegLeft.setImageResource(R.drawable.human_leg_left_orange)
                        }
                        BlePosition.RIGHT_LEG -> {
                            humanLegRight.setImageResource(R.drawable.human_leg_right_orange)
                        }
                        else -> {
                            logErr("setPosColorHuman ERROR TYPE")
                        }
                    }
                }
                score > PowerLevel.HIGH -> {
                    when (typePos) {
                        BlePosition.FACE -> {
                            humanHead.setImageResource(R.drawable.human_head_red)
                        }
                        BlePosition.LEFT_CHEST -> {
                            humanChestLeft.setImageResource(R.drawable.human_chest_left_red)
                        }
                        BlePosition.RIGHT_CHEST -> {
                            humanChestRight.setImageResource(R.drawable.human_chest_right_red)
                        }
                        BlePosition.ABDOMEN -> {
                            humanAbdomen.setImageResource(R.drawable.human_abdomen_red)
                        }
                        BlePosition.LEFT_LEG -> {
                            humanLegLeft.setImageResource(R.drawable.human_leg_left_red)

                        }
                        BlePosition.RIGHT_LEG -> {
                            humanLegRight.setImageResource(R.drawable.human_leg_right_red)
                        }
                        else -> {
                            logErr("setPosColorHuman ERROR TYPE")
                        }
                    }
                }
            }
        }
    }

    fun setHighScoreWithPos(layoutHuman: View, score: Int, typePos: BlePosition) {
        with(layoutHuman) {
            layoutPositionScoreHigh.show()
            when (typePos.key) {
                BlePosition.FACE.key -> {
                    positionHeadHighScore.run {
                        setHighScore(score, BlePosition.FACE)
                    }
                }
                BlePosition.LEFT_CHEST.key -> {
                    positionLeftChestHighScore.run {
                        setHighScore(score, BlePosition.LEFT_CHEST)
                    }
                }
                BlePosition.RIGHT_CHEST.key -> {
                    positionRightChestHighScore.run {
                        setHighScore(score, BlePosition.RIGHT_CHEST)
                    }
                }
                BlePosition.ABDOMEN.key -> {
                    positionAbdomenHighScore.run {
                        setHighScore(score, BlePosition.ABDOMEN)
                    }
                }
                BlePosition.LEFT_LEG.key -> {
                    positionLegLeftHighScore.run {
                        setHighScore(score, BlePosition.LEFT_LEG)
                    }
                }
                BlePosition.RIGHT_LEG.key -> {
                    positionLegRightHighScore.run {
                        setHighScore(score, BlePosition.RIGHT_LEG)
                    }
                }
            }
        }
    }

    fun findTitleWithKey(key: String?): String {
        return when (key) {
            BlePosition.LEFT_CHEEK.key -> BlePosition.LEFT_CHEEK.title
            BlePosition.RIGHT_CHEEK.key -> BlePosition.RIGHT_CHEEK.title
            BlePosition.FACE.key -> BlePosition.FACE.title
            BlePosition.LEFT_CHEST.key -> BlePosition.LEFT_CHEST.title
            BlePosition.RIGHT_CHEST.key -> BlePosition.RIGHT_CHEST.title
            BlePosition.ABDOMEN_UP.key -> BlePosition.ABDOMEN_UP.title
            BlePosition.LEFT_ABDOMEN.key -> BlePosition.LEFT_ABDOMEN.title
            BlePosition.ABDOMEN.key -> BlePosition.ABDOMEN.title
            BlePosition.RIGHT_ABDOMEN.key -> BlePosition.RIGHT_ABDOMEN.title
            BlePosition.LEFT_LEG.key -> BlePosition.LEFT_LEG.title
            BlePosition.RIGHT_LEG.key -> BlePosition.RIGHT_LEG.title
            else -> {
                loadStringRes(R.string.error_unknown_position_data_ble)
            }
        }
    }

    fun findBlePositionWithKey(key: String?): BlePosition {
        return when (key) {
            BlePosition.LEFT_CHEEK.key -> BlePosition.LEFT_CHEEK
            BlePosition.RIGHT_CHEEK.key -> BlePosition.RIGHT_CHEEK
            BlePosition.FACE.key -> BlePosition.FACE
            BlePosition.LEFT_CHEST.key -> BlePosition.LEFT_CHEST
            BlePosition.RIGHT_CHEST.key -> BlePosition.RIGHT_CHEST
            BlePosition.ABDOMEN_UP.key -> BlePosition.ABDOMEN_UP
            BlePosition.LEFT_ABDOMEN.key -> BlePosition.LEFT_ABDOMEN
            BlePosition.ABDOMEN.key -> BlePosition.ABDOMEN
            BlePosition.RIGHT_ABDOMEN.key -> BlePosition.RIGHT_ABDOMEN
            BlePosition.LEFT_LEG.key -> BlePosition.LEFT_LEG
            BlePosition.RIGHT_LEG.key -> BlePosition.RIGHT_LEG
            else -> BlePosition.FACE
        }
    }

    fun getListBle(isDetailPos: Boolean = true): MutableList<BlePosition> {
        val items = mutableListOf<BlePosition>()
        if (isDetailPos) {
            items.add(BlePosition.LEFT_CHEEK)
            items.add(BlePosition.RIGHT_CHEEK)
        }
        items.add(BlePosition.FACE)
        items.add(BlePosition.LEFT_CHEST)
        items.add(BlePosition.RIGHT_CHEST)
        if (isDetailPos) {
            items.add(BlePosition.ABDOMEN_UP)
            items.add(BlePosition.LEFT_ABDOMEN)
            items.add(BlePosition.RIGHT_ABDOMEN)
        }
        items.add(BlePosition.ABDOMEN)
        items.add(BlePosition.LEFT_LEG)
        items.add(BlePosition.RIGHT_LEG)
        return items
    }
}

private fun TextView.setTouchPractice(force: Float?, onTarget: Int?) {
    this.text = force?.toInt().toString()
    if (onTarget != null) {
        if (onTarget == 1) {
            this.setBackgroundResource(R.drawable.ic_touch_green)
        } else {
            this.setBackgroundResource(R.drawable.ic_touch_yellow)
        }
    } else {
        this.setBackgroundResource(R.drawable.ic_touch_green)
    }
    postDelayed({
        this.text = ""
        this.setBackgroundResource(R.drawable.ic_touch_red)
    }, 1000)
}

private fun TextView.setHighScore(scoreHigh: Int, typePos: BlePosition) {
    when (typePos) {
        BlePosition.FACE -> {
            this.text = scoreHigh.toInt().toString()
        }
        BlePosition.LEFT_CHEST -> {
            this.text = scoreHigh.toInt().toString()
        }
        BlePosition.RIGHT_CHEST -> {
            this.text = scoreHigh.toInt().toString()
        }
        BlePosition.ABDOMEN -> {
            this.text = scoreHigh.toInt().toString()
        }
        BlePosition.LEFT_LEG -> {
            this.text = scoreHigh.toInt().toString()
        }
        BlePosition.RIGHT_LEG -> {
            this.text = scoreHigh.toInt().toString()
        }
        else -> {
            logErr("setPosColorHuman ERROR TYPE")
        }
    }
}


private const val REQUEST_PERMISSION_BLE_SCAN = 101

fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = (properties and property) > 0

fun ByteArray.toHex() = joinToString("") { String.format("%02X", (it.toInt() and 0xff)) }

/**
 * Returns `true` if connection state is [CONNECTED][RxBleConnection.RxBleConnectionState.CONNECTED].
 */

/**
 * Helper functions to show BleScanException error messages as toasts.
 */

/**
 * Mapping of exception reasons to error string resource ids. Add new mappings here.
 */


private fun Activity.getScanThrottleErrorMessage(retryDate: Date?): String =
    with(StringBuilder(loadStringRes(R.string.error_undocumented_scan_throttle))) {
        retryDate?.let { date ->
            String.format(
                Locale.getDefault(),
                loadStringRes(R.string.error_undocumented_scan_throttle_retry),
                date.secondsUntil
            ).let { append(it) }
        }
        toString()
    }

private val Date.secondsUntil: Long
    get() = TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis())
