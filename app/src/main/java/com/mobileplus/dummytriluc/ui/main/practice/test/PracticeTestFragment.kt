package com.mobileplus.dummytriluc.ui.main.practice.test

import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.databinding.FragmentPracticeTestBinding
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.command.FinishCommand
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.dialog.SelectTimePracticeDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.practice.dialog.ConfirmPracticeTestDialog
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.applyClickShrink
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.hide
import com.utils.ext.postNormal
import com.utils.ext.show
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class PracticeTestFragment : BaseFragmentZ<FragmentPracticeTestBinding>(), IObserverMachine {

    override fun getLayoutBinding(): FragmentPracticeTestBinding {
        return FragmentPracticeTestBinding.inflate(layoutInflater)
    }

    private val testViewModel by viewModel<PracticeTestViewModel>()

    private val transceiver by lazy { ITransceiverController.getInstance() }
    private fun executeCommand(command: ICommand): Boolean {
        transceiver.send(command)
        return true
    }

    private var countDownStart: CountDownTimer? = null
    private var countDownEnd: CountDownTimer? = null
    private var countTimesDisposable: Disposable? = null

    enum class ActionMode {
        START, END, RETRY;

        fun nextAction(): ActionMode {
            return when (this) {
                START -> END
                END -> RETRY
                RETRY -> END
            }
        }
        companion object {
            fun getDefault() = START
        }
    }

    private var actionMode: ActionMode = ActionMode.getDefault()
        set(value) {
            field = value
            runOnUiThread {
                setUiWithActionMode(value)
            }
        }


    private val commandExecute by argument<ICommand>(ARG_COMMAND)

    fun isDoingPractice(): Boolean = actionMode == ActionMode.END

    private fun setUiWithActionMode(action: ActionMode) {
        binding.btnBackPracticeTest.isGone = action == ActionMode.END
        binding.btnActionPracticeTest.isVisible = true
        binding.btnActionPracticeTest.text = when (action) {
            ActionMode.START -> loadStringRes(R.string.start)
            ActionMode.END -> loadStringRes(R.string.end)
            ActionMode.RETRY -> loadStringRes(R.string.retry)
        }
    }

    companion object {
        fun openFragment(command: ICommand) {
            val bundle = bundleOf(
                ARG_COMMAND to command,
            )
            postNormal(
                EventNextFragmentMain(PracticeTestFragment::class.java, bundle, true)
            )
        }

        private const val ARG_COMMAND = "ARG_COMMAND"
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableSubscribe()
        handleClick()
        startPractice()
    }
    private fun handleClick() {
        binding.btnActionPracticeTest.applyClickShrink()
        binding.btnActionPracticeTest.fillGradientPrimary()
        binding.btnActionPracticeTest.clickWithDebounce {
            if (transceiver.isConnected()) {
                if (isDoingPractice()) {
                    executeCommand(FinishCommand)
                    endPractice()
                } else {
                    startPractice()
                }
            } else {
                (activity as? MainActivity)?.showDialogRequestConnect()
            }
        }

        binding.btnBackPracticeTest.clickWithDebounce {
            onBackPressed()
        }
    }

    private fun startPractice() {
        if (transceiver.isConnected()) {
            if (commandExecute.getCommandMode().isShowTimeOut()) {
                startCountDownBySetting()
            } else {
                executeStartPractice()
            }
        } else {
            (activity as? MainActivity)?.showDialogRequestConnect()
        }
    }

    private fun endPractice() {
        cancelTimeCounter()
        cancelCountDownBySetting()
        actionMode = ActionMode.RETRY
    }

    private fun executeStartPractice() {
        startTimeCounterReady {
            actionMode = ActionMode.END
            startRecord()
            startTimeCounter()
        }
    }

    private fun startCountDownBySetting() {
        SelectTimePracticeDialog()
            .onChooseListener { timeMs ->
                executeStartPractice()
                countDownEnd = object : CountDownTimer(((timeMs + 3) * 1000).toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        executeCommand(FinishCommand)
                        endPractice()
                    }
                }.start()
            }.onDismiss {
                actionMode = ActionMode.START
            }
            .show(parentFragmentManager, "SelectTimePracticeDialog")
    }

    private fun cancelCountDownBySetting() {
        if (countDownEnd != null) {
            countDownEnd?.cancel()
            countDownEnd = null
        }
    }
    private fun startTimeCounterReady(onFinish: () -> Unit) {
        try {
            countDownStart = object : CountDownTimer(3200, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.btnActionPracticeTest.hide()
                    if (millisUntilFinished > 1000) {
                        binding.txtTimeCountPracticeTest.text =
                            (millisUntilFinished / 1000).toString()
                    }
                }

                override fun onFinish() {
                    binding.txtTimeCountPracticeTest.text = "00:00s"
                    binding.btnActionPracticeTest.show()
                    onFinish()
                }
            }
            countDownStart?.start()
        } catch (e: Exception) {
            e.logErr()
        }
    }

    private fun cancelTimeCounterReady() {
        if (countDownStart != null) {
            countDownStart?.cancel()
            countDownStart = null
        }
    }

    private fun startTimeCounter() {
        var count = 0
        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                count++
                val s =
                    if (count % 60 < 10) "0${count % 60}" else "${count % 60}"
                val m =
                    if (count / 60 < 10) "0${count / 60}" else "${count / 60}"
                binding.txtTimeCountPracticeTest.text = String.format("%s:%ss", m, s)
            }.let { countTimesDisposable = it }
    }

    private fun cancelTimeCounter() {
        countTimesDisposable?.dispose()
        countTimesDisposable = null
    }

    private fun startRecord(): Boolean {
        return executeCommand(commandExecute)
    }

    private fun disposableSubscribe() {
        addDispose(
            testViewModel.isLoading.observeOn(AndroidSchedulers.mainThread())
                .subscribe { if (it) showDialog() else hideDialog() })
        addDispose(testViewModel.rxMessage.subscribe { toast(it) })
    }

    override fun onDestroy() {
        cancelTimeCounterReady()
        cancelTimeCounter()
        cancelCountDownBySetting()
        super.onDestroy()
    }

    override fun onEventMachineSendData(data: List<BluetoothResponse>) {
        lifecycleScope.launchWhenStarted {
            val title = when (commandExecute.getCommandMode()) {
                CommandMode.FREE_FIGHT -> R.string.free_fight
                CommandMode.ACCORDING_LED -> R.string.according_to_led
                CommandMode.PLAY_WITH_MUSIC -> R.string.according_to_music
                CommandMode.RELAX_HUSBAND -> R.string.beat_husband
                CommandMode.RELAX_LOVE_ENEMY -> R.string.beat_love_enemy
                CommandMode.RELAX_EX -> R.string.beat_ex
                CommandMode.RELAX_BOSS -> R.string.beat_boss
                CommandMode.LESSON -> R.string.according_to_course
                else -> null
            }?.let { getString(it) } ?: "---"
            ConfirmPracticeTestDialog.Builder(data)
                .setTitle(title)
                .setCancelable(false)
                .setModeCourse(commandExecute.getCommandMode() == CommandMode.LESSON)
                .setShowButtonPlayAgain(true)
                .build(parentFragmentManager)
                .setSubmitPracticeCallback { isSubmit ->
                    if (!isSubmit) {
                        startPractice()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        transceiver.registerObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        transceiver.removeObserver(this)
    }
}