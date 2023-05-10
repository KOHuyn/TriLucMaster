package com.mobileplus.dummytriluc.ui.video.fullscreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.core.BaseActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.ExoUtils
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_video_fullscreen.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import java.lang.Exception


/**
 * Created by KO Huyn on 12/21/2020.
 */
class VideoFullScreenActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_video_fullscreen

    private var exoPlayer: SimpleExoPlayer? = null
    private val playerView by lazy { fullScreenVideo }
    private var startPosition: Long = 0
    private var speedParameter: Float = 1f
    private var autoStart: Boolean = false
    private val mediaDataSource: DataSource.Factory by lazy {
        DefaultDataSourceFactory(
            this@VideoFullScreenActivity,
            Util.getUserAgent(
                this@VideoFullScreenActivity,
                DummyTriLucApplication.getInstance().applicationContext.applicationInfo.packageName
            )
        )
    }

    companion object {
        var videoResult: Uri? = null
        const val ARG_POSITION_SCREEN_FULL = "screen_full_arg_position"
        const val ARG_SPEED_SCREEN_FULL = "screen_full_arg_speed"
        const val ARG_AUTO_START_SCREEN_FULL = "screen_full_arg_auto_start"
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        try {
            startPosition = intent.getLongExtra(ARG_POSITION_SCREEN_FULL, 0L)
            speedParameter = intent.getFloatExtra(ARG_SPEED_SCREEN_FULL, 1f)
            autoStart = intent.getBooleanExtra(ARG_AUTO_START_SCREEN_FULL, false)
        } catch (e: Exception) {
            startPosition = 0
            e.logErr()
        }
        repeatVideoFullScreen.clickWithDebounce {
            autoStart = true
            startPosition = 0
            exoPlayer?.playWhenReady = autoStart
            exoPlayer?.seekTo(startPosition)
        }
        playVideoFullscreen.clickWithDebounce {
            autoStart = true
            exoPlayer?.playWhenReady = autoStart
        }
        pauseVideoFullscreen.clickWithDebounce {
            autoStart = false
            exoPlayer?.playWhenReady = autoStart
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            if (videoResult != null) {
                setUpExoPlayer(videoResult!!)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackPressed()
                }, 500)
            }
            playerView.onResume()
        }
    }

    private fun callBackPress() {
        val intent = Intent().apply {
            putExtra(ARG_POSITION_SCREEN_FULL, exoPlayer?.currentPosition)
            putExtra(ARG_AUTO_START_SCREEN_FULL, exoPlayer?.playWhenReady)
            putExtra(ARG_SPEED_SCREEN_FULL, exoPlayer?.playbackParameters?.speed)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            if (videoResult != null) {
                setUpExoPlayer(videoResult!!)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackPressed()
                }, 500)
            }
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

    private fun releasePlayer() {
        exoPlayer?.run {
            startPosition = currentPosition
            speedParameter = playbackParameters.speed
            autoStart = playWhenReady
            release()
        }
        exoPlayer = null
    }

    private fun setUpExoPlayer(uri: Uri?) {
        if (uri != null && exoPlayer == null) {
            val mediaSource =
                ProgressiveMediaSource.Factory(mediaDataSource).createMediaSource(uri)
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
            exoPlayer?.let {
                it.playWhenReady = autoStart
                it.playbackParameters = PlaybackParameters(speedParameter)
                it.seekTo(startPosition)
            }

            playerView.player = exoPlayer
            playerView.requestFocus()
            exoPlayer?.prepare(mediaSource, false, false)

            with(playerView) {
                btnFullScreen.setImageResource(R.drawable.ic_zoom_out)
                btnTimeSpeed.text = when (speedParameter) {
                    1f -> "1x"
                    0.5f -> "0.5x"
                    0.25f -> "0.25x"
                    0.125f -> "0.125x"
                    else -> ""
                }
                btnTimeSpeed.clickWithDebounce {
                    CustomSpinner(it, this@VideoFullScreenActivity)
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
                    callBackPress()
                }
            }
            exoPlayer?.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    loadingVideoFullScreen.setVisibility(playbackState == Player.STATE_BUFFERING)
                    repeatVideoFullScreen.setVisibility(playbackState == Player.STATE_ENDED)
                    playVideoFullscreen.setVisibility(!playWhenReady)
                    pauseVideoFullscreen.setVisibility(playWhenReady && playbackState == Player.STATE_READY)
                }
            })
            playerView.setControllerVisibilityListener {
                pauseVideoFullscreen.visibility = it
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            ViewPumpContextWrapper.wrap(LocalManageUtil.setLocal(newBase) ?: newBase)
        )
    }

    override fun onBackPressed() {
        callBackPress()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoResult = null
    }
}