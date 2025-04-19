package com.radioplayer

import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        initializeMediaSession()
    }

    override fun onDestroy() {
        releaseMediaSession()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private fun initializeMediaSession() {
        val player = ExoPlayer.Builder(this).build().apply {
            addAnalyticsListener(EventLogger())
            setWakeMode(WAKE_MODE_NETWORK)
        }
        mediaSession = MediaSession.Builder(this, player).build()
    }

    private fun releaseMediaSession() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
    }
}
