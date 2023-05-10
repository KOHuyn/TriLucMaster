package com.mobileplus.dummytriluc.ui.main.practice.dialog

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.response.DataSubmitPracticeResponse
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.ui.main.home.adapter.PowerChartDescriptionAdapter
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.ext.hide
import com.utils.ext.setTextColorz
import com.utils.ext.setVisibility
import com.utils.ext.show
import kotlinx.android.synthetic.main.fragment_confirm_practice_information.*
import kotlin.concurrent.thread
import kotlin.math.abs

/**
 * Created by KOHuyn on 3/2/2021
 */
class ConfirmPracticeInformationFragment(private val listDataBluetooth: List<BluetoothResponse>) :
    BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_confirm_practice_information

    private val adapterPos by lazy { PowerChartDescriptionAdapter(false) }
    var dataSubmit: DataSubmitPracticeResponse? = null
    var levelPractice: LevelPractice? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        txtPointRewardPractice.fillGradientPrimary()
        txtPointRewardPractice.fillGradientPrimary()
        setLevelUpdate(dataSubmit, levelPractice)
        rcvPowerPracticeTest.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterPos
        }
        calculateData()
    }

    private fun calculateData() {
        layoutLoadingConfirmPracticeInformation.show()
        thread(true) {
            val timeTotalPractice =
                if (listDataBluetooth.lastOrNull()?.endTime == null || listDataBluetooth.firstOrNull()?.startTime1 == null) null else abs(
                    listDataBluetooth.lastOrNull()?.endTime!! - listDataBluetooth.firstOrNull()?.startTime1!!
                )

            runOnUiThread {
                txtValueTimePracticeTest.text =
                    if (timeTotalPractice == null) "--:--"
                    else DateTimeUtil.convertTimeStampToMMSS(timeTotalPractice)
            }
            val dataPunchBle = mutableListOf<DataBluetooth?>()
            listDataBluetooth.forEach { dataSingle ->
                dataPunchBle.addAll(dataSingle.data)
            }
            if (dataPunchBle.isNotEmpty()) {
                var countPower = 0f
                dataPunchBle.forEach {
                    countPower += it?.force ?: 0f
                }
                runOnUiThread {
                    txtValuePunchesPracticeTest.text = dataPunchBle.size.toString()
                    txtValuePowerPracticeTest.text = countPower.toInt().toString()
                    adapterPos.items = transformToPower(dataPunchBle)
                }

                transformToPower(dataPunchBle).forEach { item ->
                    BlePositionUtils.setPosColorHuman(
                        humanConfirmTest,
                        item.score,
                        BlePositionUtils.findBlePositionWithKey(item.key)
                    )
                }
                var highFace = 0
                var highAbdomen = 0
                transformToHighScore(dataPunchBle).forEach { highScore ->
                    runOnUiThread {
                        when (highScore.key) {
                            BlePosition.FACE.key, BlePosition.LEFT_CHEEK.key, BlePosition.RIGHT_CHEEK.key -> {
                                if (highFace < highScore.score)
                                    highFace = highScore.score
                                BlePositionUtils.setHighScoreWithPos(
                                    humanConfirmTest,
                                    highFace,
                                    BlePosition.FACE
                                )
                            }
                            BlePosition.ABDOMEN_UP.key, BlePosition.LEFT_ABDOMEN.key, BlePosition.ABDOMEN.key, BlePosition.RIGHT_ABDOMEN.key -> {
                                if (highAbdomen < highScore.score)
                                    highAbdomen = highScore.score
                                BlePositionUtils.setHighScoreWithPos(
                                    humanConfirmTest,
                                    highAbdomen,
                                    BlePosition.ABDOMEN
                                )
                            }
                            else -> {
                                BlePositionUtils.setHighScoreWithPos(
                                    humanConfirmTest,
                                    highScore.score,
                                    BlePositionUtils.findBlePositionWithKey(highScore.key)
                                )
                            }
                        }
                    }
                }
            } else {
                runOnUiThread {
                    txtValuePunchesPracticeTest.text = "0"
                    txtValuePowerPracticeTest.text = "0"
                }
            }
            runOnUiThread {
                layoutLoadingConfirmPracticeInformation.hide()
            }
        }
    }

    fun runOnUiThread(block: () -> Unit) {
        if (this.isVisible) {
            activity?.runOnUiThread { block() } ?: Handler(Looper.getMainLooper()).post { block() }
        }
    }

    private fun setLevelUpdate(
        data: DataSubmitPracticeResponse?,
        choiceLevelPractice: LevelPractice?
    ) {
        if (data != null && choiceLevelPractice != null) {
            if (data.isCompleteLevel == 1) {
                titleLevelPractice?.text = loadStringRes(R.string.congratulations)
                contentLevelPractice?.text =
                    String.format(
                        loadStringRes(R.string.format_level_up),
                        choiceLevelPractice.level ?: "--"
                    )
                titleLevelPractice?.setTextColorz(R.color.clr_green)
                contentLevelPractice?.setTextColorz(R.color.clr_green)
            } else {
                titleLevelPractice?.text = loadStringRes(R.string.fighting)
                contentLevelPractice?.text =
                    String.format(
                        loadStringRes(R.string.format_not_level_up),
                        choiceLevelPractice.level
                    )
                titleLevelPractice?.setTextColorz(R.color.white)
                contentLevelPractice?.setTextColorz(R.color.white)
            }
        }
        setVisibleViewWhen(
            titleLevelPractice,
            contentLevelPractice,
        ) { data != null && choiceLevelPractice != null }
        viewPointRewardPractice.setVisibility(data != null)
        txtPointRewardPractice.setTextNotNull("+${data?.score?.toInt() ?: 0}")
    }


    private fun transformToHighScore(data: List<DataBluetooth?>): MutableList<PowerChartDescriptionAdapter.ItemDescriptionChartPower> {
        val items = mutableListOf<PowerChartDescriptionAdapter.ItemDescriptionChartPower>()
        BlePositionUtils.getListBle(true).forEach { blePos ->
            items.add(addPos(0f, blePos.title, blePos.key))
        }

        for (i in items.indices) {
            data.forEach { dataBluetooth ->
                if (dataBluetooth?.position == items[i].key) {
                    if (items[i].score < dataBluetooth?.force ?: 0f) {
                        items[i] =
                            addPos(dataBluetooth?.force ?: 0f, items[i].title, items[i].key)
                    }
                }
            }
        }
        return items
    }

    private fun transformToPower(data: List<DataBluetooth?>): MutableList<PowerChartDescriptionAdapter.ItemDescriptionChartPower> {
        val items = mutableListOf<PowerChartDescriptionAdapter.ItemDescriptionChartPower>()
        var scoreTotalFace = 0f
        var scoreTotalAbdomen = 0f
        var scoreTotalLeftChest = 0f
        var scoreTotalRightChest = 0f
        var scoreTotalLeftLeg = 0f
        var scoreTotalRightLeg = 0f
        data.forEach { item ->
            when (item?.position) {
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