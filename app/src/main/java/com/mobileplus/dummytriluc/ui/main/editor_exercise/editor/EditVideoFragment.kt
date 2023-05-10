package com.mobileplus.dummytriluc.ui.main.editor_exercise.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.model.ItemEditorExercise
import com.mobileplus.dummytriluc.data.request.SaveExerciseRequest
import com.mobileplus.dummytriluc.data.response.DetailDraftResponse
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.databinding.FragmentEditVideoBinding
import com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter.TimeLineAdapter
import com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter.VideoPreviewAdapter
import com.mobileplus.dummytriluc.ui.main.editor_exercise.add_more_video.AddMoreVideoFragment
import com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog.PositionPickerDialog
import com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog.PowerPickerDialog
import com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog.RemovePickerDialog
import com.mobileplus.dummytriluc.ui.main.editor_exercise.dialog.TimePickerDialog
import com.mobileplus.dummytriluc.ui.main.editor_exercise.step.EditVideoStepTwoFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.ExoUtils
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPushArrVideoPreview
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.video.fullscreen.VideoFullScreenActivity
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.mobileplus.dummytriluc.ui.widget.PowerPerSecond
import com.mobileplus.dummytriluc.ui.widget.SpeedyLinearLayoutManager
import com.utils.ext.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.item_video_details.*
import kotlinx.android.synthetic.main.layout_human_video.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Created by ThaiNV on 1/25/2021.
 * Editor by KOHuyn on 1/4/2021
 */
class EditVideoFragment : BaseFragmentZ<FragmentEditVideoBinding>() {
    override fun getLayoutBinding(): FragmentEditVideoBinding =
        FragmentEditVideoBinding.inflate(layoutInflater)

    private val vm by viewModel<EditVideoViewModel>()
    private val gson by inject<Gson>()

    private val idParentEditorExercise: Int by argument(ARG_ID_EDIT, AppConstants.INTEGER_DEFAULT)
    private val isUpdateExercise by argument(ARG_UPDATE_EXERCISE, false)
    private val editorRequest: SaveExerciseRequest by lazy { SaveExerciseRequest() }
    private var listDataVideoTimeLine = mutableListOf<SaveExerciseRequest.DataVideoTimeLine?>()

    //view bind
    private val rvTimeLine by lazy { rvTimeLineEditorVideo }

    companion object {
        const val ARG_ID_EDIT = "ARG_ID_DRAFT"
        const val ARG_UPDATE_EXERCISE = "ARG_UPDATE_EXERCISE"
        private const val KEY_REQUEST_CODE_FULL_SCREEN = 23

        fun openFragment(id: Int, isUpdateExercise: Boolean = false) {
            val bundle = Bundle().apply {
                putInt(ARG_ID_EDIT, id)
                putBoolean(ARG_UPDATE_EXERCISE, isUpdateExercise)
            }
            postNormal(EventNextFragmentMain(EditVideoFragment::class.java, bundle, true))
        }
    }

    private val adapterDraftExercise by lazy { VideoPreviewAdapter() }
    private val timeLineAdapter by lazy { TimeLineAdapter() }
    private val selectedVideos = arrayListOf<ItemEditorExercise>()

    //timer
    private var totalMiliSecondTimeVideoPower = 0
    private var isRunning: Boolean = false

    //initial exoplayer
    private val playerView: PlayerView by lazy { videoViewCoachFinish }
    private var exoPlayer: SimpleExoPlayer? = null
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private val exoCache = DummyTriLucApplication.exoCache
    private var uriVideo: Uri? = null

    //save state
    private var startPosition: Long = 0
    private var speedParameter: Float = 1f
    private var autoStart: Boolean = true

    override fun updateUI(savedInstanceState: Bundle?) {
        disposeViewModel()
        if (isUpdateExercise) {
            vm.getDetailPractice(idParentEditorExercise)
        } else {
            vm.getDetailDraft(idParentEditorExercise)
        }

        controlExoPlayer()
        playerView.player = getPlayer()
        generateView()
        handleClick()
    }

