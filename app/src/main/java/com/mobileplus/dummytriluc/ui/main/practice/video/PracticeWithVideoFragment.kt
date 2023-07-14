package com.mobileplus.dummytriluc.ui.main.practice.video

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.core.BaseFragmentZ
import com.google.android.exoplayer2.util.Util
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.databinding.FragmentPracticeWithVideoBinding
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.command.FinishCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLessonCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLessonNextCommand
import com.mobileplus.dummytriluc.transceiver.ext.getOrNull
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.transceiver.mode.SendCommandFrom
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.main.practice.dialog.ConfirmPracticeTestDialog
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadPracticeItem
import com.utils.ext.argument
import com.utils.ext.hide
import com.utils.ext.postNormal
import com.utils.ext.show
import com.widget.ViewPagerAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.LinkedList

/**
 * Created by KO Huyn on 05/07/2023.
 */
class PracticeWithVideoFragment : BaseFragmentZ<FragmentPracticeWithVideoBinding>(),
    IObserverMachine {

    companion object {
        fun openFragment(command: MachineLessonCommand) {
            val bundle = bundleOf(
                ARG_COMMAND to command,
            )
            postNormal(EventNextFragmentMain(PracticeWithVideoFragment::class.java, bundle, true))
        }

        private const val ARG_COMMAND = "ARG_COMMAND"
    }

    private val commandExecute by argument<MachineLessonCommand>(ARG_COMMAND)
    val listLesson by lazy { LinkedList(commandExecute.lessonId) }

    private val fragmentVideo by lazy { PracticeVideoGuideFragment() }
    private val fragmentPractice by lazy { PracticeCountDownFragment() }
    private val listFragment = listOf(fragmentVideo, fragmentPractice)
    private val adapter by lazy {
        ViewPagerAdapter(
            parentFragmentManager, listFragment,
            emptyList()
        )
    }
    private val transceiver by lazy { ITransceiverController.getInstance() }
    private val counter: MutableStateFlow<Int> = MutableStateFlow(0)


    override fun getLayoutBinding(): FragmentPracticeWithVideoBinding {
        return FragmentPracticeWithVideoBinding.inflate(layoutInflater)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        setupViewPager()
        handleClick()
        initCountTimer()
        transceiver.send(commandExecute)
        transceiver.onSendCommandStateListener(lifecycle) { state ->
            if (state.commandMode == CommandMode.LESSON_NEXT) {
                if (state is SendCommandFrom.FromMachine) {
                    val isSuccess = state.data.getOrNull<Boolean>("receive_success")
                    if (isSuccess != true) {
                        nextAction(fromApp = false)
                    }
                }
            }
            if (state.commandMode == CommandMode.FINISH) {
                when (state) {
                    is SendCommandFrom.FromApp -> {
                        showDialog()
                    }

                    is SendCommandFrom.FromMachine -> {
                        hideDialog()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        prepareVideo()
        if (Util.SDK_INT > 23) {
            fragmentVideo.restartVideo()
        }
        transceiver.registerObserver(this)
    }

    override fun onStop() {
        super.onStop()
        transceiver.removeObserver(this)
        if (Util.SDK_INT > 23) fragmentVideo.stopVideo()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            fragmentVideo.stopVideo()
        }
    }
    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            fragmentVideo.restartVideo()
        }
    }

    override fun onDestroy() {
        fragmentVideo.releasePlayer()
        super.onDestroy()
    }

    override fun onEventMachineSendData(data: List<BluetoothResponse>) {
        ConfirmPracticeTestDialog.Builder(data)
            .setTitle("")
            .setCancelable(false)
            .setShowButtonPlayAgain(false)
            .build(parentFragmentManager)
        postNormal(EventReloadPracticeItem())
        stopCounter()
    }

    fun setupViewPager() {
        binding.pagerContent.adapter = adapter
    }

    fun handleClick() {
        binding.tvContinue.setOnClickListener {
            nextAction()
        }

        binding.tvFinish.setOnClickListener {
            endCommand()
        }

        binding.tvBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnBackPracticeTest.setOnClickListener {
            onBackPressed()
        }
    }

    private fun endCommand() {
        transceiver.send(FinishCommand)
        binding.tvContinue.hide()
        binding.tvFinish.hide()
        binding.tvBack.show()
        stopCounter()
    }

    private fun nextAction(fromApp: Boolean = true) {
        val currentFragmentIndex = binding.pagerContent.currentItem
        if (listLesson.isEmpty()) {
            endCommand()
        } else {
            val data = listLesson.peek()
            if (currentFragmentIndex in 0 until adapter.count) {
                val nextIndex =
                    (currentFragmentIndex + 1).let { if (it >= adapter.count) 0 else it }

                when (val fragment = listFragment[nextIndex]) {
                    is PracticeVideoGuideFragment -> {
                        prepareVideo()
                        binding.pagerContent.currentItem = nextIndex
                        stopCounter()
                    }

                    is PracticeCountDownFragment -> {
                        if (data != null) {
                            listLesson.poll()
                            binding.pagerContent.currentItem = nextIndex
                            startCounter()
                        }
                    }
                }
            }

            if (fromApp) {
                val indexOfLessons =
                    commandExecute.lessonId.indexOfFirst { it.lessonId == data?.lessonId }
                        .takeIf { it >= 0 }
                if (indexOfLessons != null) {
                    transceiver.send(MachineLessonNextCommand(step = indexOfLessons.inc()))
                }
            }
        }
    }

    private fun prepareVideo() {
        val data = listLesson.peek()
        fragmentVideo.prepareMedia(data?.videoPath ?: "")
    }

    private fun initCountTimer() {
        lifecycleScope.launch {
            counter.collect { time ->
                val s = (time % 60).let { if (it < 10) "0$it" else it.toString() }
                val m = (time / 60).let { if (it < 10) "0$it" else it.toString() }
                fragmentPractice.updateCounter("$m:$s")
            }
        }
    }

    private var jobCounter: Job? = null
    private fun startCounter() {
        lifecycleScope.launch {
            while (isActive) {
                delay(1000)
                counter.emit(counter.value + 1)
            }
        }.let { jobCounter = it }
    }

    private fun stopCounter() {
        jobCounter?.cancel()
        jobCounter = null
    }
}