package com.ludgo.android.spotifyplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ludgo.android.spotifyplayer.R;

public class Utilities {

    /**
     * Convert a duration in milliseconds to its String representation
     * @return in format minutes:two-digit-seconds
     */
    public static String millisecondsToTime(int milliseconds){
        int seconds = milliseconds / 1000;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    /**
     * A method using ISO 3166-1 alpha-2 country code standard
     * @return lower case country code saved in shared preferences
     */
    public static String getPreferredCountryCode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_country_key),
                context.getString(R.string.pref_country_entryValues_default));
    }
}