    private fun disposeViewModel() {
        addDispose(
            vm.rxMessage.subscribe { toast(it) },
            vm.rxDetailDraftResponse.subscribe { detailDraft ->
                uriVideo = Uri.parse(detailDraft.videoPath)
                prepareMedia(uriVideo)
                fillDataDetailDraft(detailDraft)
                selectedVideos.add(
                    ItemEditorExercise(
                        detailDraft.id,
                        detailDraft.userId,
                        detailDraft.title,
                        detailDraft.imagePath,
                        detailDraft.videoPath,
                        detailDraft.folderId,
                        detailDraft.videoPathOrigin
                    )
                )
                adapterDraftExercise.items = selectedVideos
            }
        )
        addDispose(vm.rxDetailExerciseResponse.subscribe { data ->
            uriVideo = Uri.parse(data.videoPath)
            prepareMedia(uriVideo)
            fillDataDetailExercise(data)
            selectedVideos.add(
                ItemEditorExercise(
                    data.id,
                    data.userId,
                    data.title,
                    data.imagePath,
                    data.videoPath,
                    0,
                    data.videoPathOrigin
                )
            )
        })
    }

    fun plus(vararg a: Long?): Long {
        var x = 0L
        for (i in a) {
            if (i != null) {
                x += i
            }
        }
        return x
    }

    fun Long?.minus(a: Long?): Long {
        return if (this != null && a != null) {
            abs(this - a)
        } else {
            when {
                this == null && a == null -> 0L
                this == null && a != null -> a
                this != null && a == null -> this
                else -> 0L
            }
        }
    }

    private fun fillDataDetailExercise(data: DetailPracticeResponse) {
        try {
            editorRequest.apply {
                startTime1 = data.startTime1
                startTime2 = data.startTime2
                videoPath = data.videoPathOrigin ?: data.videoPath
                endTime = data.endTime
                title = data.title
                content = data.content
                imagePath = data.imagePath
                note = data.note
                subjectId = data.subject?.id
                levelRequire = data.level?.id
                moreInformation.idParent = idParentEditorExercise
                moreInformation.subject = data.subject
                moreInformation.level = data.level
                moreInformation.levelPractice = data.levelPractice
                status = data.status
            }
            adapterDraftExercise.items = data.media?.map {
                ItemEditorExercise(
                    imagePath = it.mediaThumb,
                    videoPath = it.mediaPath,
                    videoPathOrigin = it.mediaPathOrigin
                )
            }?.toMutableList() ?: mutableListOf()
            val totalTimeLine =
                if (data.endTime != 0L && data.startTime1 != 0L)
                    (data.endTime.minus(data.startTime1)).toInt() * 10
                else 0
            if (totalTimeLine > 0) {
                val startTime2 = data.startTime2
                val sortedList =
                    gson.fromJsonSafe<List<DataBluetooth>>(data.data ?: "")
                        ?.sortedBy { it.time } ?: emptyList()
                listDataVideoTimeLine.clear()

                for (i in 0..totalTimeLine) {
                    listDataVideoTimeLine.add(i, null)
                }

                for (item in sortedList) {
                    val timeFloat = item.time?.toFloat() ?: 0F
                    val startTimeFloat = startTime2?.toFloat() ?: 0F
                    val miniSecond: Float =
                        round((timeFloat - startTimeFloat) / 100) / 10

                    listDataVideoTimeLine[(miniSecond * 10).toInt()] =
                        SaveExerciseRequest.DataVideoTimeLine(
                            item.force,
                            item.onTarget,
                            item.position,
                            timeFloat
                        )
                }

                if (listDataVideoTimeLine.isNotEmpty()) {
                    timeLineAdapter.items =
                        listDataVideoTimeLine
                }
                updateTimeLine(totalTimeLine)
            }

            uriVideo = Uri.parse(data.videoPath)
            prepareMedia(uriVideo)
            videoViewCoachFinish.show()
        } catch (e: Exception) {
            e.logErr()
            toast(loadStringRes(R.string.error_unknown_error))
            onBackPressed()
        }
    }

