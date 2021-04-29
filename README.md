# react-native-radio-player
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fdehy%2Freact-native-radio-player.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fdehy%2Freact-native-radio-player?ref=badge_shield)


Play radio streams in react-native

## Installation

```sh
yarn install react-native-radio-player
```

## Usage

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