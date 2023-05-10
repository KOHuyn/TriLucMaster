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
import com.mobileplus.dummytriluc.bluetooth.CommandBle
import com.mobileplus.dummytriluc.data.request.SubmitChallengeRequest
import com.mobileplus.dummytriluc.data.request.SubmitCoachDraftRequest
import com.mobileplus.dummytriluc.data.request.SubmitPracticeResultRequest
import com.mobileplus.dummytriluc.data.response.DataSendDraftResponse
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.main.coach.save_draft.CoachSaveDraftFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopVideo
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
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
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by KO Huyn on 12/29/2020.
 */
class VideoRecordFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_record_video
    private val videoRecordViewModel by viewModel<VideoRecordViewModel>()

    //Camera
    private val cameraView: CameraView by lazy { cameraViewRecord }

    //Ble
    private fun commandRequestBle(command: String) =
        (requireActivity() as MainActivity).actionWriteBle(command)

    private fun commandLongRequestBle(command: String) =
        (requireActivity() as MainActivity).actionWriteBle(command)

    //TYPE
    private var typeRecord: Int = AppConstants.INTEGER_DEFAULT

    //PRACTICE
    private val practiceRequest by lazy { SubmitPracticeResultRequest() }

    //COACH
    private val coachRequest by lazy { SubmitCoachDraftRequest() }

    //CHALLENGE
    private val challengeRequest by lazy { SubmitChallengeRequest() }

    //GSON
    private val gson by inject<Gson>()

    //POSITION
    private var positionArr: String = ""
    private var delayPositionArr: String = ""

    //ID truyền vào máy tập theo bài
    private var idBle: Int = AppConstants.INTEGER_DEFAULT

    private var cmdRequestBleChallenge: ChallengeDetailFragment.RequestBleChallenge? = null

    //Rx
    private var isDataAvailable = false
    private var isTakenEndRecord = false
    private val rxRequestPostFragment: PublishSubject<Pair<Boolean, Boolean>> =
        PublishSubject.create()
    private var timerRecordDisposable: Disposable? = null

    companion object {
        private const val KEY_ID_LESSON_ARG_RECORD = "KEY_ID_LESSON_ARG_RECORD"
        private const val KEY_POSITION_LESSON_ARG_RECORD = "KEY_POSITION_LESSON_ARG_RECORD"
        private const val KEY_DELAY_LESSON_ARG_RECORD = "KEY_DELAY_LESSON_ARG_RECORD"
        private const val KEY_DATA_CHALLENGE_ARG_RECORD = "KEY_DATA_CHALLENGE_ARG_RECORD"
        private const val KEY_DATA_COACH_ARG_RECORD = "KEY_DATA_COACH_ARG_RECORD"
        private const val TYPE_PRACTICE = 1
        private const val TYPE_COACH = 2
        private const val TYPE_CHALLENGE = 3

        fun openFromPractice(idPractice: Int, delayPositionArr: String, positionArr: String) {
            val bundle = Bundle().apply {
                putString(
                    KEY_POSITION_LESSON_ARG_RECORD,
                    positionArr
                )
                putString(
                    KEY_DELAY_LESSON_ARG_RECORD,
                    delayPositionArr
                )
                putInt(
                    KEY_ID_LESSON_ARG_RECORD,
                    idPractice
                )
            }
            postNormal(EventNextFragmentMain(VideoRecordFragment::class.java, bundle, true))
        }

        fun openFromChallenge(request: ChallengeDetailFragment.RequestBleChallenge, gson: Gson) {
            val bundle = Bundle().apply {
                putString(
                    KEY_DATA_CHALLENGE_ARG_RECORD,
                    gson.toJson(request)
                )
            }
            postNormal(EventNextFragmentMain(VideoRecordFragment::class.java, bundle, true))
        }

        fun openFromCoach() {
            val bundle = Bundle().apply {
                putString(KEY_DATA_COACH_ARG_RECORD, "")
            }
            postNormal(EventNextFragmentMain(VideoRecordFragment::class.java, bundle, true))
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        checkConnectBle()
        checkArg()
        checkPermission()
        configCameraRecord()
        controlClick()
        disposableSubscribe()
    }

    private fun checkConnectBle() {
        if (!(activity as MainActivity).isConnectedBle) {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }

    private fun disposableSubscribe() {
        addDispose(rxRequestPostFragment.subscribe {
            if (it.first && it.second) {
                if (VideoResultActivity.videoResult != null) {
                    when (typeRecord) {
                        TYPE_PRACTICE -> {
                            if (practiceRequest.data != null) {
                                nextFragmentResult()
                            }
                        }
                        TYPE_COACH -> {
                            if (coachRequest.data != null) {
                                nextFragmentResult()
                            }
                        }
                        TYPE_CHALLENGE -> {
                            if (challengeRequest.data != null) {
                                nextFragmentResult()
                            }
                        }
                    }
                }
            }
        })
        addDispose(
            (activity as MainActivity).rxCallbackDataBle.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val isSuccess = it.first
                    val data = it.second
                    if (isSuccess) {
                        if (data.isNotEmpty()) {
                            fillDataFromBle(data.first())
                        }
                    } else {
//                        toast(loadStringRes(R.string.data_not_available))
                    }
                })
    }

    private fun fillDataFromBle(dataBle: BluetoothResponse) {
        when (typeRecord) {
            TYPE_PRACTICE -> {
                practiceRequest.apply {
                    data =
                        if (dataBle.data.isEmpty()) null else gson.toJson(dataBle.data)
                    dummyId = dataBle.machineId
                    practiceId = idBle
                    mode = dataBle.mode
                    startTime1 = dataBle.startTime1
                    startTime2 = dataBle.startTime2
                    videoPath = null
                }
            }
            TYPE_COACH -> {
                coachRequest.apply {
                    startTime1 = dataBle.startTime1
                    data =
                        if (dataBle.data.isEmpty()) null else gson.toJson(dataBle.data)
                    startTime2 = dataBle.startTime2
                    videoPath = null
                }
            }
            TYPE_CHALLENGE -> {
                challengeRequest.apply {
                    data =
                        if (dataBle.data.isEmpty()) null else gson.toJson(dataBle.data)
                    dummyId = dataBle.machineId
                    challengeId = idBle
                    mode = dataBle.mode
                    startTime1 = dataBle.startTime1
                    startTime2 = dataBle.startTime2
                    videoPath = null
                }
            }
        }
        isDataAvailable = true
        rxRequestPostFragment.onNext(Pair(isDataAvailable, isTakenEndRecord))
//        commandRequestBle(CommandBle.END)
    }

    private fun checkArg() {
        if (arguments != null) {
            when {
                //PRACTICE
                requireArguments().containsKey(KEY_POSITION_LESSON_ARG_RECORD)
                        && requireArguments().containsKey(KEY_ID_LESSON_ARG_RECORD) -> {
                    typeRecord = TYPE_PRACTICE
                    positionArr =
                        argument(KEY_POSITION_LESSON_ARG_RECORD, "").value
                    delayPositionArr = argument(KEY_DELAY_LESSON_ARG_RECORD, "").value
                    idBle = argument(KEY_ID_LESSON_ARG_RECORD, -1).value
                }
                //COACH
                requireArguments().containsKey(KEY_DATA_COACH_ARG_RECORD) -> {
                    typeRecord = TYPE_COACH
                }
                //CHALLENGE
                requireArguments().containsKey(KEY_DATA_CHALLENGE_ARG_RECORD) -> {
                    typeRecord = TYPE_CHALLENGE
                    cmdRequestBleChallenge = gson.fromJson(
                        argument(KEY_DATA_CHALLENGE_ARG_RECORD, "").value,
                        ChallengeDetailFragment.RequestBleChallenge::class.java
                    )
                    positionArr = cmdRequestBleChallenge?.positionLimit ?: "0"
                    idBle = cmdRequestBleChallenge?.challengeId ?: AppConstants.INTEGER_DEFAULT
                }
            }
            showWarningExerciseRecord?.setVisibility(typeRecord == TYPE_COACH)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                onBackPressed()
            }, 300)
            toast(loadStringRes(R.string.feature_not_available))
        }
    }

    private fun nextFragmentResult() {
        isDataAvailable = false
        when (typeRecord) {
            TYPE_PRACTICE -> {
                val bundle = Bundle().apply {
                    putString(
                        VideoResultActivity.REQUEST_POST_VIDEO_PRACTICE_ARG,
                        gson.toJson(practiceRequest)
                    )
                }
                startActivity(VideoResultActivity::class.java, bundle)
                practiceRequest.data = null
            }
            TYPE_COACH -> {
                val bundle = Bundle().apply {
                    putString(
                        VideoResultActivity.REQUEST_POST_VIDEO_COACH_ARG,
                        gson.toJson(coachRequest)
                    )
                }
                startActivity(VideoResultActivity::class.java, bundle)
                coachRequest.data = null
            }
            TYPE_CHALLENGE -> {
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
        val timeL = System.currentTimeMillis() / 1000
        when (typeRecord) {
            TYPE_PRACTICE -> {
                videoRecordViewModel.getAvgPractice(idBle)
            }
            TYPE_COACH -> {
                commandRequestBle(String.format(CommandBle.COACH_MODE, timeL))
            }
            TYPE_CHALLENGE -> {
                videoRecordViewModel.startChallenge(idBle)
                cmdRequestBleChallenge?.run {
                    commandRequestBle(
                        String.format(
                            CommandBle.CHALLENGE_MODE,
                            timeL,
                            challengeId,
                            hittingType,
                            hitData,
                            hitLimit,
                            timeLimit,
                            positionLimit ?: "0",
                            weight,
                            minPower,
                            randomDelayTime,
                        )
                    )
                }
            }
        }
    }

    private fun commandRecordEnd() {
        if (!isDataAvailable) (activity as MainActivity).requestFINISH()
        val timeEnd = System.currentTimeMillis() / 1000
        when (typeRecord) {
            TYPE_PRACTICE -> {
                practiceRequest.endTime = timeEnd
            }
            TYPE_COACH -> {
                coachRequest.endTime = timeEnd
            }
            TYPE_CHALLENGE -> {
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
            videoRecordViewModel.isLoading.observeOn(AndroidSchedulers.mainThread())
                .subscribe { if (it) showDialog() else hideDialog() })
        addDispose(videoRecordViewModel.rxAvgResponse.subscribe {
            val (isSuccess, avg) = it
            commandLongRequestBle(
                String.format(
                    CommandBle.LESSON_MODE,
                    System.currentTimeMillis() / 1000,
                    idBle,
                    positionArr,
                    delayPositionArr,
                    avg?.avgPower ?: 0,
                    avg?.avgHit ?: 0
                )
            )
        })
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

    override fun onDestroy() {
        super.onDestroy()
        unregister(this)
        timerRecordDisposable?.dispose()
    }
}