import SwiftRadioPlayer

@objc(RadioPlayer)
class RadioPlayer: RCTEventEmitter, SwiftRadioPlayerDelegate {

    private var hasListeners = false
    private let player: SwiftRadioPlayer = SwiftRadioPlayer.shared
    private var radioURL: URL?
    private var playerState: SwiftRadioPlayerState = .urlNotSet
    private var playbackState: SwiftRadioPlaybackState = .stopped
    private var state: PlayerState = .stopped
    private var metadataSeparator: String = "-"

    private enum PlayerState: String {
        case error, stopped, playing, paused, buffering
    }

    private enum EventName: String {
        case stateDidChange = "StateDidChange"
        case metadataDidChange = "MetadataDidChange"
    }

    override init() {
        super.init()
        configurePlayer()
    }

    private func configurePlayer() {
        player.isAutoPlay = true
        player.enableArtwork = false
        player.delegate = self
    }

    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    override func supportedEvents() -> [String] {
        return [EventName.stateDidChange.rawValue, EventName.metadataDidChange.rawValue]
    }

    override func startObserving() {
        hasListeners = true
    }

    override func stopObserving() {
        hasListeners = false
    }

    @objc(radioURL:withResolver:withRejecter:)
    func setRadioURL(url: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        guard let validURL = URL(string: url) else {
            reject("INVALID_URL", "The provided URL is invalid", nil)
            return
        }
        radioURL = validURL
        resolve(nil)
    }

    @objc(radioURLWithMetadataSeparator:metadataSeparator:withResolver:withRejecter:)
    func setRadioURLWithMetadataSeparator(url: String, metadataSeparator: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        guard let validURL = URL(string: url) else {
            reject("INVALID_URL", "The provided URL is invalid", nil)
            return
        }
        self.metadataSeparator = metadataSeparator
        radioURL = validURL
        resolve(nil)
    }

    @objc(play:withRejecter:)
    func play(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        guard let radioURL = radioURL else {
            reject("URL_NOT_SET", "radioURL not set", nil)
            return
        }
        player.radioURL = radioURL
        resolve(nil)
    }

    @objc(isPlaying:withRejecter:)
    func isPlaying(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve(player.isPlaying)
    }

    @objc(stop:withRejecter:)
    func stop(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        player.stop()
        resolve(nil)
    }

    private func computeAndSendStateEvent() {
        let previousState = state

        switch playerState {
        case .error, .urlNotSet:
            state = .error
        case .loading:
            state = .buffering
        case .loadingFinished where playbackState == .playing:
            state = .playing
        case .readyToPlay where playbackState == .paused:
            state = .paused
        case .loadingFinished where playbackState == .stopped:
            state = .stopped
        default:
            break
        }

        guard state != previousState else {
            print("Same state as previously. Skipping sending event")
            return
        }

        if hasListeners {
            sendEvent(withName: EventName.stateDidChange.rawValue, body: ["state": state.rawValue])
        }
    }

    func radioPlayer(_ player: SwiftRadioPlayer, playerStateDidChange state: SwiftRadioPlayerState) {
        playerState = state
        computeAndSendStateEvent()
    }

    func radioPlayer(_ player: SwiftRadioPlayer, playbackStateDidChange state: SwiftRadioPlaybackState) {
        playbackState = state
        computeAndSendStateEvent()
    }

    func radioPlayer(_ player: SwiftRadioPlayer, metadataDidChange rawValue: String?) {
        guard hasListeners else { return }

        let parts = rawValue?.components(separatedBy: metadataSeparator)
        let artistName = parts?.first?.trimmingCharacters(in: .whitespacesAndNewlines)
        let trackName = parts?.dropFirst().joined(separator: metadataSeparator).trimmingCharacters(in: .whitespacesAndNewlines)

        sendEvent(withName: EventName.metadataDidChange.rawValue, body: ["artistName": artistName ?? "", "trackName": trackName ?? ""])
    }
}
