import * as React from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import RadioPlayer, {
  RadioPlayerEvents,
  RadioPlayerMetadata,
} from 'react-native-radio-player';

export default function App() {
  const [playerState, setPlayerState] = React.useState<string | undefined>();
  const [playerPlaybackState, setPlayerPlaybackState] = React.useState<string | undefined>();
  const [metadata, setMetadata] = React.useState<RadioPlayerMetadata>();

  React.useEffect(() => {
    RadioPlayerEvents.addListener('StateDidChange', setPlayerState);
    RadioPlayerEvents.addListener(
      'PlaybackStateDidChange',
      setPlayerPlaybackState
    );
    return () => {
      RadioPlayerEvents.removeListener('StateDidChange', setPlayerState);
      RadioPlayerEvents.removeListener(
        'PlaybackStateDidChange',
        setPlayerPlaybackState
      );
    };
  }, []);

  RadioPlayerEvents.addListener('MetadataDidChange', setMetadata);

  React.useEffect(() => {
    RadioPlayer.radioURL('https://stream.fr.morow.com/morow_med.aacp');
    return () => {
      RadioPlayer.stop();
    };
  }, []);

  let play = () => {
    RadioPlayer.play();
  };

  let stop = () => {
    RadioPlayer.stop();
  };

  return (
    <View style={styles.container}>
      <View style={styles.container}>
        <Text>Title</Text>
        <Text>{metadata?.trackName ?? 'Unknown'}</Text>
        <Text>Artist</Text>
        <Text>{metadata?.artistName ?? 'Unknown'}</Text>
      </View>
      <View style={[styles.container, styles.actions]}>
        <Button
          title="Play"
          onPress={play}
          disabled={playerPlaybackState === 'playing' ? true : false}
        />
        <Button
          title="Stop"
          onPress={stop}
          disabled={playerPlaybackState === 'playing' ? false : true}
        />
      </View>
      <View style={styles.container}>
        <Text>State: {playerState}</Text>
        <Text>PlaybackState: {playerPlaybackState}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  actions: {
    flex: 1,
    flexDirection: 'row',
  },
});
