import * as React from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import RadioPlayer, {
  RadioPlayerEvents,
  RadioPlayerMetadata,
} from 'react-native-radio-player';

export default function App() {
  const [playerState, setPlayerState] = React.useState<string>('stopped');
  const [metadata, setMetadata] = React.useState<RadioPlayerMetadata>();

  React.useEffect(() => {
    RadioPlayerEvents.addListener('StateDidChange', (eventObject) => {
      setPlayerState(eventObject.state);
    });
    return () => {
      RadioPlayerEvents.removeListener('StateDidChange', (eventObject) => {
        setPlayerState(eventObject.state);
      });
    };
  }, []);

  React.useEffect(() => {
    RadioPlayerEvents.addListener('MetadataDidChange', setMetadata);
    return () => {
      RadioPlayerEvents.addListener('MetadataDidChange', setMetadata);
    };
  }, []);

  React.useEffect(() => {
    RadioPlayer.radioURLWithMetadataSeparator('https://stream.fr.morow.com/morow_med.mp3', '-');
    // RadioPlayer.radioURL('https://stream.fr.morow.com/morow_med.mp3');
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
          disabled={playerState === 'stopped' ? false : true}
        />
        <Button
          title="Stop"
          onPress={stop}
          disabled={playerState === 'stopped' ? true : false}
        />
      </View>
      <View style={styles.container}>
        <Text>State: {playerState}</Text>
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
