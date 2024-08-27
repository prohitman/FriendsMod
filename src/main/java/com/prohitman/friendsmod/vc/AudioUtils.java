package com.prohitman.friendsmod.vc;

import java.util.List;

public class AudioUtils {
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
