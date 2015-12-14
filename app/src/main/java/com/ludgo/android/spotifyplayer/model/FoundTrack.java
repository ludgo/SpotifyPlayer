package com.ludgo.android.spotifyplayer.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.ludgo.android.spotifyplayer.R;

/**
 * An object model representing a single track available at Spotify web api
 * (a simplified Spotify web api wrapper Track object),
 * includes only details necessary for this application data flow.
 */
public class FoundTrack implements Parcelable {

    public String name;
    public long duration; // in milliseconds
    public String previewUrl; // for streaming music
    public String artistName;
    public String albumName;
    public String albumThumbnail; // url format
    public String albumPoster; // url format

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeLong(duration);
        out.writeString(previewUrl);
        out.writeString(artistName);
        out.writeString(albumName);
        out.writeString(albumThumbnail);
        out.writeString(albumPoster);
    }

    public FoundTrack(Context context) {
        this.name = context.getResources().getString(R.string.model_track_name_default);
        this.artistName = context.getResources().getString(R.string.model_artist_name_default);
        this.albumName = context.getResources().getString(R.string.model_album_name_default);
    }

    private FoundTrack(Parcel in) {
        name = in.readString();
        duration = in.readLong();
        previewUrl = in.readString();
        artistName = in.readString();
        albumName = in.readString();
        albumThumbnail = in.readString();
        albumPoster = in.readString();
    }

    public static final Parcelable.Creator<FoundTrack> CREATOR
            = new Parcelable.Creator<FoundTrack>() {
        public FoundTrack createFromParcel(Parcel in) {
            return new FoundTrack(in);
        }

        public FoundTrack[] newArray(int size) {
            return new FoundTrack[size];
        }
    };
}
