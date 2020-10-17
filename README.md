# react-native-radio-player

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

RadioPlayer.radioURL('https://...');
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
