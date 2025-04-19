package com.radioplayer

import android.content.ComponentName
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.common.util.concurrent.MoreExecutors

enum class PlayerState(val state: String) {
    ERROR("error"),
    STOPPED("stopped"),
    PLAYING("playing"),
    PAUSED("paused"),
    BUFFERING("buffering")
}

@UnstableApi
class RadioPlayerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext),
    Player.Listener,
    MetadataOutput {

    private val context = reactContext
    private var controller: MediaController? = null
    private var playbackState: Int = Player.STATE_IDLE
    private var state: PlayerState? = null
    private var metadataSeparator: String = "-"

    companion object {
        private const val TAG = "RadioPlayerModule"
        private const val EVENT_STATE_CHANGE = "StateDidChange"
        private const val EVENT_METADATA_CHANGE = "MetadataDidChange"
    }

    override fun getName(): String = "RadioPlayer"

    init {
        UiThreadUtil.runOnUiThread {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                controller = controllerFuture.get().apply {
                    addListener(this@RadioPlayerModule)
                }
            }, MoreExecutors.directExecutor())
        }
    }

    @ReactMethod
    fun addListener(eventName: String?) {
        // Required for RN built-in EventEmitter Calls
    }

    @ReactMethod
    fun removeListeners(count: Int?) {
        // Required for RN built-in EventEmitter Calls
    }

    @ReactMethod
    fun radioURL(uri: String) {
        UiThreadUtil.runOnUiThread {
            controller?.setMediaItem(MediaItem.fromUri(uri))
        }
    }

    @ReactMethod
    fun radioURLWithMetadataSeparator(uri: String, metadataSeparator: String) {
        UiThreadUtil.runOnUiThread {
            this.metadataSeparator = metadataSeparator
            controller?.setMediaItem(MediaItem.fromUri(uri))
        }
    }

    @ReactMethod
    fun play() {
        UiThreadUtil.runOnUiThread {
            controller?.let {
                if (it.isPlaying) it.stop()
                it.prepare()
                it.play()
            }
        }
    }

    @ReactMethod
    fun stop() {
        UiThreadUtil.runOnUiThread {
            controller?.stop()
        }
    }

    private fun computeAndSendStateEvent() {
        val previousState = state

        state = when (playbackState) {
            Player.STATE_IDLE, Player.STATE_ENDED -> PlayerState.STOPPED
            Player.STATE_BUFFERING -> PlayerState.BUFFERING
            Player.STATE_READY -> PlayerState.PLAYING
            else -> state
        }

        if (state == previousState) return

        val stateMap = WritableNativeMap().apply {
            putString("state", state?.state)
        }
        sendEvent(EVENT_STATE_CHANGE, stateMap)
    }

    override fun onPlaybackStateChanged(state: Int) {
        playbackState = state
        computeAndSendStateEvent()
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    override fun onMediaMetadataChanged(metadata: MediaMetadata) {
        Log.i(TAG, metadata.toString())
        val (artistName, trackName) = metadata.title?.split(metadataSeparator)?.let {
          it.getOrNull(0)?.trim() to it.getOrNull(1)?.trim()
        } ?: (null to null)

        val metadataMap = WritableNativeMap().apply {
            putString("artistName", artistName)
            putString("trackName", trackName)
        }
        sendEvent(EVENT_METADATA_CHANGE, metadataMap)
    }

    override fun onMetadata(metadata: Metadata) {
        Log.i(TAG, metadata.toString())
    }
}
