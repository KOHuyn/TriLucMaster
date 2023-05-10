package com.mobileplus.dummytriluc.data.response.session


import android.graphics.Color
import android.widget.TextView
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.ui.main.home.adapter.PowerChartDescriptionAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import java.lang.Exception

data class CoachSessionOldResponse(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("class_id")
    val classId: Int? = null,
    @Expose
    @SerializedName("result_data")
    var resultData: List<ItemCoachSessionResultData>? = null,
    @Expose
    @SerializedName("result")
    var result: List<CoachSessionResultResponse>? = emptyList(),
    @Expose
    @SerializedName("created_at")
    val createdAt: String? = null,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @Expose
    @SerializedName("practice_detail")
    val practicesDetail: List<PracticesDetail>? = null,
    @Expose
    @SerializedName("users_detail")
    val usersDetail: List<UsersDetail>? = null
) {
    data class PracticesDetail(
        @Expose
        @SerializedName("id")
        val id: Int? = null,
        @Expose
        @SerializedName("title")
        val title: String? = null,
        @Expose
        @SerializedName("image_path")
        val imagePath: String? = null,
        @Expose
        @SerializedName("total_time")
        val totalTime: Int? = null,
        @Expose
        @SerializedName("round")
        val round: Int? = null
    )

    data class UsersDetail(
        @Expose
        @SerializedName("full_name")
        val fullName: String? = null,
        @Expose
        @SerializedName("avatar_path")
        val avatarPath: String? = null,
        @Expose
        @SerializedName("id")
        val id: Int? = null
    )


    data class ItemCoachSessionResultData(
        @Expose
        @SerializedName("data")
        val data: String? = null,
        @Expose
        @SerializedName("end_time")
        val endTime: Long? = null,
        @Expose
        @SerializedName("machine_id")
        val machineId: Int? = null,
        @Expose
        @SerializedName("mode")
        val mode: Int? = null,
        @Expose
        @SerializedName("session_id")
        val sessionId: Int? = null,
        @Expose
        @SerializedName("practice_id")
        val practiceId: Int? = null,
        @Expose
        @SerializedName("start_time1")
        val startTime1: Long? = null,
        @Expose
        @SerializedName("start_time2")
        val startTime2: Long? = null,
        @Expose
        @SerializedName("user_id")
        val userId: Int? = null
    ) {

        fun setRankTop(txt: TextView, rank: Int) {
            if (rank == 1) {
                txt.text = ""
            } else {
                txt.text = String.format("#%d", rank)
            }

            when (rank) {
                1 -> {
                    txt.setBackgroundResource(R.drawable.background_top1_ranking)
                }
                2, 3 -> {
                    txt.setBackgroundResource(R.color.clr_primary)
                }
                else -> {
                    txt.setBackgroundResource(R.color.clr_tab)
                }
            }
        }

        fun getHighTurnPassLevel(data: List<BluetoothResponse>): BluetoothResponse {
            return try {
                var pairDataHigh: Pair<Float, BluetoothResponse> = Pair(0F, data[0])
                data.map { dataResponse ->
                    var countPower = 0f
                    dataResponse.data.forEach {
                        countPower += it?.force ?: 0f
                    }
                    if (pairDataHigh.first < countPower) {
                        pairDataHigh = Pair(countPower, dataResponse)
                    }
                }
                return pairDataHigh.second
            } catch (e: Exception) {
                e.logErr()
                BluetoothResponse()
            }
        }


        fun transformToPower(data: List<DataBluetooth>): MutableList<PowerChartDescriptionAdapter.ItemDescriptionChartPower> {
            val items = mutableListOf<PowerChartDescriptionAdapter.ItemDescriptionChartPower>()
            var scoreTotalFace = 0f
            var scoreTotalAbdomen = 0f
            var scoreTotalLeftChest = 0f
            var scoreTotalRightChest = 0f
            var scoreTotalLeftLeg = 0f
            var scoreTotalRightLeg = 0f
            data.forEach { item ->
                when (item.position) {
                    BlePosition.FACE.key, BlePosition.LEFT_CHEEK.key, BlePosition.RIGHT_CHEEK.key -> {
                        scoreTotalFace += item.force ?: 0f
                        if (items.none {
                                it.key == BlePosition.FACE.key || it.key == BlePosition.LEFT_CHEEK.key
                                        || it.key == BlePosition.RIGHT_CHEEK.key
                            }) {
                            items.add(
                                addPos(
                                    scoreTotalFace,
                                    BlePosition.FACE.title,
                                    BlePosition.FACE.key
                                )
                            )
                        } else {
                            items[items.indexOf(items.first { it.key == BlePosition.FACE.key })] =
                                addPos(scoreTotalFace, BlePosition.FACE.title, BlePosition.FACE.key)
                        }
                    }
                    BlePosition.LEFT_CHEST.key -> {
                        scoreTotalLeftChest += item.force ?: 0f
                        items.addItems(scoreTotalLeftChest, BlePosition.LEFT_CHEST)
                    }
                    BlePosition.RIGHT_CHEST.key -> {
                        scoreTotalRightChest += item.force ?: 0f
                        items.addItems(scoreTotalRightChest, BlePosition.RIGHT_CHEST)
                    }
                    BlePosition.ABDOMEN_UP.key, BlePosition.LEFT_ABDOMEN.key, BlePosition.ABDOMEN.key, BlePosition.RIGHT_ABDOMEN.key -> {
                        scoreTotalAbdomen += item.force ?: 0f
                        if (items.none {
                                it.key == BlePosition.ABDOMEN_UP.key
                                        || it.key == BlePosition.LEFT_ABDOMEN.key
                                        || it.key == BlePosition.ABDOMEN.key
                                        || it.key == BlePosition.RIGHT_ABDOMEN.key
                            }) {
                            items.add(
                                addPos(
                                    scoreTotalAbdomen,
                                    BlePosition.ABDOMEN.title,
                                    BlePosition.ABDOMEN.key
                                )
                            )
                        } else {
                            items[items.indexOf(items.first { it.key == BlePosition.ABDOMEN.key })] =
                                addPos(
                                    scoreTotalAbdomen,
                                    BlePosition.ABDOMEN.title,
                                    BlePosition.ABDOMEN.key
                                )
                        }
                    }
                    BlePosition.LEFT_LEG.key -> {
                        scoreTotalLeftLeg += item.force ?: 0f
                        items.addItems(scoreTotalLeftLeg, BlePosition.LEFT_LEG)
                    }
                    BlePosition.RIGHT_LEG.key -> {
                        scoreTotalRightLeg += item.force ?: 0f
                        items.addItems(scoreTotalRightLeg, BlePosition.RIGHT_LEG)
                    }
                }
            }
            return items
        }

        private fun MutableList<PowerChartDescriptionAdapter.ItemDescriptionChartPower>.addItems(
            score: Float,
            pos: BlePosition
        ) {
            if (this.none { it.key == pos.key }) {
                this.add(
                    addPos(score, pos.title, pos.key)
                )
            } else {
                this[this.indexOf(this.first { it.key == pos.key })] =
                    addPos(score, pos.title, pos.key)
            }
        }

        private fun addPos(score: Float, title: String, key: String?) =
            PowerChartDescriptionAdapter.ItemDescriptionChartPower(
                Color.WHITE,
                score.toInt(), title, key
            )
    }
}
