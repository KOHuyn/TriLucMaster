package com.mobileplus.dummytriluc.ui.main.practice.detail.main

import android.graphics.Color
import android.os.Bundle
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.model.ItemPracticeDetailMain
import com.mobileplus.dummytriluc.databinding.LayoutPracticeDetailMainTopBinding
import com.mobileplus.dummytriluc.ui.main.home.adapter.PowerChartDescriptionAdapter
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventEnableTopPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import java.io.StringReader

class PracticeDetailMainTopFragment : BaseFragmentZ<LayoutPracticeDetailMainTopBinding>() {

    override fun getLayoutBinding(): LayoutPracticeDetailMainTopBinding =
        LayoutPracticeDetailMainTopBinding.inflate(layoutInflater)

    private val gson by inject<Gson>()
    private val adapterPos by lazy { PowerChartDescriptionAdapter(false) }

    fun setTitleMainTop(title: String?) {
        binding.txtTitleMainTop?.text = title
        binding.txtTitleMainTopVisible?.text = title
    }

    fun setupMainTop(data: ItemPracticeDetailMain) {
        binding.txtDatePracticeMainTop.setTextNotNull(data.getCurrDate())
        binding.txtTimePracticeMainTop.setTextNotNull(data.getTimehhmm())
        binding.txtTitlePowerMainTop.setVisibility(data.items().isNotEmpty())
        adapterPos.items = data.items()
        if (data.data != null) {
            var dataBle = emptyList<DataBluetooth>()
            logErrFull("JSON_PRACTICE", data.data)

            try {
                dataBle = gson.toList(data.data)
            } catch (e: JsonSyntaxException) {
//                e.logErr()
                try {
//                    val jsonReader = JsonReader(StringReader(data.data))
//                    jsonReader.isLenient = true
//                    dataBle = gson.fromJson(
//                        jsonReader,object :TypeToken<DataBluetooth>(){}.type
//                    )
                    dataBle = gson.toList(
                        gson.fromJson(
                            data.data.trim(),
                            JsonElement::class.java
                        ).asJsonPrimitive.asString
                    )
                } catch (e: Exception) {
                    e.logErr()
                }

            } catch (e: Exception) {
                e.logErr()
                toast(loadStringRes(R.string.data_practice_not_available))
            }

            var highFace = 0
            var highAbdomen = 0
            transformToHighScore(dataBle).forEach { highScore ->
                when (highScore.key) {
                    BlePosition.FACE.key, BlePosition.LEFT_CHEEK.key, BlePosition.RIGHT_CHEEK.key -> {
                        if (highFace < highScore.score)
                            highFace = highScore.score
                        BlePositionUtils.setHighScoreWithPos(
                            binding.layoutHumanPractice.root,
                            highFace,
                            BlePosition.FACE
                        )
                    }
                    BlePosition.ABDOMEN_UP.key, BlePosition.LEFT_ABDOMEN.key, BlePosition.ABDOMEN.key, BlePosition.RIGHT_ABDOMEN.key -> {
                        if (highAbdomen < highScore.score)
                            highAbdomen = highScore.score
                        BlePositionUtils.setHighScoreWithPos(
                            binding.layoutHumanPractice.root,
                            highAbdomen,
                            BlePosition.ABDOMEN
                        )
                    }
                    else -> {
                        BlePositionUtils.setHighScoreWithPos(
                            binding.layoutHumanPractice.root,
                            highScore.score,
                            BlePositionUtils.findBlePositionWithKey(highScore.key)
                        )
                    }
                }
            }
        }

        for (item in data.items()) {
            BlePositionUtils.setPosColorHuman(
                binding.layoutHumanPractice.root,
                item.score,
                BlePositionUtils.findBlePositionWithKey(item.key)
            )
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        configView()
        controlButton()
    }

    private fun transformToHighScore(data: List<DataBluetooth>): MutableList<PowerChartDescriptionAdapter.ItemDescriptionChartPower> {
        val items = mutableListOf<PowerChartDescriptionAdapter.ItemDescriptionChartPower>()
        BlePositionUtils.getListBle(true).forEach { blePos ->
            items.add(addPos(0f, blePos.title, blePos.key))
        }

        for (i in items.indices) {
            data.forEach { dataBluetooth ->
                if (dataBluetooth.position == items[i].key) {
                    if (items[i].score < dataBluetooth.force ?: 0f) {
                        items[i] =
                            addPos(dataBluetooth.force ?: 0f, items[i].title, items[i].key)
                    }
                }
            }
        }
        return items
    }

    private fun addPos(score: Float, title: String, key: String?) =
        PowerChartDescriptionAdapter.ItemDescriptionChartPower(
            Color.WHITE,
            score.toInt(), title, key
        )

    private fun configView() {
        setUpRcv(binding.rcvPowerMainTopPractice, adapterPos)
    }

    private fun controlButton() {
        binding.visibilityMainTop.clickWithDebounce { postNormal(EventEnableTopPractice(false)) }
        binding.layoutHideMainTop.clickWithDebounce { postNormal(EventEnableTopPractice(true)) }
    }

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    @Subscribe
    fun hideShowLayout(ev: EventEnableTopPractice) {
        binding.layoutShowMainTop.setVisibility(ev.isEnable)
        binding.layoutHideMainTop.setVisibility(!ev.isEnable)
    }

}