    private fun fillDataDetailDraft(detailDraft: DetailDraftResponse) {
        try {
            editorRequest.apply {
                startTime1 = detailDraft.startTime1
                startTime2 = detailDraft.startTime2
                videoPath = detailDraft.videoPathOrigin ?: detailDraft.videoPath
                endTime = detailDraft.endTime
            }
            val totalTimeLine =
                if (detailDraft.endTime != 0L && detailDraft.startTime1 != 0L)
                    (detailDraft.endTime - detailDraft.startTime1).toInt() * 10
                else 0
            if (totalTimeLine > 0) {
                val startTime2 = detailDraft.startTime2
                val sortedList =
                    gson.toList<DataBluetooth>(detailDraft.data).sortedBy { it.time }
                listDataVideoTimeLine.clear()

                for (i in 0..totalTimeLine) {
                    listDataVideoTimeLine.add(i, null)
                }

                for (item in sortedList) {
                    val timeFloat = item.time?.toFloat()
                    val startTimeFloat = startTime2.toFloat()
                    val miniSecond: Float =
                        round((timeFloat!! - startTimeFloat) / 100) / 10

                    listDataVideoTimeLine[(miniSecond * 10).toInt()] =
                        SaveExerciseRequest.DataVideoTimeLine(
                            item.force,
                            item.onTarget,
                            item.position,
                            timeFloat
                        )
                }

                if (listDataVideoTimeLine.isNotEmpty()) {
                    timeLineAdapter.items =
                        listDataVideoTimeLine
                }
                updateTimeLine(totalTimeLine)
            }

            uriVideo = Uri.parse(detailDraft.videoPath)
            prepareMedia(uriVideo)
            videoViewCoachFinish.show()
        } catch (e: Exception) {
            e.logErr()
            toast(loadStringRes(R.string.error_unknown_error))
            onBackPressed()
        }
    }

    private fun updateTimeLine(totalMsTimeLine: Int) {
        totalMiliSecondTimeVideoPower = totalMsTimeLine
        val second = totalMsTimeLine / 10
        val m = second / 60
        val s = second % 60
        timeHourVideo.text = m.setTime()
        timeMinuteVideo.text = s.setTime()
    }

    private fun Int.setTime(): String = if (this < 10) "0$this" else this.toString()

    private fun handleClick() {
        clickBackPress()
        clickVideoThumb()
        clickShowTimeDuration()
        clickZoomIn()
        clickZoomOut()
        clickPlayTimeLine()
        clickClearTimeLine()
        clickAddVideo()
        clickDeleteVideo()
        clickNextStep2()
        clickTimeLine()
        clickVideo()
    }

    private fun clickBackPress() {
        binding.btnBackToHomeEditVideo.clickWithDebounce {
            onBackPressed()
        }
    }

    private fun clickVideoThumb() {
        adapterDraftExercise.onClickItem = VideoPreviewAdapter.OnClickVideoListener {
            it?.let {
                uriVideo = Uri.parse(it)
                prepareMedia(uriVideo)
            } ?: toast(getString(R.string.error_can_not_play_video))
        }

        adapterDraftExercise.updateDataListener = object : VideoPreviewAdapter.UpdateDataListener {
            override fun onUpdateData(updatedData: MutableList<ItemEditorExercise>) {
                selectedVideos.clear()
                selectedVideos.addAll(updatedData)
            }
        }
    }

