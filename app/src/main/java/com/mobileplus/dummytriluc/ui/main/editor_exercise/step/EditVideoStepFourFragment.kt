package com.mobileplus.dummytriluc.ui.main.editor_exercise.step

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.SaveExerciseRequest
import com.mobileplus.dummytriluc.databinding.FragmentEditVideoFourBinding
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter.TimeLineAdapter
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoViewModel
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadExerciseCoach
import com.mobileplus.dummytriluc.ui.utils.extensions.BlePosition
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.mobileplus.dummytriluc.ui.widget.SpeedyLinearLayoutManager
import com.mobileplus.dummytriluc.ui.widget.ViewSeekBar
import com.utils.ext.*
import kotlinx.android.synthetic.main.item_video_details.*
import kotlinx.android.synthetic.main.layout_human_video.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class EditVideoStepFourFragment : BaseFragmentZ<FragmentEditVideoFourBinding>() {

    override fun getLayoutBinding(): FragmentEditVideoFourBinding =
        FragmentEditVideoFourBinding.inflate(layoutInflater)

    private val viewModel by viewModel<EditVideoViewModel>()
    private val rvTimeLine by lazy { rvTimeLineEditorVideo }
    private val arrLevelValue = mutableListOf<Int>()

    private val timeLineAdapter by lazy { TimeLineAdapter() }
    var isRunning: Boolean = false

    private val gson by inject<Gson>()
    private val request: SaveExerciseRequest by lazy {
        val json = argument(ARG_DATA_STEP_4, "").value
        if (json.isBlank()) SaveExerciseRequest() else gson.fromJson(
            json,
            SaveExerciseRequest::class.java
        )
    }
    private val isUpdateExercise by argument(ARG_UPDATE_EXERCISE, false)

    companion object {
        private const val ARG_DATA_STEP_4 = "ARG_DATA_STEP_4"
        private const val ARG_UPDATE_EXERCISE = "ARG_UPDATE_EXERCISE2"
        fun openFragment(request: SaveExerciseRequest, gson: Gson, isEditExercise: Boolean) {
            val bundle = Bundle()
            bundle.putString(ARG_DATA_STEP_4, gson.toJson(request))
            bundle.putBoolean(ARG_UPDATE_EXERCISE, isEditExercise)
            postNormal(
                EventNextFragmentMain(
                    EditVideoStepFourFragment::class.java,
                    bundle,
                    true
                )
            )
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposeViewModel()
        setupRcv()
        fillData()
        handleClick()
        setupView()
    }

    private fun setupView() {
//        if (isUpdateExercise) {
//            request.moreInformation.levelPractice?.let {
//                binding.spinnerNumberLevel.text = it.size.toString()
//                generateSlider(if (it.size <= 1) 1 else it.size)
//            }
//        }
    }

    private fun disposeViewModel() {
        viewModel.run {
            addDispose(
                rxMessage.subscribe { toast(it) },
                isLoading.subscribe { if (it) showDialog() else hideDialog() },
                savePracticeSuccess.subscribe {
                    postNormal(EventReloadExerciseCoach())
                    (requireActivity() as MainActivity).popBackToTrainerMain()
                }
            )
        }
    }

    private fun fillData() {
        btnClearTimeLine.invisible()
        playerVideoEditExercise.hide()
//        binding.spinnerNumberLevel.text = 3.toString()
//        generateSlider(3)
        val totalTimeLine =
            if (request.endTime != 0L && request.startTime1 != 0L)
                ((request.endTime ?: 0L) - (request.startTime1 ?: 0L)).toInt() * 10
            else 0
        updateTimeLine(totalTimeLine)
        timeLineAdapter.items = request.moreInformation.listDataVideoTimeline
    }

    private fun handleClick() {
        binding.btnBackToPrevious.clickWithDebounce {
            onBackPressed()
        }

        binding.btnBackEditVideoFour.clickWithDebounce {
            onBackPressed()
        }

        binding.btnNextToStepFour.clickWithDebounce {
            request.levelPractice = "100"
            if (isUpdateExercise) {
                viewModel.updatePractice(request)
            } else {
                viewModel.savePractice(request)
            }
//            if (arrLevelValue.isEmpty()) {
//                toast(getString(R.string.you_have_not_selected_a_level))
//            } else {
//                request.levelPractice = arrLevelValue.toList().joinToString(",")
//                if (isUpdateExercise) {
//                    viewModel.updatePractice(request)
//                } else {
//                    viewModel.savePractice(request)
//                }
//            }
        }

        clickPlayTimeLine()
        clickZoomIn()
        clickZoomOut()
//        clickLevel()
    }

    private fun arrLevel(): MutableList<CustomSpinner.SpinnerItem> {
        val items = mutableListOf<CustomSpinner.SpinnerItem>()
        for (i in 1..5) {
            items.add(CustomSpinner.SpinnerItem(i.toString(), i.toString()))
        }
        return items
    }

//    private fun generateSlider(@IntRange(from = 1, to = 5) numberLevel: Int) {
//        binding.sliderNumberPickerContainer.removeAllViews()
//        arrLevelValue.clear()
//        for (levelId in 1..numberLevel) {
//            arrLevelValue.add(50)
//            val seekBarSlide = ViewSeekBar(requireContext())
//            seekBarSlide.maxProgress = 7
//            seekBarSlide.max = 120
//            seekBarSlide.min = 50
//            val progress =
//                when (levelId) {
//                    1 -> 0
//                    2 -> 3
//                    3 -> 6
//                    else -> 2
//                }
//
//            seekBarSlide.generate(50,
//                120,
//                7,
//                progress,
//                getString(R.string.value_level, levelId),
//                object : ViewSeekBar.OnUpdateValue {
//                    override fun onUpdate(progress: Int) {
//                        arrLevelValue[levelId - 1] = progress
//                    }
//                }
//            )
//            binding.sliderNumberPickerContainer.addView(seekBarSlide)
//        }
//    }

//    private fun clickLevel() {
//        binding.spinnerNumberLevel.clickWithDebounce {
//            CustomSpinner(it, requireContext())
//                .setTextSize(resources.getDimension(R.dimen.text_14))
//                .setTextColor(R.color.clr_tab)
//                .setBackGroundSpinner(Color.WHITE)
//                .setDataSource(arrLevel())
//                .build()
//                .setOnSelectedItemCallback { item ->
//                    generateSlider(item.name.toInt())
//                }
//        }
//    }

    private fun clickPlayTimeLine() {
        btnPlayTimeLine.clickWithDebounce {
            if (isRunning) {
                pauseTimeline()
            } else {
                startTimeLine()
                autoScroll()
            }
        }
    }

    private fun clickZoomIn() {
        btnZoomIn.clickWithDebounce {
            timeLineAdapter.zoomIn()
        }
    }

    private fun clickZoomOut() {
        btnZoomOut.clickWithDebounce {
            timeLineAdapter.zoomOut()
        }
    }

    private fun setupRcv() {
        rvTimeLine.run {
            adapter = timeLineAdapter
            layoutManager =
                SpeedyLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }


        rvTimeLine.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var oldPos = AppConstants.INTEGER_DEFAULT
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val currentPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (currentPosition != oldPos) {
                    updateHitPosition(currentPosition)
                    oldPos = currentPosition
                }
                val lastPos =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (isRunning && lastPos == timeLineAdapter.itemCount) {
                    rvTimeLine.scrollToPosition(0)
                    pauseTimeline()
                }
            }
        })
    }

    private fun updateTimeLine(totalMsTimeLine: Int) {
        val second = totalMsTimeLine / 10
        val m = second / 60
        val s = second % 60
        timeHourVideo.text = m.setTime()
        timeMinuteVideo.text = s.setTime()
    }

    private fun Int.setTime(): String = if (this < 10) "0$this" else this.toString()

    fun updateHitPosition(currPos: Int) {
        try {
            val item = timeLineAdapter.items[currPos]
            Handler(Looper.getMainLooper()).postDelayed({
                val valuePower: Int = item?.force?.roundToInt() ?: 0
                tvCurrentPower?.text = if (valuePower == 0) "" else valuePower.toString()
            }, 150)
            if (item != null) {
                when (item.position) {
                    BlePosition.LEFT_CHEEK.key -> humanHeadLeft.show()
                    BlePosition.FACE.key -> humanHeadCenter.show()
                    BlePosition.RIGHT_CHEEK.key -> humanHeadRight.show()
                    BlePosition.RIGHT_CHEST.key -> humanChestRight.show()
                    BlePosition.LEFT_CHEST.key -> humanChestLeft.show()
                    BlePosition.LEFT_ABDOMEN.key -> humanHipLeft.show()
                    BlePosition.ABDOMEN_UP.key -> humanHipCenter.show()
                    BlePosition.ABDOMEN.key -> humanHipBottom.show()
                    BlePosition.RIGHT_ABDOMEN.key -> humanHipRight.show()
                    BlePosition.LEFT_LEG.key -> humanHipBottom1.show()
                    BlePosition.RIGHT_LEG.key -> humanHipBottom2.show()
                }
            } else {
                setInvisible(
                    humanHeadLeft,
                    humanHeadCenter,
                    humanHeadRight,
                    humanChestRight,
                    humanChestLeft,
                    humanHipLeft,
                    humanHipCenter,
                    humanHipBottom,
                    humanHipRight,
                    humanHipBottom1,
                    humanHipBottom2
                )
            }
        } catch (e: Exception) {
            e.logErr()
        }
    }

    private fun setInvisible(vararg arrView: View?) {
        arrView.forEach {
            it?.invisible()
        }
    }

    private fun pauseTimeline() {
        isRunning = false
        btnPlayTimeLine.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_video_player
            )
        )
        rvTimeLine.stopScroll()
    }

    private fun startTimeLine() {
        isRunning = true
        btnPlayTimeLine.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_video_pause
            )
        )
    }

    private fun autoScroll() {
        val speedScroll = 200L
        val handler = Handler(Looper.getMainLooper())
        val runnable: Runnable = object : Runnable {
            override fun run() {
                val lastPos =
                    (rvTimeLine.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (lastPos == timeLineAdapter.itemCount - 1) {
                    rvTimeLine.scrollToPosition(0)
                    pauseTimeline()
                    return
                }
                if (lastPos < timeLineAdapter.itemCount && isRunning) {
                    rvTimeLine.smoothScrollToPosition(timeLineAdapter.itemCount)
                    handler.postDelayed(this, speedScroll)
                }
            }
        }
        handler.postDelayed(runnable, speedScroll)
    }

}