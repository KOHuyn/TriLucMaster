package com.mobileplus.dummytriluc.ui.video.record

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import com.core.BaseFragment
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.data.request.SubmitChallengeRequest
import com.mobileplus.dummytriluc.data.request.SubmitCoachDraftRequest
import com.mobileplus.dummytriluc.data.request.SubmitPracticeResultRequest
import com.mobileplus.dummytriluc.data.response.DataSendDraftResponse
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.command.FinishCommand
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.command.IPracticeCommand
import com.mobileplus.dummytriluc.transceiver.mode.CommandMode
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.coach.save_draft.CoachSaveDraftFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopVideo
import com.mobileplus.dummytriluc.ui.video.result.VideoResultActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import com.utils.ext.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_record_video.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Created by KO Huyn on 12/29/2020.
 */
class VideoRecordFragment : BaseFragment(), IObserverMachine {
    companion object {
        fun openFragment(command: IPracticeCommand) {
            val bundle = Bundle().apply {
                putParcelable(ARG_COMMAND, command)
            }
            postNormal(EventNextFragmentMain(VideoRecordFragment::class.java, bundle, true))
        }

        private const val ARG_COMMAND = "ARG_COMMAND"
    }
    override fun getLayoutId(): Int = R.layout.fragment_record_video
    private val vm by viewModel<VideoRecordViewModel>()
    //Camera
    private val cameraView: CameraView by lazy { cameraViewRecord }

    //PRACTICE
    private val practiceRequest by lazy { SubmitPracticeResultRequest() }

    //COACH
    private val coachRequest by lazy { SubmitCoachDraftRequest() }

    //CHALLENGE
    private val challengeRequest by lazy { SubmitChallengeRequest() }

    //GSON
    private val gson by inject<Gson>()
    //Rx
    private var isDataAvailable = false
    private var isTakenEndRecord = false
    private val rxRequestPostFragment: PublishSubject<Pair<Boolean, Boolean>> =
        PublishSubject.create()
    private var timerRecordDisposable: Disposable? = null
    private val transceiver by lazy { ITransceiverController.getInstance() }

