package com.mobileplus.dummytriluc.ui.main.power

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.core.BaseFragmentZ
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemProgressPower
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.ItemPower
import com.mobileplus.dummytriluc.databinding.FragmentPowerBinding
import com.mobileplus.dummytriluc.ui.main.power.adapter.ProgressPowerAdapter
import com.mobileplus.dummytriluc.ui.utils.ChartUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.applyColorRefresh
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class PowerFragment : BaseFragmentZ<FragmentPowerBinding>() {
    override fun getLayoutBinding(): FragmentPowerBinding =
        FragmentPowerBinding.inflate(layoutInflater)

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(PowerFragment::class.java, true))
        }
    }

    private var isSelectedFilterDate: String = ApiConstants.MONTH
    private var page = 1
        set(value) {
            field = value
            if (field <= 1) {
                binding.btnNextPower.isEnabled = false
                binding.btnNextPower.alpha = 0.5f
                field = 1
            } else {
                binding.btnNextPower.isEnabled = true
                binding.btnNextPower.alpha = 1f
            }
            setupDataChartPower(null)
        }
    private val vm by viewModel<PowerViewModel>()
    private val progressAdapter by lazy { ProgressPowerAdapter() }
    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        viewModelCallback()
        vm.getDataPower()
        handleClick()
        configView()
        setupChartView()
    }

    private fun viewModelCallback() {
        addDispose(vm.responsePower.subscribe { data ->
            binding.txtDatePower.text = data.time
//            layoutInfoPower.setVisibility(data.chart.isNotEmpty())
            setupDataBarChartStatistic(data.chart)
        })
        addDispose(vm.isLoading.subscribe { isLoading ->
            binding.refreshLayoutPower.isRefreshing = isLoading
            binding.btnPreviousPower.isEnabled = !isLoading
            if (page > 1) {
                binding.btnNextPower.isEnabled = !isLoading
                binding.btnNextPower.alpha = if (isLoading) 0.5f else 1f
            }
            binding.btnPreviousPower.alpha = if (isLoading) 0.5f else 1f
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
    }


    private fun configView() {
        binding.btnNextPower.isEnabled = false
        binding.btnNextPower.alpha = 0.5f
        setUpRcv(binding.rcvProgressPower, progressAdapter)
        binding.refreshLayoutPower.applyColorRefresh()
    }

    private fun handleClick() {
        binding.btnBackPower.clickWithDebounce { onBackPressed() }
        binding.refreshLayoutPower.setOnRefreshListener {
            vm.getDataPower(isSelectedFilterDate, page)
        }

        binding.rgPower.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPowerWeek -> {
                    if (isSelectedFilterDate != ApiConstants.WEEK) {
                        isSelectedFilterDate = ApiConstants.WEEK
                        page = 1
                        vm.getDataPower(isSelectedFilterDate, page)
                    }
                }
                R.id.rbPowerMonth -> {
                    if (isSelectedFilterDate != ApiConstants.MONTH) {
                        isSelectedFilterDate = ApiConstants.MONTH
                        page = 1
                        vm.getDataPower(isSelectedFilterDate, page)
                    }
                }
                R.id.rbPowerYear -> {
                    if (isSelectedFilterDate != ApiConstants.YEAR) {
                        isSelectedFilterDate = ApiConstants.YEAR
                        page = 1
                        vm.getDataPower(isSelectedFilterDate, page)
                    }
                }
            }
        }

        binding.btnPreviousPower.clickWithDebounce {
            page++
            vm.getDataPower(isSelectedFilterDate, page)
        }
        binding.btnNextPower.clickWithDebounce {
            page--
            vm.getDataPower(isSelectedFilterDate, page)
        }
    }

    private fun setupChartView() {
        configPieChart()
        configBarChart()
    }

    private fun configPieChart() {
        ChartUtils.createPieChart(binding.pieChartPower)
    }

    private fun configBarChart() {
        ChartUtils.createBarChart(binding.barChartPower)
        setupDataBarChartStatistic()
    }

    private fun setupDataBarChartStatistic(dataChart: MutableList<ItemPower> = mutableListOf()) {
        val values = arrayListOf<BarEntry>()
        val labels = arrayListOf<String>()
        binding.barChartPower.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                try {
                    if (dataChart.isNotEmpty()) {
                        e?.let { setupDataChartPower(dataChart[it.x.toInt()]) }
                    }
                } catch (e: Exception) {
                    setupDataChartPower(null)
                    e.logErr()
                }
            }

            override fun onNothingSelected() {
                setupDataChartPower(null)
            }
        })

        if (dataChart.isNotEmpty()) {
//            barChartPower.highlightValue(0f, 0)
//            setupDataChartPower(dataChart[0])
            for (i in dataChart.indices) {
                values.add(BarEntry(i.toFloat(), dataChart[i].score.toFloat()))
                labels.add(dataChart[i].title)
            }
        } else {
            values.add(BarEntry(0f, 0f))
        }
        val set = BarDataSet(values, "Statistic").apply {
            setDrawIcons(false)
            setDrawValues(false)
            highLightColor = ResourcesCompat.getColor(resources, R.color.clr_blue_light, null)
            color = ResourcesCompat.getColor(resources, R.color.clr_blue, null)
            valueTextColor = Color.WHITE
        }
        val dataSet = arrayListOf<IBarDataSet>(set)

        binding.barChartPower.apply {
            if (labels.isNotEmpty()) {
                xAxis.setDrawLabels(true)
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            } else {
                xAxis.setDrawLabels(false)
            }
            data = BarData(dataSet).apply {
                barWidth = 0.5f
            }
            animateY(1000)
            invalidate()
        }
    }

    private fun setupDataChartPower(dataChart: ItemPower?) {
        binding.layoutInfoPower.setVisibility(dataChart != null)
        if (dataChart != null) {
            binding.txtTotalPowerCount.text = dataChart.score.toString()
            val entries = arrayListOf<PieEntry>()
            val colorsChart = mutableListOf<Int>()
            if (dataChart.detail.isEmpty()) {
                entries.add(PieEntry(1f))
                colorsChart.add(R.color.clr_blue)
            } else {
                val itemsChartPowerDescription =
                    mutableListOf<ItemProgressPower>()
                for (i in dataChart.detail.indices) {
                    val item = dataChart.detail[i]
                    entries.add(PieEntry(item.scoreChart))
                    colorsChart.add(Color.parseColor(item.colorZ))
                    val progress =
                        if (dataChart.score != 0) item.scoreChart.toInt() * 100 / dataChart.score else 0
                    itemsChartPowerDescription.add(
                        ItemProgressPower(
                            progress,
                            colorsChart[i], item.titleZ
                        )
                    )
                }
                progressAdapter.items = itemsChartPowerDescription

            }
            val dataSet = PieDataSet(entries, "Power Chart").apply {
                setDrawIcons(false)
                sliceSpace = 3f
                iconsOffset = MPPointF(0f, 40f)
                selectionShift = 5f
                colors = colorsChart
            }
            binding.pieChartPower.run {
                data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter())
                    setDrawValues(false)
                }
                animateY(1000, Easing.EaseInOutQuad)
                invalidate()
            }
        }
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
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.txtHeader.text = loadStringRes(R.string.power)
        binding.rbPowerWeek.text = loadStringRes(R.string.week)
        binding.rbPowerMonth.text = loadStringRes(R.string.month)
        binding.rbPowerYear.text = loadStringRes(R.string.year)
        binding.txtLabelPower.text = loadStringRes(R.string.power)
    }
}