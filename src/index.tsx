import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

type RadioPlayerType = {
  radioURL(url: string): Promise<void>;
  radioURLWithMetadataSeparator(
    url: string,
    metadataSeparator: string
  ): Promise<void>;
  play(): Promise<void>;
  stop(): Promise<void>;
};

export type RadioPlayerMetadata = {
  artistName: string;
  trackName: string;
};

const LINKING_ERROR =
  `The package 'react-native-radio-player' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const RadioPlayer = NativeModules.RadioPlayer
  ? NativeModules.RadioPlayer
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
const RadioPlayerEvents = new NativeEventEmitter(NativeModules.RadioPlayer);

export default RadioPlayer as RadioPlayerType;
export { RadioPlayerEvents };
