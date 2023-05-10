package com.mobileplus.dummytriluc.ui.main.practice.dialog.adapter

import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePositionUtils
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.*
import com.utils.ext.invisible
import kotlinx.android.synthetic.main.item_confirm_practice_index.view.*
import kotlinx.android.synthetic.main.item_confirm_practice_time.view.*
import kotlin.math.roundToInt

/**
 * Created by KOHuyn on 3/2/2021
 */
class AdapterIndexPractice : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<BaseItemIndexPractice>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return items[position].getTypeItem().viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == TypeIndexPractice.TIME.viewType)
            BaseViewHolder(parent.inflateExt(R.layout.item_confirm_practice_time))
        else BaseViewHolder(parent.inflateExt(R.layout.item_confirm_practice_index))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (getItemViewType(position) == TypeIndexPractice.TIME.viewType) {
            with(holder.itemView) {
                val item = items[position] as ItemTimeIndexPractice
                txtConfirmPracticeTime.text = item.time
                txtHeaderTextPractice.setTextNotNull(item.title)
            }
        } else {
            with(holder.itemView) {
                val item = items[position] as ItemIndexPractice
                timeLineConfirm.setVisibility(itemCount > 1)
                if (item.type != TypeIndexPractice.INDEX_START) {
                    viewSubmitPracticeTop.show()
                } else {
                    viewSubmitPracticeTop.invisible()
                }
                viewSubmitPracticeBottom.setVisibility(item.type != TypeIndexPractice.INDEX_END)
                imgPositionSubmitPractice.setImageResource(item.getIcon())
                txtTurnSubmitPractice.text = BlePositionUtils.findTitleWithKey(item.position)
                txtProgressSubmitPractice.text =
                    "${item.currPunch?.roundToInt() ?: 0}/${item.defaultPunch?.roundToInt() ?: 0}"
                prgConfirmPractice.progress = item.getProgress()
                val layers = prgConfirmPractice.progressDrawable as LayerDrawable
                layers.getDrawable(0).applyColorFilter(Color.parseColor("#2A2E43"))
                layers.getDrawable(1)
                    .applyColorFilter(Color.parseColor(if (item.isPassIndex()) "#00CF21" else "#E32828"))
            }
        }
    }

    override fun getItemCount(): Int = items.size
}

interface BaseItemIndexPractice {
    fun getTypeItem(): TypeIndexPractice

