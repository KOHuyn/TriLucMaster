package com.mobileplus.dummytriluc.ui.main.practice.detail.info

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.databinding.LayoutPracticeDetailInfoTopBinding
import com.mobileplus.dummytriluc.ui.main.practice.detail.info.adapter.VideoPreviewAdapter
import com.mobileplus.dummytriluc.ui.utils.StartSnapHelper
import com.mobileplus.dummytriluc.ui.utils.ExoUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventEnableTopPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.video.fullscreen.VideoFullScreenActivity
import com.mobileplus.dummytriluc.ui.video.result.VideoResultActivity
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.ext.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject


class PracticeDetailInfoTopFragment : BaseFragmentZ<LayoutPracticeDetailInfoTopBinding>() {
    override fun getLayoutBinding(): LayoutPracticeDetailInfoTopBinding =
        LayoutPracticeDetailInfoTopBinding.inflate(layoutInflater)

    private val playerView: PlayerView by lazy { binding.videoViewInfoPractice }
    private val gson by inject<Gson>()

    //save state
    var startPosition: Long = 0
    var speedParameter: Float = 1f
    var autoStart: Boolean = false
    private var posItemMedia: Int = 0
        set(value) {
            field = value
            runOnUiThread {
                if (field > adapterVideo.itemCount - 1) field = adapterVideo.itemCount - 1
                val isDisableNext = field == adapterVideo.itemCount - 1
                binding.btnNextVideoPractice.isEnabled = !isDisableNext
                binding.btnNextVideoPractice.alpha = if (isDisableNext) 0.5f else 1f

                if (field < 0) field = 0
                val isDisablePrev = field == 0
                binding.btnPrevVideoPractice.isEnabled = !isDisablePrev
                binding.btnPrevVideoPractice.alpha = if (isDisablePrev) 0.5f else 1f
            }
        }
    private val adapterVideo by lazy { VideoPreviewAdapter() }
    private var exoPlayer: SimpleExoPlayer? = null
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private val exoCache = DummyTriLucApplication.exoCache
    private var uriVideo: Uri? = null
    private var dataResponse: DetailPracticeResponse? = null
    var initialized = false

    companion object {
        const val KEY_REQUEST_CODE_FULL_SCREEN = 12
    }


    fun initView() {
        if (!initialized) {
            dataResponse?.let { data ->
                binding.txtTitleInfoTop.text = data.title
                adapterVideo.items = data.media ?: mutableListOf()
                if (data.media != null && data.media.isNotEmpty()) {
                    playerView.player = getPlayer()
                    posItemMedia = 0
                    scrollToPosMedia(posItemMedia, false)
                } else {
                    binding.layoutVideoPreview.hide()
                }
                binding.btnNavigateVideoMain.setVisibility(data.videoPath != null && data.data != null && data.startTime2 != null)
                initialized = true
            }
        }
    }

    fun loadDataInfoTop(data: DetailPracticeResponse) {
        dataResponse = data
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        controlExoPlayer()
        handleClick()
        binding.rcvPreviewVideo.run {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterVideo
            setHasFixedSize(true)
        }
        adapterVideo.onItemClicked = OnClickItemAdapter { _, position ->
            if (position == posItemMedia) return@OnClickItemAdapter
            posItemMedia = position
            scrollToPosMedia(posItemMedia)
        }
        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcvPreviewVideo)
    }

    private fun handleClick() {
        binding.btnNavigateVideoMain.clickWithDebounce {
            dataResponse?.let { data ->
                if (data.videoPath != null && data.data != null && data.startTime2 != null) {
                    val bundle = Bundle().apply {
                        putString(
                            VideoResultActivity.PRACTICE_VIEW_DATA,
                            gson.toJson(dataResponse)
                        )
                    }
                    startActivity(VideoResultActivity::class.java, bundle)
                }
            }
        }
        binding.visibilityInfoTop.clickWithDebounce { postNormal(EventEnableTopPractice(false)) }
        binding.layoutHideInfoTop.clickWithDebounce { postNormal(EventEnableTopPractice(true)) }
        binding.btnPrevVideoPractice.clickWithDebounce {
            posItemMedia--
            if (posItemMedia >= 0) {
                scrollToPosMedia(posItemMedia)
            }
            logErr("prev:$posItemMedia")
        }

        binding.btnNextVideoPractice.clickWithDebounce {
            posItemMedia++
            if (posItemMedia <= adapterVideo.itemCount - 1) {
                scrollToPosMedia(posItemMedia)
            }
            logErr("next:$posItemMedia")
        }

        binding.repeatVideoInfo.clickWithDebounce {
            replayVideo()
        }
        binding.playVideoInfo.clickWithDebounce { playVideo() }
        binding.pauseVideoInfo.clickWithDebounce { pauseVideo() }
    }

    private fun scrollToPosMedia(pos: Int, autoStart: Boolean = true) {
        this.autoStart = autoStart
        startPosition = 0
        binding.rcvPreviewVideo.scrollToPosition(pos)
        adapterVideo.setSelectedVideo(pos)
        uriVideo = Uri.parse(adapterVideo.items[pos].mediaPath)
        prepareMedia(uriVideo)
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
    fun hideShowLayout(ev: EventEnableTopPractice) {
        if (!ev.isEnable) {
            stopVideo()
        }
        binding.layoutShowInfoTop.setVisibility(ev.isEnable)
        binding.layoutHideInfoTop.setVisibility(!ev.isEnable)
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
        val mediaSource1 =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4"))
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

    private val playerCallback: Player.EventListener? = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            binding.loadingVideoInfo?.setVisibility(playbackState == Player.STATE_BUFFERING && playWhenReady)
            binding.repeatVideoInfo?.setVisibility(playbackState == Player.STATE_ENDED)
            binding.playVideoInfo?.setVisibility(!playWhenReady)
            binding.pauseVideoInfo?.setVisibility(playWhenReady && playbackState == Player.STATE_READY)
            playerView.setControllerVisibilityListener {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    binding.pauseVideoInfo.visibility = it
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

    fun stopVideo() {
        if (exoPlayer?.playWhenReady == true) {
            startPosition = exoPlayer?.currentPosition ?: 0
            speedParameter = exoPlayer?.playbackParameters?.speed ?: 1f
            autoStart = exoPlayer?.playWhenReady ?: false
            exoPlayer?.playWhenReady = false
        }
    }

    fun restartVideo() {
        logErr("restart Video")
        if (exoPlayer == null) {
            prepareMedia(uriVideo)
        } else {
            exoPlayer?.seekTo(startPosition)
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

    fun releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer?.stop()
            exoPlayer!!.release()
            exoPlayer = null
        }
    }
}