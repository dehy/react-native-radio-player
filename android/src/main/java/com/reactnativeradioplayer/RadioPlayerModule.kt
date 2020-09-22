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


class RadioPlayerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), Player.EventListener, MetadataOutput {

    private val context = reactContext
    private var player: SimpleExoPlayer = SimpleExoPlayer.Builder(reactContext).build()

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
        player.prepare()
        player.play()
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

  override fun onPlaybackStateChanged(state: Int) {
    var stateString = "unknown"
    var playbackStateString = "unknown"
    when (state) {
      Player.STATE_IDLE, Player.STATE_ENDED -> {
        stateString = "loadingFinished"
        playbackStateString = "stopped"
      }
      Player.STATE_BUFFERING -> {
        stateString = "loading"
        playbackStateString = "paused"
      }
      Player.STATE_READY -> {
        stateString = "loadingFinished"
        playbackStateString = "playing"
      }
    }
    val stateMap = WritableNativeMap()
    stateMap.putString("state", stateString)
    sendEvent(this.context, "StateDidChange", stateMap)

    val playbackStateMap = WritableNativeMap()
    playbackStateMap.putString("playbackState", playbackStateString)
    sendEvent(this.context, "PlaybackStateDidChange", playbackStateMap)
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
    var artistName = "Unknown"
    var trackName = "Unknown"
    for (i in 1..metadata.length()) {
      val entry: Metadata.Entry = metadata.get(i-1)
      if (entry is IcyInfo) {
        if (entry.title != null) {
          val parts: List<String> = entry.title!!.split(" - ")
          artistName = parts[0]
          trackName = parts[1]
        }
      }
    }
    val metadataMap = WritableNativeMap()
    metadataMap.putString("artistName", artistName)
    metadataMap.putString("trackName", trackName)
    sendEvent(this.context, "MetadataDidChange", metadataMap)
  }
}