    companion object {
        fun transformToItemIndex(
            data: List<BluetoothResponse>,
            sampleResponse: List<DataBluetooth>,
            level: Float = 1F
        ): MutableList<BaseItemIndexPractice> {

            fun getType(pos: Int, totalSize: Int): TypeIndexPractice {
                return when (pos) {
                    0 -> TypeIndexPractice.INDEX_START
                    totalSize - 1 -> TypeIndexPractice.INDEX_END
                    else -> TypeIndexPractice.INDEX_NORMAL
                }
            }

            data class SampleResponse(var isPass: Boolean, val data: DataBluetooth)

            val items: MutableList<BaseItemIndexPractice> = mutableListOf()
            data.forEach { bleArr ->
                val samplesPractice: List<SampleResponse> =
                    sampleResponse.map {
                        val newForce = if (it.force != null) it.force!! * level else 0F
                        it.force = newForce
                        SampleResponse(false, it)
                    }
                items.add(ItemTimeIndexPractice(DateTimeUtil.convertTimeStampToHHMM(bleArr.startTime1)))
                for (i in bleArr.data.indices) {
                    val ble = bleArr.data[i]
                    if (ble?.onTarget == 1) {
                        sample@ for (sample in samplesPractice) {
                            if (!sample.isPass)
                                if (ble.position == sample.data.position) {
                                    items.add(
                                        ItemIndexPractice(
                                            i,
                                            ble.position,
                                            ble.force,
                                            sample.data.force,
                                            true,
                                            getType(i, bleArr.data.size)
                                        )
                                    )
                                    sample.isPass = true
                                    break@sample
                                }
                        }
                    } else {
                        items.add(
                            ItemIndexPractice(
                                i,
                                ble?.position,
                                ble?.force,
                                null,
                                false,
                                getType(i, bleArr.data.size)
                            )
                        )
                    }
                }
            }
            return items
        }

        fun transformToItemIndex(data: List<BluetoothResponse>): MutableList<BaseItemIndexPractice> {
            val items: MutableList<BaseItemIndexPractice> = mutableListOf()
            data.forEach { bleArr ->
                items.add(ItemTimeIndexPractice(DateTimeUtil.convertTimeStampToHHMM(bleArr.startTime1)))
                for (i in bleArr.data.indices) {
                    val ble = bleArr.data[i]
                    fun getType(pos: Int, totalSize: Int): TypeIndexPractice {
                        return when (pos) {
                            0 -> TypeIndexPractice.INDEX_START
                            totalSize - 1 -> TypeIndexPractice.INDEX_END
                            else -> TypeIndexPractice.INDEX_NORMAL
                        }
                    }
                    if (ble != null) {
                        items.add(
                            ItemIndexPractice(
                                i,
                                ble.position,
                                ble.force,
                                50F,
                                ble.onTarget == 1,
                                getType(i, bleArr.data.size)
                            )
                        )
                    }
                }
            }
            return items
        }

        fun transformToItemIndex(
            data: List<BluetoothResponse>,
            arrTitle: MutableList<Pair<Int?, String?>>
        ): MutableList<BaseItemIndexPractice> {
            val items: MutableList<BaseItemIndexPractice> = mutableListOf()
            data.forEach { bleArr ->
                items.add(ItemTimeIndexPractice(DateTimeUtil.convertTimeStampToHHMM(bleArr.startTime1),arrTitle.find { it.first == bleArr.practiceId }?.second))
                for (i in bleArr.data.indices) {
                    val ble = bleArr.data[i]
                    fun getType(pos: Int, totalSize: Int): TypeIndexPractice {
                        return when (pos) {
                            0 -> TypeIndexPractice.INDEX_START
                            totalSize - 1 -> TypeIndexPractice.INDEX_END
                            else -> TypeIndexPractice.INDEX_NORMAL
                        }
                    }
                    if (ble != null) {
                        items.add(
                            ItemIndexPractice(
                                i,
                                ble.position,
                                ble.force,
                                50F,
                                ble.onTarget == 1,
                                getType(i, bleArr.data.size)
                            )
                        )
                    }
                }
            }
            return items
        }
    }
}

enum class TypeIndexPractice(val viewType: Int) {
    TIME(1),
    INDEX_START(2),
    INDEX_NORMAL(3),
    INDEX_END(4)
}

data class ItemTimeIndexPractice(val time: String, val title: String? = null) :
    BaseItemIndexPractice {
    override fun getTypeItem(): TypeIndexPractice {
        return TypeIndexPractice.TIME
    }
}

data class ItemIndexPractice(
    val turn: Int,
    val position: String? = null,
    val currPunch: Float? = null,
    val defaultPunch: Float? = null,
    val onTarget: Boolean = false,
    val type: TypeIndexPractice
) : BaseItemIndexPractice {
    override fun getTypeItem(): TypeIndexPractice {
        return type
    }

    fun getProgress(): Int = if (onTarget) {
        if (currPunch == null || defaultPunch == null) 0 else ((currPunch * 100) / (defaultPunch * 1.5F)).roundToInt()
    } else 0


    fun isPassIndex(): Boolean =
        if (currPunch == null || defaultPunch == null) false
        else {
            currPunch / defaultPunch >= 1
        }

    fun getIcon(): Int = when (position) {
        BlePosition.FACE.key -> R.drawable.ic_head_center
        BlePosition.LEFT_CHEEK.key -> R.drawable.ic_head_left
        BlePosition.RIGHT_CHEEK.key -> R.drawable.ic_head_right
        BlePosition.LEFT_CHEST.key -> R.drawable.ic_chest_left
        BlePosition.RIGHT_CHEST.key -> R.drawable.ic_chest_right
        BlePosition.ABDOMEN.key -> R.drawable.ic_hip_center
        BlePosition.ABDOMEN_UP.key -> R.drawable.ic_hip_bottom
        BlePosition.LEFT_ABDOMEN.key -> R.drawable.ic_hip_left
        BlePosition.RIGHT_ABDOMEN.key -> R.drawable.ic_hip_right
        BlePosition.LEFT_LEG.key -> R.drawable.ic_leg_left
        BlePosition.RIGHT_LEG.key -> R.drawable.ic_leg_right
        else -> R.drawable.ic_head_center
    }
}
