package com.ludgo.android.spotifyplayer.util;

public class Utilities {

    /**
     * Convert a duration in milliseconds to its String representation
     * @return in format minutes:two-digit-seconds
     */
    public static String millisecondsToTime(int milliseconds){
        int seconds = milliseconds / 1000;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
