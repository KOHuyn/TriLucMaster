package com.mobileplus.dummytriluc.ui.main.home

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.core.BaseFragmentZ
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.*
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.data.response.*
import com.mobileplus.dummytriluc.databinding.FragmentHomeBinding
import com.mobileplus.dummytriluc.transceiver.ConnectionState
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.TransceiverEvent
import com.mobileplus.dummytriluc.transceiver.command.IPracticeCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineFreePunchCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLedPunchCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineMusicCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineRelaxCommand
import com.mobileplus.dummytriluc.transceiver.mode.RelaxType
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.dialog.ChooseModePracticeDialog
import com.mobileplus.dummytriluc.ui.dialog.SelectMusicDialog
import com.mobileplus.dummytriluc.ui.dialog.SetupTargetDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.connect.ConnectFragment
import com.mobileplus.dummytriluc.ui.main.home.adapter.LessonHomeAdapter
import com.mobileplus.dummytriluc.ui.main.home.adapter.PowerChartDescriptionAdapter
import com.mobileplus.dummytriluc.ui.main.power.PowerFragment
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestFragment
import com.mobileplus.dummytriluc.ui.main.punch.PunchFragment
import com.mobileplus.dummytriluc.ui.main.ranking.RankingFragment
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.ChartUtils
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventClickNavBar
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateInfo
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.applyClickShrink
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class HomeFragment : BaseFragmentZ<FragmentHomeBinding>(), IObserverMachine {
    override fun getLayoutBinding(): FragmentHomeBinding =
        FragmentHomeBinding.inflate(layoutInflater)

    private val vm by viewModel<HomeViewModel>()
    private val gson by inject<Gson>()
    private val powerChartAdapter by lazy { PowerChartDescriptionAdapter() }
    private val lessonAdapter by lazy { LessonHomeAdapter() }
    var onSwitchToPractice: CallbackToFragment? = null
    private var dataHomeResponse: HomeListResponse? = null
    private val transceiver by lazy { ITransceiverController.getInstance() }
    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        vm.getHomeList()
        configChart()
        getUser()
        configView()
        controllerClick()
        setUpRcv(binding.rcvChartPowerHome, powerChartAdapter)
        setUpRcv(binding.rcvLessonYesterday, lessonAdapter)
    }

    override fun onEventMachineSendData(data: List<BluetoothResponse>) {
        vm.getHomeList()
    }

    private fun dataOnWeek(dataChart: MutableList<ItemChart> = mutableListOf()) {
        val values = arrayListOf<BarEntry>()
        val weekDays = arrayListOf<String>()
        var currDate = Calendar.getInstance().time.time / 1000
        for (i in 0..6) {
            weekDays.add(DateTimeUtil.convertTimeStampToDate(currDate, "dd/MM"))
            currDate -= 86400
        }
        weekDays.reverse()
        for (i in weekDays.indices) {
            val valueInArrayFirstOrNull = if (dataChart.isNotEmpty()) dataChart.firstOrNull {
                weekDays[i] == DateTimeUtil.convertDate(
                    it.titleZ,
                    "dd/MM/yy",
                    "dd/MM"
                )
            } else null
            if (dataChart.isNotEmpty() && valueInArrayFirstOrNull != null) {
                values.add(BarEntry(i.toFloat(), valueInArrayFirstOrNull.scoreChart))
            } else {
                values.add(BarEntry(i.toFloat(), 0f))
            }
        }
        val set = BarDataSet(values, "Statistic").apply {
            setDrawIcons(false)
            color = Color.parseColor("#00E0FF")
            valueTextColor = Color.WHITE
        }
        binding.chartStatistics.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(weekDays)
            data = BarData(arrayListOf<IBarDataSet>(set)).apply { barWidth = 0.5f }
            invalidate()
        }
    }

    private fun configChart() {
        ChartUtils.createBarChart(binding.chartStatistics)
        ChartUtils.createPieChart(binding.chartPowerHome)
        ChartUtils.createRadarChart(binding.chartTargetHome, emptyArray())
    }

    private fun disposeViewModel() {
        addDispose(vm.isLoading.subscribe { setRefreshHome(it) })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.homeListResponse.subscribe { responseHome(it) })
    }

    private fun responseHome(home: HomeListResponse) {
        dataHomeResponse = home
        parsePunch(home.punch)
        binding.txtAvgRadarChart.fillGradientPrimary()
        binding.txtAvgRadarChart.setTextNotNull(home.avg?.toString())
        home.lastPractice?.let { parseLastPractice(it) }
        home.week?.let { parseWeek(it) }
        home.chart?.let { parseAbility(it) }
        home.power?.let { parsePower(it) }
        home.rank?.let { parseRank(it) }
        parseCalories(home.calories)
        parseTimePractice(home.timePractice)
    }

    private fun getUser() {
        vm.user?.let {
            binding.imgAvatarUserHome.show(it.avatarPath)
            binding.txtNameUserHome.text = it.fullName
        }
    }

    private fun parseLastPractice(lastPractice: ArrayList<LastPractice>) {
        if (lastPractice.isNotEmpty()) {
            lessonAdapter.items = lastPractice
        }
        binding.layoutYesterdayHome.setVisibility(lastPractice.isNotEmpty())
    }

    private fun parseWeek(week: Week) {
        binding.tvPunch.text = week.totalPunchZ
        binding.tvPower.text = week.totalPowerZ
        binding.tvCal.text = week.totalCaloriesZ
        binding.tvAccuracy.text = week.accuracy ?: "--"
        dataOnWeek(week.chart)
    }

    private fun parseCalories(goalInfo: GoalInfo?) {
        binding.progressBarCaloriesHome.progress = goalInfo?.getProgress() ?: 0
        binding.txtCountCaloriesHome.text = goalInfo?.totalZ ?: "-"
        binding.tvGoalCalories.text = goalInfo?.goalZ
        val timeTypeLabel = TargetType.getType(goalInfo?.timeType)?.stringRes?.let { "/" + getString(it) } ?: ""
        val title = getString(R.string.calories) + timeTypeLabel
        binding.txtLabelCalories.text = title
    }

    private fun parseTimePractice(goalInfo: GoalInfo?) {
        binding.progressBarPracticeTimeHome.progress = goalInfo?.getProgress() ?: 0
        val strTimePractice = "${(goalInfo?.totalZ ?: "-")} ${getString(R.string.minutes)}"
        binding.txtCountPracticeTimeHome.text = strTimePractice
        binding.tvGoalPracticeTime.text = goalInfo?.goalZ ?: "-"
        val timeTypeLabel = TargetType.getType(goalInfo?.timeType)?.stringRes?.let { "/" + getString(it) } ?: ""
        val title = getString(R.string.time_practice) + timeTypeLabel
        binding.txtLabelPracticeTime.text = title
    }

    private fun parseRank(rank: Rank) {
        binding.txtNameRankingHome.text = rank.rankTitle
        binding.tvRank.text = String.format("#%s", rank.top)
        binding.txtScoreRanking.text = rank.scoreZ
        when {
            (rank.rankUp ?: 0) > 0 -> {
                binding.txtRankUp.text = String.format("+%d", rank.rankUp)
                binding.txtRankUp.setTextColorz(R.color.clr_green)
                binding.txtRankUp.setDrawableStart(R.drawable.ic_rank_up)
            }
            (rank.rankUp ?: 0) < 0 -> {
                binding.txtRankUp.text = String.format("%d", rank.rankUp)
                binding.txtRankUp.setTextColorz(R.color.clr_red)
                binding.txtRankUp.setDrawableStart(R.drawable.ic_rank_down)
            }
            else -> {
                binding.txtRankUp.setTextColorz(R.color.white)
                binding.txtRankUp.text = ""
                binding.txtRankUp.setDrawableStart(R.drawable.ic_rank_normal)
            }
        }
        binding.progressRankingHome.progress = rank.getProgress().toFloat()
    }

    private fun parsePower(power: Power) {
        binding.tvGoalPower.text = power.goalZ
        binding.txtTotalPowerCountHome.text = power.powerZ
        binding.progressBarPowerHome.progress = power.getProgress()
        val timeTypeLabel = TargetType.getType(power.timeType)?.stringRes?.let { "/" + getString(it) } ?: ""
        val title = getString(R.string.power) + timeTypeLabel
        binding.txtLabelPower.text = title
        val entries = arrayListOf<PieEntry>()
        val entriesTitle = arrayListOf<String>()

        val colorsChart = mutableListOf<Int>()

        if (power.detail != null && power.detail.isNotEmpty()) {
            for (item in power.detail) {
                colorsChart.add(Color.parseColor(item.colorZ))
                entries.add(PieEntry(item.scoreChart))
                entriesTitle.add(item.titleZ)
            }
        }

        if (!entries.isNullOrEmpty()) {
            var isAllZero = true
            for (item in entries) {
                if (item.value != 0f) {
                    isAllZero = false
                    break
                }
            }
            if (isAllZero) {
                colorsChart.add(Color.parseColor("#00D1FF"))
                entries.add(PieEntry(1f))
            }
        }

        val dataSet = PieDataSet(entries, "Power Chart").apply {
            setDrawIcons(false)
            sliceSpace = 3f
            iconsOffset = MPPointF(0f, 40f)
            selectionShift = 5f
            colors = colorsChart
        }
        val itemsChartPowerDescription =
            mutableListOf<PowerChartDescriptionAdapter.ItemDescriptionChartPower>()
        for (i in entriesTitle.indices) {
            itemsChartPowerDescription.add(
                PowerChartDescriptionAdapter.ItemDescriptionChartPower(
                    colorsChart[i],
                    score = entries[i].value.toInt(),
                    title = entriesTitle[i]
                )
            )
        }
        powerChartAdapter.items = itemsChartPowerDescription

        binding.chartPowerHome.run {
            data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter())
                setDrawValues(false)
            }
            invalidate()
        }
    }

    private fun parseAbility(chart: MutableList<ItemChart> = mutableListOf()) {
        val titles = arrayListOf<String>()
        val entries = arrayListOf<RadarEntry>()
        val backgroundEntries = arrayListOf<RadarEntry>()
        if (chart.isEmpty()) {
            for (i in 0..5) {
                titles.add("-")
                entries.add(RadarEntry(5f))
                backgroundEntries.add(RadarEntry(10f))
            }
        } else {
            for (item in chart) {
                titles.add(item.titleZ)
                entries.add(RadarEntry(item.scoreChart))
                backgroundEntries.add(RadarEntry(item.max ?: 0f))
            }
        }

        var titleArray = arrayOf<String>()
        titleArray = titles.toArray(titleArray)
        ChartUtils.createRadarChart(binding.chartTargetHome, titleArray)
        val set = RadarDataSet(entries, "Target")
        set.apply {
            color = resources.getColor(R.color.clr_blue)
            lineWidth = 2f
            setDrawHighlightIndicators(false)
        }
        val bgSet = RadarDataSet(backgroundEntries, "background")
        bgSet.apply {
            highlightCircleFillColor = Color.GRAY
            color = Color.TRANSPARENT
            setDrawFilled(true)
            setDrawHighlightIndicators(false)
        }
        val sets = arrayListOf<IRadarDataSet>(bgSet, set)
        val radarData = RadarData(sets).apply {
            setDrawValues(false)
        }
        binding.chartTargetHome.apply {
            data = radarData
            invalidate()
            animateXY(1000, 1000, Easing.EaseInOutQuad)
        }
    }

    private fun parsePunch(punch: Punch?) {
        binding.progressBarPunchHome.progress = punch?.getProgress() ?: 0
        binding.txtCountPunchHome.text = punch?.totalZ ?: "-"
        binding.tvGoalPunch.text = punch?.goalZ
        val timeTypeLabel = TargetType.getType(punch?.timeType)?.stringRes?.let { "/" + getString(it) } ?: ""
        val title = getString(R.string.punch) + timeTypeLabel
        binding.txtLabelPunch.text = title
    }

    private fun setColorFilterConnect(@ColorRes color: Int) {
        binding.btnConnectBleHome.setColorFilter(
            ContextCompat.getColor(requireContext(), color),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun configView() {
        transceiver.onConnectionStateChange(lifecycle) { state ->
            when (state) {
                ConnectionState.CONNECTED -> {
                    setColorFilterConnect(R.color.clr_primary)
                }

                else -> {
                    setColorFilterConnect(R.color.white)
                }
            }
        }
        binding.btnPracticeNowHome.applyClickShrink()
        binding.txtCountPunchHome.fillGradientPrimary()
        binding.txtScoreRanking.fillGradientPrimary()
        binding.swipeToRefreshLayout.applyColorRefresh()
        binding.tvToday.text = DateTimeUtil.convertCurrentDate()
    }

    private fun controllerClick() {
        binding.btnPracticeNowHome.clickWithDebounce {
            ChooseModePracticeDialog()
                .show(parentFragmentManager)
                .setCallbackChooseMode { dialog,type ->
                    handleSelectPracticeMode(type)
                }
        }
        binding.viewPunch.clickWithDebounce {
            PunchFragment.openFragment()
        }
        binding.viewPower.clickWithDebounce {
            PowerFragment.openFragment()
        }
        binding.viewRanking.clickWithDebounce {
            RankingFragment.openFragment(dataHomeResponse?.rank, gson)
        }
        binding.lnProfileHome.clickWithDebounce {
            postNormal(
                EventNextFragmentMain(UserInfoFragment::class.java, true)
            )
        }
        binding.btnOpenNavView.clickWithDebounce { postNormal(EventClickNavBar()) }
        binding.btnConnectBleHome.clickWithDebounce {
            ConnectFragment.openFragment()
        }
        binding.swipeToRefreshLayout.run {
            setOnRefreshListener {
                vm.getHomeList()
            }
        }
        binding.ivEditPunch.clickWithDebounce {
            SetupTargetDialog()
                .setTargetUnit(TargetUnit.TOTAL_PUNCH)
                .apply {
                    dataHomeResponse?.punch?.let {punch ->
                        TargetType.getType(punch.timeType)?.let { setTargetType(it) }
                        punch.goal?.let { setTargetPoint(it) }
                    }
                }.setOnResultListener { targetUnit, targetTime, targetPoint ->
                    updateTarget(targetUnit, targetTime, targetPoint)
                }.show(parentFragmentManager,"ivEditPunch")
        }
        binding.ivEditCalories.clickWithDebounce {
            SetupTargetDialog()
                .setTargetUnit(TargetUnit.TOTAL_CALORIES)
                .apply {
                    dataHomeResponse?.calories?.let {calories ->
                        TargetType.getType(calories.timeType)?.let { setTargetType(it) }
                        calories.goal?.let { setTargetPoint(it) }
                    }
                }.setOnResultListener { targetUnit, targetTime, targetPoint ->
                    updateTarget(targetUnit, targetTime, targetPoint)
                }.show(parentFragmentManager,"ivEditCalories")
        }
        binding.ivEditPower.clickWithDebounce {
            SetupTargetDialog()
                .setTargetUnit(TargetUnit.TOTAL_POWER)
                .apply {
                    dataHomeResponse?.power?.let {power ->
                        TargetType.getType(power.timeType)?.let { setTargetType(it) }
                        power.goal?.let { setTargetPoint(it) }
                    }
                }.setOnResultListener { targetUnit, targetTime, targetPoint ->
                    updateTarget(targetUnit, targetTime, targetPoint)
                }.show(parentFragmentManager,"ivEditCalories")
        }
        binding.ivEditPracticeTime.clickWithDebounce {
            SetupTargetDialog()
                .setTargetUnit(TargetUnit.TIME_PRACTICE)
                .apply {
                    dataHomeResponse?.timePractice?.let {timePractice ->
                        TargetType.getType(timePractice.timeType)?.let { setTargetType(it) }
                        timePractice.goal?.let { setTargetPoint(it) }
                    }
                }.setOnResultListener { targetUnit, targetTime, targetPoint ->
                    updateTarget(targetUnit, targetTime, targetPoint)
                }.show(parentFragmentManager,"ivEditPracticeTime")
        }
    }

    private fun handleSelectPracticeMode(type: ChooseModePracticeDialog.PracticeType){
        when (type) {
            ChooseModePracticeDialog.PracticeType.COURSE -> {
                onSwitchToPractice?.callbackToPractice()
            }
            ChooseModePracticeDialog.PracticeType.FOR_FUN -> {

            }
            ChooseModePracticeDialog.PracticeType.FREE_FIGHT -> {
                val command = MachineFreePunchCommand(-1, -1)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    PracticeTestFragment.openFragment(command.copy(avgPower = avgPower,avgHit = avgHit))
                }
            }

            ChooseModePracticeDialog.PracticeType.ACCORDING_LED -> {
                val command = MachineLedPunchCommand(-1, -1)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    PracticeTestFragment.openFragment(command.copy(avgPower = avgPower, avgHit = avgHit))
                }
            }
            ChooseModePracticeDialog.PracticeType.ACCORDING_MUSIC -> {
                val command = MachineMusicCommand(50, 20, 544)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    vm.getListMusic { listMusic ->
                        SelectMusicDialog(listMusic ?: emptyList())
                            .setOnMusicSelected { music ->
                                PracticeTestFragment.openFragment(
                                    command.copy(
                                        avgPower = avgPower,
                                        avgHit = avgHit,
                                        musicId = music.id ?: -1
                                    )
                                )
                            }.show(parentFragmentManager, "")
                    }
                }
            }
            ChooseModePracticeDialog.PracticeType.RELAX -> { handleDeveloping() }
            ChooseModePracticeDialog.PracticeType.BEAT_HUSBAND -> {
                val command = MachineRelaxCommand(-1, -1, RelaxType.HUSBAND)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    PracticeTestFragment.openFragment(command.copy(avgPower = avgPower, avgHit = avgHit))
                }
            }
            ChooseModePracticeDialog.PracticeType.BEAT_LOVE_ENEMY -> {
                val command = MachineRelaxCommand(-1, -1, RelaxType.LOVE_ENEMY)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    PracticeTestFragment.openFragment(command.copy(avgPower = avgPower, avgHit = avgHit))
                }
            }
            ChooseModePracticeDialog.PracticeType.BEAT_EX -> {
                val command =MachineRelaxCommand(-1, -1, RelaxType.EX)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    PracticeTestFragment.openFragment(command.copy(avgPower = avgPower, avgHit = avgHit))
                }
            }
            ChooseModePracticeDialog.PracticeType.BEAT_BOSS -> {
                val command = MachineRelaxCommand(-1, -1, RelaxType.BOSS)
                checkConnectOrElse(command) { avgPower, avgHit ->
                    PracticeTestFragment.openFragment(command.copy(avgPower = avgPower, avgHit = avgHit))
                }
            }
        }
    }

    private fun checkConnectOrElse(data: IPracticeCommand, block: (avgPower: Int, avgHit: Int) -> Unit) {
        if (transceiver.isConnected()) {
            vm.getAvgPractice(data.getIdPractice().toInt()) { avgPower, avgHit ->
                block(avgPower, avgHit)
            }
        } else {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }

    private fun updateTarget(targetUnit: TargetUnit, targetTime: TargetType, targetPoint: Int) {
        vm.updateTarget(targetUnit, targetTime, targetPoint) { isSuccess ->
            if (isSuccess) {
                vm.getHomeList()
            }
        }
    }

    private fun  handleDeveloping()  {
        toast(getString(R.string.feature_developing))
    }

    private fun setRefreshHome(isRefresh: Boolean) {
        binding.swipeToRefreshLayout.isRefreshing = isRefresh
    }

    fun interface CallbackToFragment {
        fun callbackToPractice()
    }

    fun clickToSelf() {
        if (binding.nestedScrollHome.scrollY >= 500) {
            binding.nestedScrollHome.post {
                binding.nestedScrollHome.smoothScrollTo(0, 0, 500)
            }
            return
        }
//        if (nestedScrollHome.scrollY <= 500) {
//            if (swipeToRefreshLayout.isRefreshing) return
//            homeViewModel.getHomeList()
//            return
//        }
    }

    override fun onStart() {
        super.onStart()
        register(this)
        transceiver.registerObserver(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
        transceiver.removeObserver(this)
    }

    @Subscribe
    fun onUpdateInfo(e: EventUpdateInfo) {
        binding.imgAvatarUserHome.show(e.user.avatarPath)
        binding.txtNameUserHome.text = e.user.fullName ?: "----"
        logErr("updateUser")
    }

    @Subscribe
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.txtLabelPunch.text = loadStringRes(R.string.punch)
        binding.txtLabelValuePunch.text = loadStringRes(R.string.punch)
        binding.txtLabelTargetPunch.text = loadStringRes(R.string.target)
        binding.txtLabelPower.text = loadStringRes(R.string.power)
        binding.txtLabelValuePower.text = loadStringRes(R.string.power)
        binding.txtLabelTargetPower.text = loadStringRes(R.string.target)
        binding.txtLabelRankPoint.text = loadStringRes(R.string.point_ranking)
        binding.txtLabelLastestExercise.text = loadStringRes(R.string.latest_exercise)
        binding.txtLabelLast7Day.text = loadStringRes(R.string.last_7_days)
        binding.txtLabel7DayPunch.text = loadStringRes(R.string.punch)
        binding.txtLabel7DayPower.text = loadStringRes(R.string.power)
        binding.txtLabel7DayCal.text = loadStringRes(R.string.cal)
        binding.txtLabel7DayAccuracy.text = loadStringRes(R.string.accuracy)
        binding.btnPracticeNowHome.text = loadStringRes(R.string.practice_now)

        lessonAdapter.notifyDataSetChanged()
    }

}