    private fun clickShowTimeDuration() {
        btnShowTimer.clickWithDebounce {
            pauseTimeline()
            TimePickerDialog()
                .setTimeDefault(totalMiliSecondTimeVideoPower)
                .build(parentFragmentManager)
                .setCallbackTimerListener { minute, second ->
                    updateTotalVideoTime(minute, second)
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

    private fun clickPlayTimeLine() {
        btnPlayTimeLine.clickWithDebounce {
            if (totalMiliSecondTimeVideoPower > 0) {
                if (isRunning) {
                    pauseTimeline()
                } else {
                    startTimeLine()
                    autoScroll()
                }
            } else {
                toast(getString(R.string.valid_time_video))
            }
        }
    }

    private fun clickClearTimeLine() {
        btnClearTimeLine.clickWithDebounce {
            pauseTimeline()
            rvTimeLine.scrollToPosition(0)
            if (listDataVideoTimeLine.isNotEmpty()) {
                for (i in 0 until listDataVideoTimeLine.size) {
                    listDataVideoTimeLine[i] = null
                }
                timeLineAdapter.items = listDataVideoTimeLine
            }
        }
    }

    private fun clickAddVideo() {
        btnAddVideoDraft.clickWithDebounce {
            AddMoreVideoFragment.openFragment(selectedVideos, 0, gson)
            stopVideo()
        }
    }

    private fun clickDeleteVideo() {
        btnDeleteVideoDraft.clickWithDebounce {
            adapterDraftExercise.removeSelectedItem()
        }
    }

    private fun clickNextStep2() {
        binding.btnNextToStepTwo.clickWithDebounce {
            editorRequest.moreInformation.totalTimeDuration = totalMiliSecondTimeVideoPower
            editorRequest.moreInformation.listDataVideoTimeline = listDataVideoTimeLine
            editorRequest.media =
                adapterDraftExercise.items.map { SaveExerciseRequest.Media(it.videoPathOrigin) }
            editorRequest.data = gson.toJson(listDataVideoTimeLine.filterNotNull()
                .map { item ->
                    DataBluetooth(
                        item.force,
                        item.onTarget,
                        item.position,
                        item.time?.toLong()
                    )
                })
            logErr("request editor: ${gson.toJson(editorRequest)}")
            if (listDataVideoTimeLine.filterNotNull().isEmpty()) {
                toast(getString(R.string.require_next_edit_video))
            } else {
                EditVideoStepTwoFragment.openFragment(editorRequest, gson, isUpdateExercise)
                stopVideo()
            }
        }
    }

    private fun clickVideo() {
        repeatVideoInfo.clickWithDebounce {
            replayVideo()
        }
        playVideoInfo.clickWithDebounce { playVideo() }
        pauseVideoInfo.clickWithDebounce { pauseVideo() }
    }

    private fun clickTimeLine() {
        timeLineAdapter.listener = object : PowerPerSecond.PowerPerSecondListener {
            override fun onPowerClick(position: Int) {
                val oldPower = timeLineAdapter.items[position]?.force?.roundToInt() ?: 0
                PowerPickerDialog(oldPower)
                    .build(parentFragmentManager)
                    .onFillPowerCallback { power, dialog ->
                        timeLineAdapter.items[position]?.force = power.toFloat()
                        timeLineAdapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
            }

            override fun onPositionClick(position: Int) {
                PositionPickerDialog()
                    .build(parentFragmentManager)
                    .setPositionCallbackListener { pos, dialog ->
                        timeLineAdapter.items[position]?.position = pos
                        timeLineAdapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
            }

            override fun onDeleteRequest(position: Int) {
                RemovePickerDialog()
                    .build(parentFragmentManager)
                    .setOnRemoveClick {
                        timeLineAdapter.items[position] = null
                        timeLineAdapter.notifyItemChanged(position)
                    }
            }

            override fun onAddRequest(position: Int) {
                fun openPowerDialog(posBle: String?) {
                    Handler(Looper.getMainLooper()).post {
                        PowerPickerDialog()
                            .build(parentFragmentManager)
                            .onFillPowerCallback { power, dialog ->
                                timeLineAdapter.items[position].run {
                                    timeLineAdapter.items[position] =
                                        SaveExerciseRequest.DataVideoTimeLine(
                                            position = posBle,
                                            force = power.toFloat(),
                                            time = (position.toFloat() * 100) + (editorRequest.startTime2?.toFloat()
                                                ?: 0F)
                                        )
                                }
                                timeLineAdapter.notifyItemChanged(position)
                                dialog.dismiss()
                            }
                    }
                }

                PositionPickerDialog()
                    .build(parentFragmentManager)
                    .setPositionCallbackListener { pos, dialog ->
                        dialog.dismiss()
                        openPowerDialog(pos)
                    }
            }
        }
    }

    private fun generateView() {
        gridActivePower.show()
        setupRcv()
    }

    private fun setupRcv() {
        binding.rcvDraftExercise.run {
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    true
                )
            )
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterDraftExercise
        }

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

    fun updateHitPosition(currPos: Int) {
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

    private fun updateTotalVideoTime(minute: Int, second: Int) {
        totalMiliSecondTimeVideoPower = (minute * 60 + second) * 10
        if (totalMiliSecondTimeVideoPower > 0) {
            updateTimeLine(totalMiliSecondTimeVideoPower)
            val wrapper = mutableListOf<SaveExerciseRequest.DataVideoTimeLine?>()
            for (i in 0 until totalMiliSecondTimeVideoPower + 1) {
                if (i < timeLineAdapter.itemCount) {
                    wrapper.add(timeLineAdapter.items[i])
                } else {
                    wrapper.add(null)
                }
            }
            listDataVideoTimeLine = wrapper
            timeLineAdapter.items = listDataVideoTimeLine
        } else {
            toast(getString(R.string.valid_time_video))
        }
    }

    private fun getPlayer(): SimpleExoPlayer? {
        if (exoPlayer == null) {
            preparePlayer()
        }
        return exoPlayer
    }

    private fun preparePlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
        cacheDataSourceFactory = CacheDataSourceFactory(
            exoCache,
            DefaultHttpDataSourceFactory(
                Util.getUserAgent(
                    requireContext(),
                    DummyTriLucApplication.getInstance().applicationContext.applicationInfo.packageName
                )
            )
        )
    }

    private fun prepareMedia(uri: Uri?) {
        val mediaSource2 =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri)

        val mediaSourcee = ConcatenatingMediaSource(true, mediaSource2)
        exoPlayer?.prepare(mediaSourcee, true, true)
        exoPlayer?.playWhenReady = autoStart
        exoPlayer?.playbackParameters = PlaybackParameters(speedParameter)
        exoPlayer?.seekTo(startPosition)
        exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer?.addListener(playerCallback)
    }

    private val playerCallback: Player.EventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            loadingVideoInfo?.setVisibility(playbackState == Player.STATE_BUFFERING && playWhenReady)
            repeatVideoInfo?.setVisibility(playbackState == Player.STATE_ENDED)
            playVideoInfo?.setVisibility(!playWhenReady)
            pauseVideoInfo?.setVisibility(playWhenReady && playbackState == Player.STATE_READY)
            playerView.setControllerVisibilityListener {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    pauseVideoInfo.visibility = it
                }
            }
        }

        override fun onPlayerError(error: com.google.android.exoplayer2.ExoPlaybackException?) {
            when (error?.type) {
                com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE -> {
                    toast(getString(R.string.error_exoplayer_type_source))
                }
                com.google.android.exoplayer2.ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    toast(getString(R.string.error_exoplayer_type_out_of_memory))
                }
                com.google.android.exoplayer2.ExoPlaybackException.TYPE_REMOTE -> {
                    toast(getString(R.string.error_exoplayer_type_remote))
                }
                com.google.android.exoplayer2.ExoPlaybackException.TYPE_RENDERER -> {
                    toast(getString(R.string.error_exoplayer_type_renderer))
                }
                com.google.android.exoplayer2.ExoPlaybackException.TYPE_UNEXPECTED -> {
                    toast(getString(R.string.error_exoplayer_type_unexpected))
                }
            }
        }
    }

    private fun controlExoPlayer() {
        with(playerView) {
            btnTimeSpeed?.text = when (speedParameter) {
                1f -> "1x"
                0.5f -> "0.5x"
                0.25f -> "0.25x"
                0.125f -> "0.125x"
                else -> ""
            }
            btnTimeSpeed?.clickWithDebounce {
                CustomSpinner(it, requireContext())
                    .setBackGroundSpinner(Color.parseColor("#2C2C2C"))
                    .setShowUp(true)
                    .setTextSize(resources.getDimension(R.dimen.text_10))
                    .setWidthWindow(resources.getDimension(R.dimen._50sdp))
                    .setDataSource(ExoUtils.listSpeedParameter).build()
                    .setOnSelectedItemCallback { item ->
                        exoPlayer?.playbackParameters = PlaybackParameters(item.id.toFloat())
                    }
            }
            btnFullScreen?.clickWithDebounce {
                val intent =
                    Intent(requireContext(), VideoFullScreenActivity::class.java).apply {
                        putExtra(
                            VideoFullScreenActivity.ARG_POSITION_SCREEN_FULL,
                            exoPlayer!!.currentPosition
                        )
                        putExtra(
                            VideoFullScreenActivity.ARG_SPEED_SCREEN_FULL,
                            exoPlayer!!.playbackParameters.speed
                        )
                        putExtra(
                            VideoFullScreenActivity.ARG_AUTO_START_SCREEN_FULL,
                            exoPlayer!!.playWhenReady
                        )
                    }
                VideoFullScreenActivity.videoResult = uriVideo
                startActivityForResult(
                    intent,
                    KEY_REQUEST_CODE_FULL_SCREEN
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY_REQUEST_CODE_FULL_SCREEN) {
            if (resultCode == Activity.RESULT_OK) {
                startPosition =
                    data?.getLongExtra(VideoFullScreenActivity.ARG_POSITION_SCREEN_FULL, 0L)
                        ?: 0L
                autoStart =
                    data?.getBooleanExtra(
                        VideoFullScreenActivity.ARG_AUTO_START_SCREEN_FULL,
                        false
                    )
                        ?: false
                speedParameter =
                    data?.getFloatExtra(VideoFullScreenActivity.ARG_SPEED_SCREEN_FULL, 1f) ?: 1f
            }
        }
    }

    private fun releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer?.stop()
            exoPlayer!!.release()
            exoPlayer = null
        }
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

    private fun stopVideo() {
        if (exoPlayer?.playWhenReady == true) {
            startPosition = exoPlayer?.currentPosition ?: 0
            speedParameter = exoPlayer?.playbackParameters?.speed ?: 1f
            autoStart = exoPlayer?.playWhenReady ?: false
            exoPlayer?.playWhenReady = false
        }
    }

    private fun restartVideo() {
        logErr("restart Video")
        if (exoPlayer == null) {
            prepareMedia(uriVideo)
        } else {
            exoPlayer?.seekTo(startPosition)
        }
    }

    @Subscribe
    fun onSelectedDraft(arrVideoPreview: EventPushArrVideoPreview) {
        val selectedDrafts = arrVideoPreview.arr
        selectedVideos.clear()
        selectedVideos.addAll(selectedDrafts)
        adapterDraftExercise.items = selectedDrafts.toMutableList()
        adapterDraftExercise.items[0].videoPath?.let {
            startPosition = 0L
            autoStart = false
            uriVideo = Uri.parse(it)
            prepareMedia(uriVideo)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        register(this)
    }

    override fun onDetach() {
        super.onDetach()
        unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            restartVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            restartVideo()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            stopVideo()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            stopVideo()
        }
    }
}