package com.ludgo.android.spotifyplayer;

/**
 * An object model representing a single track available at Spotify web api
 * (a simplified Spotify web api wrapper Track object),
 * includes only details necessary for this application data flow.
 */
public class FoundTrack {

    public String name;
    public long duration; // in milliseconds
    public String previewUrl; // for streaming music
    public String artistName;
    public String albumName;
    public String albumThumbnail; // url format
    public String albumPoster; // url format

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
}
