package com.mobileplus.dummytriluc.ui.utils.extensions

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.databinding.LayoutHumanBinding
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
enum class BodyPosition(val key: String?, @StringRes val titleRes: Int) {
    LEFT_PUNCH("1", R.string.left_punch),
    CENTER_PUNCH("2", R.string.center_punch),
    RIGHT_PUNCH("3", R.string.right_punch),
    HOOK_PUNCH("4", R.string.hook_punch);

    companion object {
        fun getType(position: String?): BodyPosition? {
            return values().find { it.key == position }
        }
    }
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
            groupPosition.show()
            listOf(
                positionBodyBottom,
                positionBodyLeft,
                positionBodyCenter,
                positionBodyRight
            ).forEach { it.isVisible = false }
            when (data.position) {
                BodyPosition.HOOK_PUNCH.key -> {
                    positionBodyBottom.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BodyPosition.LEFT_PUNCH.key->{
                    positionBodyLeft.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BodyPosition.CENTER_PUNCH.key->{
                    positionBodyCenter.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                BodyPosition.RIGHT_PUNCH.key->{
                    positionBodyRight.setTouchPractice(
                        data.force,
                        data.onTarget
                    )
                }
                else -> {
                }
            }
        }
    }

    fun setPosColorHuman(layoutHuman: LayoutHumanBinding, score: Int, key: String?) {
        val typePos = BodyPosition.values().find { it.key == key }
        with(layoutHuman) {
            when {
                PowerLevel.LOW <= score && score < PowerLevel.MEDIUM -> {
                    when (typePos) {
                        BodyPosition.CENTER_PUNCH -> {
                            bodyCenter.setImageResource(R.drawable.img_body_center_green)
                        }
                        BodyPosition.HOOK_PUNCH -> {
                            bodyBottom.setImageResource(R.drawable.img_body_top_green)
                        }
                        BodyPosition.LEFT_PUNCH -> {
                            bodyLeft.setImageResource(R.drawable.img_body_left_green)
                        }
                        BodyPosition.RIGHT_PUNCH -> {
                            bodyRight.setImageResource(R.drawable.img_body_right_green)
                        }
                        else -> {
                            logErr("setPosColorHuman ERROR TYPE")
                        }
                    }
                }
                PowerLevel.MEDIUM < score && score < PowerLevel.HIGH -> {
                    when (typePos) {
                        BodyPosition.CENTER_PUNCH -> {
                            bodyCenter.setImageResource(R.drawable.img_body_center_orange)
                        }
                        BodyPosition.HOOK_PUNCH -> {
                            bodyBottom.setImageResource(R.drawable.img_body_top_orange)
                        }
                        BodyPosition.LEFT_PUNCH -> {
                            bodyLeft.setImageResource(R.drawable.img_body_left_orange)
                        }
                        BodyPosition.RIGHT_PUNCH -> {
                            bodyRight.setImageResource(R.drawable.img_body_right_orange)
                        }
                        else -> {
                            logErr("setPosColorHuman ERROR TYPE")
                        }
                    }
                }
                score > PowerLevel.HIGH -> {
                    when (typePos) {
                        BodyPosition.CENTER_PUNCH -> {
                            bodyCenter.setImageResource(R.drawable.img_body_center_red)
                        }
                        BodyPosition.HOOK_PUNCH -> {
                            bodyBottom.setImageResource(R.drawable.img_body_top_red)
                        }
                        BodyPosition.LEFT_PUNCH -> {
                            bodyLeft.setImageResource(R.drawable.img_body_left_red)
                        }
                        BodyPosition.RIGHT_PUNCH -> {
                            bodyRight.setImageResource(R.drawable.img_body_right_red)
                        }
                        else -> {
                            logErr("setPosColorHuman ERROR TYPE")
                        }
                    }
                }
            }
        }
    }

    fun setHighScoreWithPos(layoutHuman: LayoutHumanBinding, score: Int, key: String?) {
        val typePos = BodyPosition.values().find { it.key == key }
        with(layoutHuman) {
            groupHighScore.show()
            when (typePos) {
                BodyPosition.CENTER_PUNCH -> {
                    scoreBodyCenter.setHighScore(score)
                }
                BodyPosition.HOOK_PUNCH -> {
                    scoreBodyBottom.setHighScore(score)
                }
                BodyPosition.LEFT_PUNCH -> {
                    scoreBodyLeft.setHighScore(score)
                }
                BodyPosition.RIGHT_PUNCH -> {
                    scoreBodyRight.setHighScore(score)
                }
                else->{}
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
    this.isVisible = true
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
        this.isVisible = false
    }, 1000)
}

private fun TextView.setHighScore(scoreHigh: Int) {
    this.text = scoreHigh.toString()
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
