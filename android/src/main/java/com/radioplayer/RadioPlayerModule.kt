package com.radioplayer

import android.util.Log
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.util.EventLogger
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

@UnstableApi
class RadioPlayerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), Player.Listener,
  MetadataOutput {

  private val context = reactContext
  private var player: ExoPlayer = ExoPlayer.Builder(reactContext).build()
  private var playbackState: Int = Player.STATE_IDLE
  private var state: PlayerState? = null

  private var metadataSeparator: String = "-"

  override fun getName(): String {
    return "RadioPlayer"
  }

  init {
    UiThreadUtil.runOnUiThread {
      player.addAnalyticsListener(EventLogger())
      player.setWakeMode(WAKE_MODE_NETWORK)
      player.addListener(this)
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
      val item: MediaItem = MediaItem.fromUri(uri)
      player.setMediaItem(item)
      //play()
    }
  }

  @ReactMethod
  fun radioURLWithMetadataSeparator(uri: String, metadataSeparator: String) {
    UiThreadUtil.runOnUiThread {
      this.metadataSeparator = metadataSeparator
      val item: MediaItem = MediaItem.fromUri(uri)
      player.setMediaItem(item)
      //play()
    }
  }

  @ReactMethod
  fun play() {
    UiThreadUtil.runOnUiThread {
      if (player.isPlaying) {
        player.stop()
      }
      player.prepare()
      player.play()
    }
  }

  @ReactMethod
  fun stop() {
    UiThreadUtil.runOnUiThread { player.stop() }
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

  private fun sendEvent(reactContext: ReactContext,
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
