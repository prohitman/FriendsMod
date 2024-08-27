package com.prohitman.friendsmod.vc;

import java.util.List;

public class AudioUtils {
    public static final short SIZE = 960;
    public static short[] combineAudio(List<short[]> audioParts) {
        short[] result = new short[SIZE];
        int sample;
        for (int i = 0; i < result.length; i++) {
            sample = 0;
            for (short[] audio : audioParts) {
                if (audio == null) {
                    sample += 0;
                } else {
                    sample += audio[i];
                }
            }
            if (sample > Short.MAX_VALUE) {
                result[i] = Short.MAX_VALUE;
            } else if (sample < Short.MIN_VALUE) {
                result[i] = Short.MIN_VALUE;
            } else {
                result[i] = (short) sample;
            }
        }
        return result;
    }

    public static short[] concatenateShortArrays(List<short[]> audioParts) {
        int totalLength = 0;
        for (short[] array : audioParts) {
            totalLength += array.length;
        }

        short[] audio = new short[totalLength];

        int currentIndex = 0;
        for (short[] audioPart : audioParts) {
            System.arraycopy(audioPart, 0, audio, currentIndex, audioPart.length);
            currentIndex += audioPart.length;
        }

        return audio;
    }
}
