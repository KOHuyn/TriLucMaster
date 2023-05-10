package com.mobileplus.dummytriluc.ui.video.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import com.core.BaseActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.request.SubmitChallengeRequest
import com.mobileplus.dummytriluc.data.request.SubmitCoachDraftRequest
import com.mobileplus.dummytriluc.data.request.SubmitPracticeResultRequest
import com.mobileplus.dummytriluc.data.response.DataPracticeChallenge
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.ui.dialog.SaveDraftSuccessDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.ExoUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopVideo
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPostVideoSuccess
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventSendVideoPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.video.fullscreen.VideoFullScreenActivity
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.otaliastudios.cameraview.VideoResult
import com.utils.ext.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_video_result.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.layout_human.view.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.*
import java.util.concurrent.TimeUnit


/**
 * Created by KO Huyn on 12/18/2020.
 */
class VideoResultActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_video_result

    private val videoResultViewModel by viewModel<VideoResultViewModel>()
    private val gson: Gson by inject()

    //initial exoplayer
    private val playerView: PlayerView by lazy { videoViewFinish }
    private var exoPlayer: SimpleExoPlayer? = null
    private var uriVideo: Uri? = null
    private val mediaDataSource: DataSource.Factory by lazy {
        DefaultDataSourceFactory(
            this@VideoResultActivity,
            Util.getUserAgent(
                this@VideoResultActivity,
                DummyTriLucApplication.getInstance().applicationContext.applicationInfo.packageName
            )
        )
    }

    //save state
    private var startPosition: Long = 0
    private var speedParameter: Float = 1f
    private var autoStart: Boolean = true

    //argument
    private var typePostVideo = AppConstants.INTEGER_DEFAULT
    private var requestCoach: SubmitCoachDraftRequest? = null
    private var requestPractice: SubmitPracticeResultRequest? = null
    private var requestChallenge: SubmitChallengeRequest? = null

    //callback data từ máy tập lắng nghe duration video
    private val rxDurationCallback: PublishSubject<Long> = PublishSubject.create()
    private var durationDisposable: Disposable? = null
    private val isListenerDurationChange: Boolean
        get() = durationDisposable != null

    companion object {
        var videoResult: VideoResult? = null

        //argument
        const val ID_DRAFT = "id_draft_coach_activity"
        const val REQUEST_POST_VIDEO_COACH_ARG = "request_coach_activity"
        const val REQUEST_POST_VIDEO_PRACTICE_ARG = "request_practice_activity"
        const val REQUEST_POST_VIDEO_CHALLENGE_ARG = "request_challenge_activity"
        const val PRACTICE_FAQ_URL = "urlFaqPractice"
        const val PRACTICE_FAQ_START_TIME = "START_TIME_FAQ_PRACTICE"
        const val PRACTICE_FAQ_DATA = "DATA_FAQ_PRACTICE"
        const val DATA_REPLAY_CHALLENGE = "data_replay_challenge"
        const val PRACTICE_VIEW_DATA = "practice_view_data"

        //type
        const val TYPE_PRACTICE = 1
        const val TYPE_COACH = 2
        const val TYPE_CHALLENGE = 3

        // Saved instance state keys.
        private const val KEY_SPEED_PARAMETERS = "speed_selector_parameters"
        private const val KEY_POSITION = "position"
        private const val KEY_AUTO_PLAY = "auto_play"

        //request code
        private const val KEY_REQUEST_CODE_FULL_SCREEN = 23

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_POSITION, startPosition)
        outState.putBoolean(KEY_AUTO_PLAY, autoStart)
        outState.putFloat(KEY_SPEED_PARAMETERS, speedParameter)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            startPosition = savedInstanceState.getLong(KEY_POSITION)
            autoStart = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            speedParameter = savedInstanceState.getFloat(KEY_SPEED_PARAMETERS)
        }
        with(humanVideoResult) {
            layoutPositionScore.show()
        }
        getArg()
        handleDisposableViewModel(videoResultViewModel)
        btnSendRecordVideoResult.fillGradientPrimary()
        controlButton()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            ViewPumpContextWrapper.wrap(LocalManageUtil.setLocal(newBase) ?: newBase)
        )
    }

    private fun getArg() {
        if (intent != null) {
            when {
                //chuyển màn hình từ bài tập nháp
                intent!!.hasExtra(ID_DRAFT) -> {
                    val id: Int = intent.getIntExtra(ID_DRAFT, AppConstants.INTEGER_DEFAULT)
                    videoResultViewModel.getDetailDraft(id)
                    layoutBottomControlVideoResult.hide()
                }
                //chuyển màn hình sau khi quay bài tập nháp ở mode huấn luyện viên
                intent!!.hasExtra(REQUEST_POST_VIDEO_COACH_ARG) -> {
                    typePostVideo = TYPE_COACH
                    val result = videoResult ?: kotlin.run {
                        onBackPressed()
                        return
                    }
                    uriVideo = Uri.fromFile(result.file)
                    try {
                        val dataMachineResponse =
                            intent.getStringExtra(REQUEST_POST_VIDEO_COACH_ARG)
                        requestCoach =
                            gson.fromJson(dataMachineResponse, SubmitCoachDraftRequest::class.java)
                        if (requestCoach!!.data != null && requestCoach!!.startTime2 != null) {
                            setupDataBluetoothFromMachine(
                                gson.toList(requestCoach!!.data!!),
                                requestCoach!!.startTime2!!
                            )
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
                //chuyển màn hình từ phần câu hỏi trong luyện tập
                intent!!.hasExtra(PRACTICE_FAQ_URL) -> {
                    layoutBottomControlVideoResult.hide()
                    val dataMachineResponse = intent.getStringExtra(PRACTICE_FAQ_DATA)!!
                    val startTime2 = intent.getLongExtra(PRACTICE_FAQ_START_TIME, 0)
                    val url = intent.getStringExtra(PRACTICE_FAQ_URL)

                    val dataBle =
                        gson.fromJsonPretty<List<DataBluetooth>>(dataMachineResponse)
                    uriVideo = Uri.parse(url)
                    dataBle?.let { setupDataBluetoothFromMachine(dataBle, startTime2) }
                }
                //chuyển màn hình sau khi quay luyện tập
                intent!!.hasExtra(REQUEST_POST_VIDEO_PRACTICE_ARG) -> {
                    typePostVideo = TYPE_PRACTICE
                    val result = videoResult ?: kotlin.run {
                        onBackPressed()
                        return
                    }
                    uriVideo = Uri.fromFile(result.file)
                    try {
                        val dataMachineResponse =
                            intent.getStringExtra(REQUEST_POST_VIDEO_PRACTICE_ARG)
                        requestPractice =
                            gson.fromJson(
                                dataMachineResponse,
                                SubmitPracticeResultRequest::class.java
                            )
                        if (requestPractice!!.data != null && requestPractice!!.startTime2 != null) {
                            val data = if (requestPractice!!.data!!.contains("[null,")) {
                                requestPractice!!.data!!.replace("[null,", "[")
                            } else {
                                requestPractice!!.data!!
                            }
                            logErr(data)
                            setupDataBluetoothFromMachine(
                                gson.toList(data),
                                requestPractice!!.startTime2!!
                            )
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }

                //Chuyển màn sau khi record ở màn thách đấu
                intent.hasExtra(REQUEST_POST_VIDEO_CHALLENGE_ARG) -> {
                    typePostVideo = TYPE_CHALLENGE
                    val result = videoResult ?: kotlin.run {
                        onBackPressed()
                        return
                    }
                    uriVideo = Uri.fromFile(result.file)
                    try {
                        val dataMachineResponse =
                            intent.getStringExtra(REQUEST_POST_VIDEO_CHALLENGE_ARG)
                        requestChallenge =
                            gson.fromJson(
                                dataMachineResponse,
                                SubmitChallengeRequest::class.java
                            )
                        if (requestChallenge!!.data != null && requestChallenge!!.startTime2 != null) {
                            val data = if (requestChallenge!!.data!!.contains("[null,")) {
                                requestChallenge!!.data!!.replace("[null,", "[")
                            } else
                                requestChallenge!!.data!!
                            logErr(data)
                            setupDataBluetoothFromMachine(
                                gson.toList(data),
                                requestChallenge!!.startTime2!!
                            )
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
                //Chuyển màn xem lại thách đấu
                intent.hasExtra(DATA_REPLAY_CHALLENGE) -> {
                    layoutBottomControlVideoResult.hide()

                    val detailChallengeJson = intent.getStringExtra(DATA_REPLAY_CHALLENGE)
                    try {
                        val dataChallenge: DataPracticeChallenge =
                            gson.fromJson(detailChallengeJson, DataPracticeChallenge::class.java)
                        if (dataChallenge.data != null && dataChallenge.start_time2 != null) {
                            val dataBle =
                                gson.toList<DataBluetooth>(dataChallenge.data)
                            setupDataBluetoothFromMachine(dataBle, dataChallenge.start_time2)
                        }
                        dataChallenge.video_path?.let { uriVideo = Uri.parse(it) }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
                //Chuyển màn xem video bài tập
                intent.hasExtra(PRACTICE_VIEW_DATA) -> {
                    layoutBottomControlVideoResult.hide()

                    val detailPracticeJson = intent.getStringExtra(PRACTICE_VIEW_DATA)
                    try {
                        val dataPractice: DetailPracticeResponse =
                            gson.fromJson(detailPracticeJson, DetailPracticeResponse::class.java)
                        if (dataPractice.data != null && dataPractice.startTime2 != null) {
                            val dataBle =
                                gson.toList<DataBluetooth>(dataPractice.data)
                            setupDataBluetoothFromMachine(dataBle, dataPractice.startTime2)
                        }
                        dataPractice.videoPath?.let { uriVideo = Uri.parse(it) }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }
        }
    }

    private fun controlButton() {
        repeatVideoResult.clickWithDebounce {
            replayVideo()
        }
        playVideoResult.clickWithDebounce {
            playVideo()
        }
        pauseVideoResult.clickWithDebounce {
            pauseVideo()
        }
        btnSaveRecordVideoResult.clickWithDebounce {
            uriVideo?.let { uri ->
                saveFileFromUri(this, uri)
            }
        }
        var isShowHuman = true
        showHumanVideo?.clickWithDebounce {
            isShowHuman = !isShowHuman
            if (isShowHuman) {
                showHumanVideo?.setImageResource(R.drawable.ic_next)
            } else {
                showHumanVideo?.setImageResource(R.drawable.ic_back)
            }
            layoutHumanVideo?.setVisibility(isShowHuman)
        }
        btnBackVideoRecordResult.clickWithDebounce { onBackPressed() }
        btnRecordRepeatCoach.clickWithDebounce { onBackPressed() }
        btnSendRecordVideoResult.clickWithDebounce {
            if (typePostVideo != AppConstants.INTEGER_DEFAULT) {
                when (typePostVideo) {
                    TYPE_PRACTICE -> {
                        submitPractice()
                    }
                    TYPE_COACH -> {
                        submitCoach()
                    }
                    TYPE_CHALLENGE -> {
                        submitChallenge()
                    }
                }
            } else {
                toast(loadStringRes(R.string.error_unknown_error))
            }
        }
    }

    private fun handleDisposableViewModel(vm: VideoResultViewModel) {
        addDispose(
            vm.isLoading.subscribe {
                if (it) showDialog() else hideDialog()
            }
        )

        addDispose(vm.rxPushIdPractice.subscribe {
            val idPracticeVideo = it.first
            val dummyResult = it.second
            postNormal(EventSendVideoPractice(idPracticeVideo, dummyResult))
        })

        addDispose(vm.rxDraftResponse.subscribe { response ->
            finish()
            postNormal(response)
        })

        addDispose(vm.rxSubmitSuccess.subscribe { isSuccess ->
            if (isSuccess) {
                btnSendRecordVideoResult.isEnabled = false
                btnSendRecordVideoResult.alpha = 0.5f
                btnSendRecordVideoResult.text = loadStringRes(R.string.sent)
                postNormal(EventPostVideoSuccess())
                if (typePostVideo == TYPE_PRACTICE || typePostVideo == TYPE_CHALLENGE) {
                    SaveDraftSuccessDialog()
                        .setCancelableDialog(false)
                        .setTitleSaved(
                            when (typePostVideo) {
                                TYPE_PRACTICE -> {
                                    loadStringRes(R.string.title_question_answer)
                                }
                                TYPE_COACH -> {
                                    loadStringRes(R.string.draft_exercise)
                                }
                                TYPE_CHALLENGE -> {
                                    loadStringRes(R.string.my_challenge)
                                }
                                else -> ""
                            }
                        ).setCallbackConfirm {
                            onBackPressed()
                            postNormal(EventPopVideo())
                        }.show(supportFragmentManager)
                }
            }
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxDetailDraftResponse.subscribe { response ->
            try {
                uriVideo = Uri.parse(response.videoPath)
                setUpExoPlayer(uriVideo)
                setupDataBluetoothFromMachine(gson.toList(response.data), response.startTime2)
                logErr(response.data)
            } catch (e: Exception) {
                e.logErr()
            }
        })
    }

    private fun submitCoach() {
        if (videoResult != null && requestCoach != null) {
            videoResultViewModel.submitCoachDraft(videoResult!!.file, requestCoach!!)
        } else {
            toast(loadStringRes(R.string.error_unknown_error))
        }
    }

    private fun submitPractice() {
        if (videoResult != null && requestPractice != null) {
            videoResultViewModel.submitPracticeResult(videoResult!!.file, requestPractice!!)
        } else {
            toast(loadStringRes(R.string.error_unknown_error))
        }
    }

    private fun submitChallenge() {
        if (videoResult != null && requestChallenge != null) {
            YesNoButtonDialog()
                .setTitle(getString(R.string.save_challenge))
                .setMessage(getString(R.string.do_you_want_save_challenge))
                .setTextAccept(getString(R.string.yes))
                .setTextCancel(getString(R.string.no))
                .showDialog(supportFragmentManager, YesNoButtonDialog.TAG)
                .setOnCallbackAcceptButtonListener {
                    videoResultViewModel.submitChallenge(videoResult!!.file, requestChallenge!!)
                }
        } else {
            toast(loadStringRes(R.string.error_unknown_error))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY_REQUEST_CODE_FULL_SCREEN) {
            if (resultCode == Activity.RESULT_OK) {
                startPosition =
                    data?.getLongExtra(VideoFullScreenActivity.ARG_POSITION_SCREEN_FULL, 0L) ?: 0L
                autoStart =
                    data?.getBooleanExtra(VideoFullScreenActivity.ARG_AUTO_START_SCREEN_FULL, false)
                        ?: false
                speedParameter =
                    data?.getFloatExtra(VideoFullScreenActivity.ARG_SPEED_SCREEN_FULL, 1f) ?: 1f
            }
        }
    }

    private fun saveFileFromUri(context: Context, uri: Uri) {
        val childPath = "TriLuc"
        val fileOutput =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/$childPath",
                "triluc${System.currentTimeMillis()}.mp4"
            )
        if (!fileOutput.exists()) {
            logErr("Folder does'nt exist,creating it")
            val rv = fileOutput.parentFile.mkdir()
            logErr("created:$rv")
        }
        logErr(fileOutput.toString())

        var inputStream: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(fileOutput, false))
            val buf = ByteArray(1024)
            inputStream!!.read(buf)
            do {
                bos.write(buf)
            } while (inputStream.read(buf) != -1)
        } catch (e: IOException) {
            e.logErr()
        } finally {
            try {
                inputStream?.close()
                bos?.close()
                toast(getString(R.string.save_video_success))
            } catch (e: IOException) {
                e.logErr()
            }
        }
    }

    private fun setUpExoPlayer(uri: Uri?) {
        if (exoPlayer == null && uri != null) {
            val mediaSource =
                ProgressiveMediaSource.Factory(mediaDataSource).createMediaSource(uri)
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
            exoPlayer?.let {
                it.playWhenReady = autoStart
                it.playbackParameters = PlaybackParameters(speedParameter)
                it.seekTo(startPosition)
            }
            playerView.player = exoPlayer
            exoPlayer?.prepare(mediaSource, false, false)
            playerView.requestFocus()
            with(playerView) {
                btnTimeSpeed.text = when (speedParameter) {
                    1f -> "1x"
                    0.5f -> "0.5x"
                    0.25f -> "0.25x"
                    0.125f -> "0.125x"
                    else -> ""
                }
                btnTimeSpeed.clickWithDebounce {
                    CustomSpinner(it, this@VideoResultActivity)
                        .setBackGroundSpinner(Color.parseColor("#2C2C2C"))
                        .setShowUp(true)
                        .setTextSize(resources.getDimension(R.dimen.text_10))
                        .setWidthWindow(resources.getDimension(R.dimen._50sdp))
                        .setDataSource(ExoUtils.listSpeedParameter).build()
                        .setOnSelectedItemCallback { item ->
                            exoPlayer?.playbackParameters = PlaybackParameters(item.id.toFloat())
                        }
                }
                btnFullScreen.clickWithDebounce {
                    val bundle = Bundle().apply {
                        putLong(
                            VideoFullScreenActivity.ARG_POSITION_SCREEN_FULL,
                            exoPlayer!!.currentPosition
                        )
                        putFloat(
                            VideoFullScreenActivity.ARG_SPEED_SCREEN_FULL,
                            exoPlayer!!.playbackParameters.speed
                        )
                        putBoolean(
                            VideoFullScreenActivity.ARG_AUTO_START_SCREEN_FULL,
                            exoPlayer!!.playWhenReady
                        )
                    }
                    VideoFullScreenActivity.videoResult = uriVideo
                    startActivityForResult(
                        VideoFullScreenActivity::class.java,
                        KEY_REQUEST_CODE_FULL_SCREEN,
                        bundle
                    )
                }
            }
        }


        exoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                val isPlay = playWhenReady && playbackState == Player.STATE_READY
                setListenerDurationChange(isPlay)
                loadingVideoResult.setVisibility(playbackState == Player.STATE_BUFFERING)
                repeatVideoResult.setVisibility(playbackState == Player.STATE_ENDED)
                playVideoResult.setVisibility(!playWhenReady)
                pauseVideoResult.setVisibility(playWhenReady && playbackState == Player.STATE_READY)
                playerView.setControllerVisibilityListener {
                    if (playWhenReady && playbackState == Player.STATE_READY) {
                        pauseVideoResult.visibility = it
                    }
                }
            }
        })
    }

    fun setListenerDurationChange(isListener: Boolean) {
        if (isListener) {
            if (durationDisposable == null) {
                Observable.timer(100, TimeUnit.MILLISECONDS)
                    .repeat()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe {
                        rxDurationCallback.onNext(playerView.player.currentPosition)
                    }.let { durationDisposable = it }
            }
        } else {
            durationDisposable?.dispose()
            durationDisposable = null
        }
    }

    private fun releasePlayer() {
        if (exoPlayer != null) {
            startPosition = exoPlayer!!.currentPosition
            speedParameter = exoPlayer!!.playbackParameters.speed
            autoStart = exoPlayer!!.playWhenReady
            exoPlayer!!.release()
            exoPlayer = null
        }
        if (isListenerDurationChange) setListenerDurationChange(false)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        releasePlayer()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            setUpExoPlayer(uriVideo)
            playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            setUpExoPlayer(uriVideo)
            playerView.onResume()
        }
    }


    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    private fun setupDataBluetoothFromMachine(data: List<DataBluetooth>, startTime2: Long) {
        with(humanVideoResult) {
            layoutHumanPosition.hide()
        }
        addDispose(rxDurationCallback.subscribe { duration ->
            for (i in data.indices) {
                if (((data[i].time!! - startTime2) / 100) == duration.toLong() / 100) {
                    BlePositionUtils.setCallbackBleDataForce(humanVideoResult, data[i])
                }
            }
        })
    }

    private fun playVideo() {
        autoStart = true
        exoPlayer?.playWhenReady = autoStart
    }

    private fun pauseVideo() {
        autoStart = false
        exoPlayer?.playWhenReady = autoStart
    }

    private fun replayVideo() {
        autoStart = true
        startPosition = 0
        exoPlayer?.playWhenReady = autoStart
        exoPlayer?.seekTo(startPosition)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        videoResult = null
    }

    override fun onDestroy() {
        durationDisposable?.dispose()
        super.onDestroy()
    }
}