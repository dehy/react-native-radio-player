# react-native-radio-player
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fdehy%2Freact-native-radio-player.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fdehy%2Freact-native-radio-player?ref=badge_shield)


Play radio streams in react-native

## Installation

```sh
yarn install react-native-radio-player
```

## Usage

In android/main/AndroidManifest.xml :

 - Add the following permissions
```xml
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
```
 - Add the following queries intent
```xml
  <queries>
    <intent>
      <action android:name="androidx.media3.session.MediaSessionService"/>
    </intent>
  </queries>
```
 - Add the following service under the application tag
```xml
  <service android:name="com.radioplayer.PlaybackService" android:foregroundServiceType="mediaPlayback" android:exported="true">
    <intent-filter>
      <action android:name="androidx.media3.session.MediaSessionService"/>
    </intent-filter>
  </service>
```

```js
import RadioPlayer, {
  RadioPlayerEvents,
  RadioPlayerMetadata,
} from 'react-native-radio-player';

// ...

const metadataSeparator = "-"; // Used to split artist and title in stream metadata
RadioPlayer.radioURLWithMetadataSeparator('https://...', metadataSeparator);
// or RadioPlayer.radioURL('https://...')
RadioPlayer.stop();
RadioPlayer.play();

// State: error, stopped, playing, paused, buffering
RadioPlayerEvents.addEventListener('stateDidChange', (event) => {
  console.log(event.state);
});
// Metadata: {"artistName": "Example Artist", "trackName": "Example Title"}
RadioPlayerEvents.addListener('MetadataDidChange', (metadata) => {
  console.log(`Artist: ${metadata.artistName}`);
  console.log(`Title: ${metadata.trackName}`);
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fdehy%2Freact-native-radio-player.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fdehy%2Freact-native-radio-player?ref=badge_large)
Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
