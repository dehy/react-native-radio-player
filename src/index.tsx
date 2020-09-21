import { NativeModules } from 'react-native';

type RadioPlayerType = {
  multiply(a: number, b: number): Promise<number>;
};

const { RadioPlayer } = NativeModules;

export default RadioPlayer as RadioPlayerType;
