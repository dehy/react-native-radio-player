package com.radioplayer

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture

class PlaybackCallback : MediaSession.Callback {
  @OptIn(UnstableApi::class)
  override fun onPlaybackResumption(
    mediaSession: MediaSession,
    controller: MediaSession.ControllerInfo
  ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
    val settable = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
    settable.set(MediaSession.MediaItemsWithStartPosition.EMPTY)
    return settable
  }
}
