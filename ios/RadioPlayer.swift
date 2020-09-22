import FRadioPlayer

@objc(RadioPlayer)
class RadioPlayer: RCTEventEmitter, FRadioPlayerDelegate {

    var hasListeners: Bool = false;
    let player: FRadioPlayer
    var radioURL: URL?

    override init() {
        player = FRadioPlayer.shared
        super.init()
                
        player.isAutoPlay = true
        player.enableArtwork = false
        player.delegate = self
    }
    
    /// Base overide for RCTEventEmitter.
    ///
    /// - Returns: all supported events
    override func supportedEvents() -> [String] {
        return [
            "StateDidChange",
            "PlaybackStateDidChange",
            "MetadataDidChange"
        ]
    }
    
    // Will be called when this module's first listener is added.
    override func startObserving() {
        hasListeners = true;
        // Set up any upstream listeners or background tasks as necessary
    }

    // Will be called when this module's last listener is removed, or on dealloc.
    override func stopObserving() {
        hasListeners = false;
        // Remove upstream listeners, stop unnecessary background tasks
    }
    
    @objc(radioURL:withResolver:withRejecter:)
    func radioURL(url: String, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        radioURL = URL(string: url)
    }
    
    @objc(play:withRejecter:)
    func play(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        if (radioURL == nil) {
            print("radioURL not set")
            return
        }
        player.radioURL = radioURL
    }

    @objc(isPlaying:withRejecter:)
    func isPlaying(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Bool {
        return player.isPlaying
    }
    
    @objc(stop:withRejecter:)
    func stop(resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
        player.stop()
    }
    
    func radioPlayer(_ player: FRadioPlayer, playerStateDidChange state: FRadioPlayerState) {
        var stateString: String? = nil
        switch state {
        case .error:
            stateString = "error"
        case .loading:
            stateString = "loading"
        case .loadingFinished:
            stateString = "loadingFinished"
        case .readyToPlay:
            stateString = "readyToPlay"
        case .urlNotSet:
            stateString = "urlNotSet"
        }
        print("player \(player) player state did change to \(stateString ?? "Unknown")")
        if (hasListeners) {
            sendEvent(withName: "StateDidChange", body: ["state": stateString])
        }
    }
    
    func radioPlayer(_ player: FRadioPlayer, playbackStateDidChange state: FRadioPlaybackState) {
        var playbackStateString: String? = nil
        switch state {
        case .playing:
            playbackStateString = "playing"
        case .paused:
            playbackStateString = "paused"
        case .stopped:
            playbackStateString = "stopped"
        }
        print("player \(player) playback state did change to \(playbackStateString ?? "Unknown")")
        if (hasListeners) {
            sendEvent(withName: "PlaybackStateDidChange", body: ["playbackState": playbackStateString])
        }
    }
    
    func radioPlayer(_ player: FRadioPlayer, metadataDidChange artistName: String?, trackName: String?) {
        if (hasListeners) {
            sendEvent(withName: "MetadataDidChange", body: ["artistName": artistName, "trackName": trackName])
        }
    }
}
