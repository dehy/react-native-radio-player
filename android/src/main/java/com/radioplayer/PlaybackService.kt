package com.radioplayer

import android.content.Intent
import android.util.Log
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private lateinit var player: Player
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        initializeMediaSession()
    }

    override fun onDestroy() {
        releaseMediaSession()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val player = mediaSession?.player
        if (player!!.playWhenReady) {
            player.pause()
        }
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private fun initializeMediaSession() {
        player = ExoPlayer.Builder(this).build().apply {
            addAnalyticsListener(EventLogger())
            setWakeMode(WAKE_MODE_NETWORK)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("PlayerError", "Playback failed", error)
                }
            })
        }
        mediaSession = MediaSession.Builder(this, player)
          .setCallback(PlaybackCallback())
          .build()
    }

    private fun releaseMediaSession() {
        mediaSession?.run {
            release()
            player.release()
            mediaSession = null
        }
    }
}
