package com.ludgo.android.spotifyplayer;

import android.os.Parcel;
import android.os.Parcelable;

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

    public FoundTrack() {
        // Always also default images are to be provided
        this.albumThumbnail = "http://placehold.it/64x64";
        this.albumPoster = "http://placehold.it/200x200";
    }

//    protected FoundTrack(String name,
//                         long duration,
//                         String previewUrl,
//                         String artistName,
//                         String albumName,
//                         String albumThumbnail,
//                         String albumPoster) {
//        this.name = name;
//        this.duration = duration;
//        this.previewUrl = previewUrl;
//        this.artistName = artistName;
//        this.albumName = albumName;
//        this.albumThumbnail = albumThumbnail;
//        this.albumPoster = albumPoster;
//    }

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
