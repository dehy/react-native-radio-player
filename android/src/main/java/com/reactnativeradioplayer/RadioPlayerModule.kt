package com.reactnativeradioplayer

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.google.android.exoplayer2.C.WAKE_MODE_NETWORK
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.EventLogger

enum class PlayerState(val state: String) {
  ERROR("error"),
  STOPPED("stopped"),
  PLAYING("playing"),
  PAUSED("paused"),
  BUFFERING("buffering"),
}

class RadioPlayerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), Player.EventListener, MetadataOutput {

  private val context = reactContext
  private var player: SimpleExoPlayer = SimpleExoPlayer.Builder(reactContext).build()
  private var playbackState: Int = Player.STATE_IDLE
  private var state: PlayerState? = null

  private var metadataSeparator: String = "-"

  override fun getName(): String {
    return "RadioPlayer"
  }

  init {
    UiThreadUtil.runOnUiThread {
      player.addAnalyticsListener(EventLogger(DefaultTrackSelector(this.context)))
      player.addMetadataOutput(this)
      player.setThrowsWhenUsingWrongThread(true)
      player.setWakeMode(WAKE_MODE_NETWORK)
      player.addListener(this)
    }
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
                        params: NativeMap) {
    reactContext
      .getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  override fun onMetadata(metadata: Metadata) {
    Log.i("RadioPlayerMetadata", metadata.toString())
    var artistName: String? = null
    var trackName: String? = null
    for (i in 1..metadata.length()) {
      val entry: Metadata.Entry = metadata.get(i - 1)
      if (entry is IcyInfo) {
        if (entry.title != null) {
          val parts: List<String> = entry.title!!.split(this.metadataSeparator)
          trackName = entry.title!!
          if (parts.count() >= 2) {
            artistName = parts[0].trim()
            trackName = parts[1].trim()
          }
        }
      }
    }
    val metadataMap = WritableNativeMap()
    metadataMap.putString("artistName", artistName)
    metadataMap.putString("trackName", trackName)
    sendEvent(this.context, "MetadataDidChange", metadataMap)
  }
}