    private val command by argument<IPracticeCommand>(ARG_COMMAND)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        checkConnectBle()
        showWarningExerciseRecord?.setVisibility(command.getCommandMode() == CommandMode.COACH)
        checkPermission()
        configCameraRecord()
        controlClick()
        disposableSubscribe()
    }

    private fun executeCommand(command: ICommand) {
        transceiver.send(command)
    }

    private fun checkConnectBle() {
        if (!transceiver.isConnected()) {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }

    private fun disposableSubscribe() {
        addDispose(rxRequestPostFragment.subscribe {
            if (it.first && it.second) {
                if (VideoResultActivity.videoResult != null) {
                    when (command.getCommandMode()) {
                        CommandMode.LESSON -> {
                            if (practiceRequest.data != null) {
                                nextFragmentResult()
                            }
                        }
                        CommandMode.COACH -> {
                            if (coachRequest.data != null) {
                                nextFragmentResult()
                            }
                        }
                        CommandMode.CHALLENGE -> {
                            if (challengeRequest.data != null) {
                                nextFragmentResult()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun fillDataFromBle(dataBle: BluetoothResponse) {
        when (command.getCommandMode()) {
            CommandMode.LESSON -> {
                practiceRequest.apply {
                    data =
                        if (dataBle.data.isEmpty()) null else gson.toJson(dataBle.data)
                    dataArr = listOf(dataBle)
                    dummyId = dataBle.machineId
                    practiceId = command.getIdPractice().toInt()
                    mode = dataBle.mode
                    startTime1 = dataBle.startTime1
                    startTime2 = dataBle.startTime2
                    videoPath = null
                    sessionId = dataBle.sessionId?.toString()
                }
            }
            CommandMode.COACH -> {
                coachRequest.apply {
                    startTime1 = dataBle.startTime1
                    data =
                        if (dataBle.data.isEmpty()) null else gson.toJson(dataBle.data)
                    startTime2 = dataBle.startTime2
                    videoPath = null
                }
            }
            CommandMode.CHALLENGE -> {
                challengeRequest.apply {
                    data =
                        if (dataBle.data.isEmpty()) null else gson.toJson(dataBle.data)
                    dummyId = dataBle.machineId
                    challengeId = command.getIdPractice().toInt()
                    mode = dataBle.mode
                    startTime1 = dataBle.startTime1
                    startTime2 = dataBle.startTime2
                    videoPath = null
                    sessionId = dataBle.sessionId?.toString()
                }
            }
        }
        isDataAvailable = true
        rxRequestPostFragment.onNext(Pair(isDataAvailable, isTakenEndRecord))
//        commandRequestBle(CommandBle.END)
    }

    private fun nextFragmentResult() {
        isDataAvailable = false
        when (command.getCommandMode()) {
            CommandMode.LESSON -> {
                val bundle = Bundle().apply {
                    putString(
                        VideoResultActivity.REQUEST_POST_VIDEO_PRACTICE_ARG,
                        gson.toJson(practiceRequest)
                    )
                }
                startActivity(VideoResultActivity::class.java, bundle)
                practiceRequest.data = null
            }
            CommandMode.COACH -> {
                val bundle = Bundle().apply {
                    putString(
                        VideoResultActivity.REQUEST_POST_VIDEO_COACH_ARG,
                        gson.toJson(coachRequest)
                    )
                }
                startActivity(VideoResultActivity::class.java, bundle)
                coachRequest.data = null
            }
            CommandMode.CHALLENGE -> {
                val bundle = Bundle().apply {
                    putString(
                        VideoResultActivity.REQUEST_POST_VIDEO_CHALLENGE_ARG,
                        gson.toJson(challengeRequest)
                    )
                }
                startActivity(VideoResultActivity::class.java, bundle)
                challengeRequest.data = null
            }
        }
    }

    private fun configCameraRecord() {
        btnCancelRecord.clickWithDebounce { onBackPressed() }
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoRecordingStart() {
                super.onVideoRecordingStart()
                if ((requireActivity() as MainActivity).isConnectedBle) {
                    commandRecordStart()
                    updateViewRecordStart()
                }
            }

            override fun onVideoRecordingEnd() {
                updateViewRecordEnd()
                commandRecordEnd()
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                VideoResultActivity.videoResult = result
                isTakenEndRecord = true
                rxRequestPostFragment.onNext(Pair(isDataAvailable, isTakenEndRecord))
            }
        })
    }

    private fun commandRecordStart() {
        val command = command
        executeCommand(command)
        when (command.getCommandMode()) {
            CommandMode.CHALLENGE -> {
                vm.startChallenge(command.getIdPractice().toInt())
            }
        }
    }

    private fun commandRecordEnd() {
        if (!isDataAvailable) {
            executeCommand(FinishCommand)
        }
        val timeEnd = System.currentTimeMillis() / 1000
        when (command.getCommandMode()) {
            CommandMode.LESSON -> {
                practiceRequest.endTime = timeEnd
            }
            CommandMode.COACH -> {
                coachRequest.endTime = timeEnd
            }
            CommandMode.CHALLENGE -> {
                challengeRequest.endTime = timeEnd
            }
        }
    }

    private fun updateViewRecordStart() {
        var count = 0
        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .repeat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                count++
                val s = if (count % 60 < 10) "0${count % 60}" else "${count % 60}"
                val m = if (count / 60 < 10) "0${count / 60}" else "${count / 60}"
                txtDurationRecord?.text = "$m:$s s"
            }.let { timerRecordDisposable = it }
    }

    private fun updateViewRecordEnd() {
        timerRecordDisposable?.dispose()
        timerRecordDisposable = null
        txtDurationRecord?.text = "00:00 s"
    }

    private fun disposableViewModel() {
        addDispose(
            vm.isLoading.observeOn(AndroidSchedulers.mainThread())
                .subscribe { if (it) showDialog() else hideDialog() })
    }

    private fun controlClick() {
        var isRotate = false
        btnCameraRotate.clickWithDebounce {
            if (cameraViewRecord.isTakingVideo) return@clickWithDebounce
            it.post {
                isRotate = !isRotate
                btnCameraRotate.animate()
                    .rotationY(if (isRotate) 180f else 0f)
                    .setDuration(500).start()
                cameraView.toggleFacing()
            }
        }

        btnActionRecord.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                cameraView.takeVideoSnapshot(
                    File(
                        ContextWrapper(context).filesDir,
                        "triluc${System.currentTimeMillis()}.mp4"
                    )
                )
            } else {
                if (cameraView.isTakingVideo) {
                    cameraView.stopVideo()
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !cameraView.isOpened) {
            cameraView.open()
        } else {
            onBackPressed()
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraView.open()
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    @Subscribe
    fun popVideo(ev: EventPopVideo) {
        parentFragmentManager.beginTransaction().remove(this).commit()
        parentFragmentManager.popBackStack()
    }

    @Subscribe
    fun openSavedDraft(ev: DataSendDraftResponse) {
        Handler(Looper.getMainLooper()).postDelayed({
            CoachSaveDraftFragment.openFragment(
                ev,
                gson
            )
        }, 500)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transceiver.registerObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister(this)
        transceiver.removeObserver(this)
        timerRecordDisposable?.dispose()
    }

    override fun onEventMachineSendData(data: List<BluetoothResponse>) {
        if (data.isNotEmpty()) {
            fillDataFromBle(data.first())
        }
    }
}