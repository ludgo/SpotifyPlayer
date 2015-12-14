package com.ludgo.android.spotifyplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * An object model representing a single artist listed at Spotify web api
 * (a simplified Spotify web api wrapper Artist object),
 * includes only details necessary for this application data flow.
 */
public class FoundArtist implements Parcelable {

    public String id;
    public String name;
    public String thumbnail; // url format

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(thumbnail);
    }

    public FoundArtist() {
        // Always also default images are to be provided
        this.thumbnail = "http://placehold.it/64x64";
    }

//    protected FoundArtist(String id,
//                         String name,
//                         String thumbnail) {
//        this.id = id;
//        this.name = name;
//        this.thumbnail = thumbnail;
//    }

    private FoundArtist(Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbnail = in.readString();
    }

    public static final Parcelable.Creator<FoundArtist> CREATOR
            = new Parcelable.Creator<FoundArtist>() {
        public FoundArtist createFromParcel(Parcel in) {
            return new FoundArtist(in);
        }

        public FoundArtist[] newArray(int size) {
            return new FoundArtist[size];
        }
    };
}
