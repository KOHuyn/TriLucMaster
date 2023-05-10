package com.mobileplus.dummytriluc.ui.utils

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner

object ExoUtils {
    private val context: Context = DummyTriLucApplication.getInstance().applicationContext
    private val mediaDataSource: DataSource.Factory by lazy {
        DefaultDataSourceFactory(
            context,
            Util.getUserAgent(
                context,
                DummyTriLucApplication.getInstance().applicationContext.applicationInfo.packageName
            )
        )
    }

    fun setupExoPlayer(playerView: PlayerView, exoPlayer: SimpleExoPlayer, uri: Uri) {
        val mediaSource =
            ProgressiveMediaSource.Factory(mediaDataSource).createMediaSource(uri)
        exoPlayer.prepare(mediaSource, false, false)
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = exoPlayer
        playerView.requestFocus()
    }

    val listSpeedParameter = mutableListOf(
        CustomSpinner.SpinnerItem("0.125x", 0.125f.toString()),
        CustomSpinner.SpinnerItem("0.25x", 0.25f.toString()),
        CustomSpinner.SpinnerItem("0.5x", 0.5f.toString()),
        CustomSpinner.SpinnerItem("1x", 1f.toString()),
    )
}