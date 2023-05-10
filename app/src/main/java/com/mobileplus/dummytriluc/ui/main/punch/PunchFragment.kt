package com.mobileplus.dummytriluc.ui.main.punch

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.core.BaseFragmentZ
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.ItemPunch
import com.mobileplus.dummytriluc.databinding.FragmentPunchBinding
import com.mobileplus.dummytriluc.ui.utils.ChartUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.applyColorRefresh
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class PunchFragment : BaseFragmentZ<FragmentPunchBinding>() {

    override fun getLayoutBinding(): FragmentPunchBinding =
        FragmentPunchBinding.inflate(layoutInflater)

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(PunchFragment::class.java, true))
        }
    }

    private val vm by viewModel<PunchViewModel>()
    private var isSelectedFilterDate: String = ApiConstants.MONTH
    private var page = 1
        set(value) {
            field = value
            runOnUiThread {
                if (field <= 1) {
                    binding.btnNextPunch.isEnabled = false
                    binding.btnNextPunch.alpha = 0.5f
                    field = 1
                } else {
                    binding.btnNextPunch.isEnabled = true
                    binding.btnNextPunch.alpha = 1f
                }
                binding.layoutCurrentPunch.hide()
            }
        }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        viewModelCallback()
        vm.getDataPunch()
        configView()
        handleClick()
        setupBarChartStatistic()
    }

    private fun viewModelCallback() {
        addDispose(vm.responsePunch.subscribe { data ->
            setupDataBarChartStatistic(data.chart)
            binding.txtDatePunch.text = data.time
        })
        addDispose(vm.isLoading.subscribe { isLoading ->
            binding.refreshLayoutPunch.isRefreshing = isLoading
            if (page > 1) {
                binding.btnNextPunch.isEnabled = !isLoading
                binding.btnNextPunch.alpha = if (isLoading) 0.5f else 1f
            }
            binding.btnPreviousPunch.isEnabled = !isLoading
            binding.btnPreviousPunch.alpha = if (isLoading) 0.5f else 1f

        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
    }

    private fun configView() {
        binding.btnNextPunch.isEnabled = false
        binding.btnNextPunch.alpha = 0.5f
        binding.txtCurrentPunch.fillGradientPrimary()
        binding.refreshLayoutPunch.applyColorRefresh()
    }

    private fun handleClick() {
        binding.refreshLayoutPunch.setOnRefreshListener {
            vm.getDataPunch(isSelectedFilterDate, page)
        }
        binding.btnBackPunch.clickWithDebounce {
            onBackPressed()
        }

        binding.rgPunch.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPunchWeek -> {
                    if (isSelectedFilterDate != ApiConstants.WEEK) {
                        isSelectedFilterDate = ApiConstants.WEEK
                        page = 1
                        vm.getDataPunch(isSelectedFilterDate, page)
                    }
                }
                R.id.rbPunchMonth -> {
                    if (isSelectedFilterDate != ApiConstants.MONTH) {
                        isSelectedFilterDate = ApiConstants.MONTH
                        page = 1
                        vm.getDataPunch(isSelectedFilterDate, page)
                    }
                }
                R.id.rbPunchYear -> {
                    if (isSelectedFilterDate != ApiConstants.YEAR) {
                        isSelectedFilterDate = ApiConstants.YEAR
                        page = 1
                        vm.getDataPunch(isSelectedFilterDate, page)
                    }
                }
            }
        }

        binding.btnPreviousPunch.clickWithDebounce {
            page++
            vm.getDataPunch(isSelectedFilterDate, page)
        }
        binding.btnNextPunch.clickWithDebounce {
            page--
            vm.getDataPunch(isSelectedFilterDate, page)
        }
    }

    private fun setupBarChartStatistic() {
        ChartUtils.createBarChart(binding.barChartPunch)
        binding.barChartPunch.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                binding.txtCurrentPunch.text = "${e?.y?.toInt() ?: 0}"
                binding.layoutCurrentPunch.show()
            }

            override fun onNothingSelected() {
                binding.layoutCurrentPunch.hide()
                binding.txtCurrentPunch.text = "0"
            }
        })
        setupDataBarChartStatistic()
    }

    private fun setupDataBarChartStatistic(dataChart: MutableList<ItemPunch> = mutableListOf()) {
        val values = arrayListOf<BarEntry>()
        val labels = mutableListOf<String>()
        if (dataChart.isEmpty()) {
            binding.layoutCurrentPunch.hide()
            values.add(BarEntry(0f, 0f))
        } else {
            for (i in dataChart.indices) {
                labels.add(dataChart[i].title)
                values.add(BarEntry(i.toFloat(), dataChart[i].score.toFloat()))
            }
        }

        val set = BarDataSet(values, "Statistic").apply {
            setDrawIcons(false)
            setDrawValues(false)
            highLightColor = ResourcesCompat.getColor(resources, R.color.clr_blue_light, null)
            color = ResourcesCompat.getColor(resources, R.color.clr_blue, null)
            valueTextColor = Color.WHITE
        }
        val dataSet = arrayListOf<IBarDataSet>(set)
        binding.barChartPunch.run {
            if (labels.isNotEmpty()) {
                xAxis.setDrawLabels(true)
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            } else {
                xAxis.setDrawLabels(false)
            }
            data = BarData(dataSet).apply { barWidth = 0.5f }
            animateY(1000)
            invalidate()
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
        binding.txtHeader.text = loadStringRes(R.string.punch)
        binding.txtLabelPunch.text = loadStringRes(R.string.punch)
        binding.rbPunchWeek.text = loadStringRes(R.string.week)
        binding.rbPunchMonth.text = loadStringRes(R.string.month)
        binding.rbPunchYear.text = loadStringRes(R.string.year)
    }
}