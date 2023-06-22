package com.mobileplus.dummytriluc.ui.main.coach

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.ActionConnection
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.response.StatisticalCoachResponse
import com.mobileplus.dummytriluc.databinding.FragmentCoachMainBinding
import com.mobileplus.dummytriluc.transceiver.command.CoachModeCommand
import com.mobileplus.dummytriluc.ui.dialog.MenuOptionsDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.coach.adapter.CoachAdapter
import com.mobileplus.dummytriluc.ui.main.coach.adapter.RankingDiscipleAdapter
import com.mobileplus.dummytriluc.ui.main.coach.create_course.CoachCreateCourseFragment
import com.mobileplus.dummytriluc.ui.main.coach.disciple.DiscipleFragment
import com.mobileplus.dummytriluc.ui.main.coach.draft.CoachDraftFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.CoachGroupFragment
import com.mobileplus.dummytriluc.ui.main.coach.my_practice.CoachPracticeFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.list_old.CoachSessionListOldFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.saved_list.CoachSessionSavedListFragment
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoFragment
import com.mobileplus.dummytriluc.ui.main.practice.adapter.PracticeMoreAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.utils.ChartUtils
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopVideo
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadExerciseCoach
import com.mobileplus.dummytriluc.ui.utils.extensions.applyColorRefresh
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setVisibleViewWhen
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordFragment
import com.utils.applyClickShrink
import com.utils.ext.clickWithDebounce
import com.utils.ext.register
import com.utils.ext.setVisibility
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class CoachMainFragment : BaseFragmentZ<FragmentCoachMainBinding>() {
    override fun getLayoutBinding(): FragmentCoachMainBinding =
        FragmentCoachMainBinding.inflate(layoutInflater)

    private val coachMainViewModel by viewModel<CoachMainViewModel>()
    private val adapterMyExercise by lazy { CoachAdapter() }
    private val rankingDiscipleAdapter by lazy { RankingDiscipleAdapter() }
    private val page by lazy { Page() }
    private var isReload = true
    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(coachMainViewModel)
//        coachMainViewModel.getTrainerList()
        coachMainViewModel.getListRankingDisciple()
        coachMainViewModel.getStatistical()
        configView()
        handleClick()
        setupChart()
    }

    private fun configView() {
        binding.refreshCoachMain.applyColorRefresh()
        binding.cmbMyExercise.applyClickShrink()
        binding.cmbExerciseDraft.applyClickShrink()
        binding.cmbDisciple.applyClickShrink()
        binding.cmbDiscipleGroup.applyClickShrink()
        binding.cmbCreateCourseNewCoach.applyClickShrink()
        binding.cmbCreateExerciseNewCoach.applyClickShrink()
        binding.cmbListExerciseSave.applyClickShrink()
        binding.cmbListExerciseOld.applyClickShrink()
        binding.rcvMyExerciseCoach.run {
            setupRcvCoach()
            adapter = adapterMyExercise
        }
        setUpRcv(binding.rvDiscipleList, rankingDiscipleAdapter)
        binding.rvDiscipleList.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
    }

    private fun handleClick() {
        binding.btnBackCoach.clickWithDebounce { onBackPressed() }
        binding.refreshCoachMain.setOnRefreshListener {
            coachMainViewModel.getTrainerList()
            isReload = true
            coachMainViewModel.getListRankingDisciple()
        }

        binding.nestedScrollCoach.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (v.getChildAt(v.childCount - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.childCount - 1)).measuredHeight - v.measuredHeight) && scrollY > oldScrollY) {
                        isReload = false
                        page.run {
                            if (currPage < totalPage && !isLoading) {
                                isLoading = true
                                currPage++
                                coachMainViewModel.getListRankingDisciple(currPage)
                            }
                        }
                    }
                }
            })
        binding.btnMoreMyExerciseCoach.clickWithDebounce { CoachPracticeFragment.openFragment() }
        binding.cmbCreateExerciseNewCoach.clickWithDebounce { nextFragmentRecord() }
        binding.cmbCreateCourseNewCoach.clickWithDebounce { CoachCreateCourseFragment.openFragment() }
        binding.cmbMyExercise.clickWithDebounce { CoachPracticeFragment.openFragment() }
        binding.cmbExerciseDraft.clickWithDebounce { CoachDraftFragment.openFragment() }
        binding.cmbDisciple.clickWithDebounce { DiscipleFragment.openFragment() }
        binding.cmbDiscipleGroup.clickWithDebounce { CoachGroupFragment.openFragment(isGuest = false) }
        binding.btnCreateSessionNewCoach.clickWithDebounce { CoachSessionFragment.openFragment() }
        binding.cmbListExerciseSave.clickWithDebounce { CoachSessionSavedListFragment.openFragment() }
        binding.cmbListExerciseOld.clickWithDebounce { CoachSessionListOldFragment.openFragment() }

        adapterMyExercise.onClickItem = CoachAdapter.OnItemClick { item, type ->
            when (type) {
                CoachAdapter.TYPE_SINGLE -> {
                    item.id?.let { PracticeDetailFragment.openFragment(it) }
                }
                CoachAdapter.TYPE_MULTI -> {
                    item.id?.let { PracticeFolderFragment.openFragment(it) }
                }
            }
        }

        adapterMyExercise.onLongClickItem =
            CoachAdapter.OnLongClickPractice { item, viewType, pos ->
                when (viewType) {
                    PracticeMoreAdapter.TYPE_SINGLE -> {
                        MenuOptionsDialog.builder()
                            .addMenuOption(
                                R.drawable.ic_pen_edit,
                                getString(R.string.edit_exercise),
                                MenuOptionsDialog.MenuOption.EDITOR_EXERCISE
                            ).addMenuOption(
                                R.drawable.ic_video_delete,
                                getString(R.string.delete_exercise),
                                MenuOptionsDialog.MenuOption.DELETE
                            )
                            .build(parentFragmentManager)
                            .setOnCallbackListener { type ->
                                if (type == MenuOptionsDialog.MenuOption.EDITOR_EXERCISE) {
                                    item.id?.let { EditVideoFragment.openFragment(it, true) }
                                        ?: toast(loadStringRes(R.string.error_unknown_error))
                                }
                                if (type == MenuOptionsDialog.MenuOption.DELETE) {
                                    item.id?.let { coachMainViewModel.deletePractice(it, pos) }
                                        ?: toast(loadStringRes(R.string.error_unknown_error))
                                }
                            }
                    }
                    PracticeMoreAdapter.TYPE_MULTI -> {
                        MenuOptionsDialog.builder()
                            .addMenuOption(
                                R.drawable.ic_pen_edit,
                                getString(R.string.edit_course),
                                MenuOptionsDialog.MenuOption.EDITOR_FOLDER
                            ).addMenuOption(
                                R.drawable.ic_video_delete,
                                getString(R.string.delete_course),
                                MenuOptionsDialog.MenuOption.DELETE
                            )
                            .build(parentFragmentManager)
                            .setOnCallbackListener { type ->
                                if (type == MenuOptionsDialog.MenuOption.EDITOR_FOLDER) {
                                    item.id?.let { CoachCreateCourseFragment.openFragmentEditor(it) }
                                        ?: toast(loadStringRes(R.string.error_unknown_error))
                                }
                                if (type == MenuOptionsDialog.MenuOption.DELETE) {
                                    item.id?.let { coachMainViewModel.deletePractice(it, pos) }
                                        ?: toast(loadStringRes(R.string.error_unknown_error))
                                }
                            }
                    }
                }
            }


    }

    private fun nextFragmentRecord() {
        if ((requireActivity() as MainActivity).isConnectedBle) {
            VideoRecordFragment.openFragment(CoachModeCommand)
        } else {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }

    private fun enableRefresh() {
        binding.refreshCoachMain.isRefreshing = true
    }

    private fun cancelRefresh() {
        binding.refreshCoachMain.isRefreshing = false
    }

    private fun callbackViewModel(vm: CoachMainViewModel) {
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxArrayMyExercise.subscribe {
            binding.noDataExercise.root.setVisibility(it.isEmpty())
            adapterMyExercise.items = it.toMutableList()
        })
        addDispose(vm.rxRankingDisciple.subscribe {
            val items = it.first
            val pageResponse = it.second
            setVisibleViewWhen(binding.tvDisciple, binding.rvDiscipleList) { items.isNotEmpty() }
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            if (rankingDiscipleAdapter.items.isEmpty() || isReload)
                rankingDiscipleAdapter.items = items.toMutableList()
            else
                rankingDiscipleAdapter.items.addAll(items)
            binding.rvDiscipleList.requestLayout()
        })
        addDispose(vm.rxStatisticalCoach.subscribe {
            binding.barChartCoach.apply {
                data = setupDataBarChartStatistic(it)
                setLabel(it.map { it.date ?: "" }.toTypedArray())
                animateY(1000)
                invalidate()
            }
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                binding.loadMoreCoachMain.setVisibility(it)
            } else {
                if (it) enableRefresh() else cancelRefresh()
            }
        })
        addDispose(vm.rxDeleteAt.subscribe {
            val pos = it.first
            val isSuccess = it.second
            if (isSuccess) {
                adapterMyExercise.delete(pos)
            }
        })
    }

    private fun RecyclerView.setupRcvCoach() {
        this.run {
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    isHorizontalRecyclerView = true
                )
            )
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupChart() {
        ChartUtils.createBarChart(binding.barChartCoach)
        binding.barChartCoach.apply {
            setVisibleYRangeMinimum(40f, YAxis.AxisDependency.LEFT)
            data = setupDataBarChartStatistic()
            setLabel()
            animateY(1000)
            invalidate()
        }
    }

    private fun BarChart.setLabel(label: Array<String> = emptyArray()) {
        xAxis.setDrawLabels(label.isNotEmpty())
        if (label.isNotEmpty()) {
            xAxis.valueFormatter = IndexAxisValueFormatter(label)
        }
    }

    private fun setupDataBarChartStatistic(arrData: List<StatisticalCoachResponse> = emptyList()): BarData {
        val values = arrayListOf<BarEntry>()
        if (arrData.isEmpty()) {
            for (i in 0..6) {
                values.add(BarEntry(i.toFloat(), 0f))
            }
        } else {
            for (i in arrData.indices) {
                values.add(BarEntry(i.toFloat(), arrData[i].total?.toFloat() ?: 0F))
            }
        }
        val set = BarDataSet(values, "Statistic").apply {
            setDrawIcons(false)
            color = Color.parseColor("#00E0FF")
            valueTextColor = Color.WHITE
        }
        val dataSet = arrayListOf<IBarDataSet>(set)
        return BarData(dataSet).apply {
            barWidth = 0.5f
        }
    }

    @Subscribe
    fun popVideo(ev: EventPopVideo) {
        CoachDraftFragment.openFragment()
    }

    @Subscribe
    fun reloadExercise(ev: EventReloadExerciseCoach) {
        coachMainViewModel.getTrainerList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister(this)
    }


}