package com.radioplayer

import android.util.Log
import android.content.ComponentName
//import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.MoreExecutors
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import androidx.media3.session.MediaController
import com.facebook.react.bridge.NativeMap
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule


enum class PlayerState(val state: String) {
  ERROR("error"),
  STOPPED("stopped"),
  PLAYING("playing"),
  PAUSED("paused"),
  BUFFERING("buffering"),
}


class PlaybackService : MediaSessionService() {
  private var mediaSession: MediaSession? = null

  // Create your Player and MediaSession in the onCreate lifecycle event
  override fun onCreate() {
    super.onCreate()
    val player = ExoPlayer.Builder(this).build()
    player.addAnalyticsListener(EventLogger())
    player.setWakeMode(WAKE_MODE_NETWORK)
    mediaSession = MediaSession.Builder(this, player).build()
  }

  // Remember to release the player and media session in onDestroy
  override fun onDestroy() {
    mediaSession?.run {
      player.release()
      release()
      mediaSession = null
    }
    super.onDestroy()
  }

  // This example always accepts the connection request
  override fun onGetSession(
    controllerInfo: MediaSession.ControllerInfo
  ): MediaSession? = mediaSession
}


@UnstableApi
class RadioPlayerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
  Player.Listener,
  MetadataOutput {

  private val context = reactContext
  private var controller: MediaController? = null
  private var playbackState: Int = Player.STATE_IDLE
  private var state: PlayerState? = null

  private var metadataSeparator: String = "-"

  override fun getName(): String {
    return "RadioPlayer"
  }

  init {
    UiThreadUtil.runOnUiThread {
      val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
      val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
      controllerFuture.addListener(
        {
          // Call controllerFuture.get() to retrieve the MediaController.
          // MediaController implements the Player interface, so it can be
          // attached to the PlayerView UI component.
          // playerView.setPlayer(controllerFuture.get())
          val player= controllerFuture.get()
          player.addListener(this)
          controller = player
        },
        MoreExecutors.directExecutor()
      )

      // player.addAnalyticsListener(EventLogger())
      // player.setWakeMode(WAKE_MODE_NETWORK)
      // player.addListener(this)
    }
  }

  // Required for rn built in EventEmitter Calls.
  @ReactMethod
  fun addListener(eventName: String?) {
  }

  @ReactMethod
  fun removeListeners(count: Int?) {
  }

  @ReactMethod
  fun radioURL(uri: String) {
    UiThreadUtil.runOnUiThread {
      val player = controller
      if (player != null) {
        val item: MediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(item)
        //play()
      }
    }
  }

  @ReactMethod
  fun radioURLWithMetadataSeparator(uri: String, metadataSeparator: String) {
    UiThreadUtil.runOnUiThread {
      val player = controller
      if (player != null) {
        this.metadataSeparator = metadataSeparator
        val item: MediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(item)
        //play()
      }
    }
  }

  @ReactMethod
  fun play() {
    UiThreadUtil.runOnUiThread {
      val player = controller
      if (player != null) {
        if (player.isPlaying) {
          player.stop()
        }
        player.prepare()
        player.play()
      }
    }
  }

  @ReactMethod
  fun stop() {
    UiThreadUtil.runOnUiThread {
      val player = controller
      if (player != null)
        player.stop()
        player?.release()
    }
  }

  private fun computeAndSendStateEvent() {
    val previousState = this.state

    when (this.playbackState) {
      Player.STATE_IDLE, Player.STATE_ENDED -> {
        this.state = PlayerState.STOPPED
      }

      Player.STATE_BUFFERING -> {
        this.state = PlayerState.BUFFERING
      }

      Player.STATE_READY -> {
        this.state = PlayerState.PLAYING
      }
    }

    if (this.state === null || this.state === previousState) {
      return
    }

    val stateMap = WritableNativeMap()
    stateMap.putString("state", this.state!!.state)
    sendEvent(this.context, "StateDidChange", stateMap)
  }

  override fun onPlaybackStateChanged(state: Int) {
    this.playbackState = state
    computeAndSendStateEvent()
  }

  private fun sendEvent(
    reactContext: ReactContext,
    eventName: String,
    params: NativeMap
  ) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  override fun onMediaMetadataChanged(metadata: MediaMetadata) {
    Log.i("RadioPlayerMediaMetadata", metadata.toString())
    var artistName: String? = null
    var trackName: String? = null
    if (metadata.title != null) {
      val parts: List<String> = metadata.title!!.split(this.metadataSeparator)
      trackName = metadata.title!!.toString()
      if (parts.count() >= 2) {
        artistName = parts[0].trim()
        trackName = parts[1].trim()
      }
    }
    val metadataMap = WritableNativeMap()
    metadataMap.putString("artistName", artistName)
    metadataMap.putString("trackName", trackName)
    sendEvent(this.context, "MetadataDidChange", metadataMap)
  }

  override fun onMetadata(metadata: Metadata) {
    Log.i("RadioPlayerMetadata", metadata.toString())
  }
}
