package com.ludgo.android.spotifyplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * http://sapandiwakar.in/tutorial-how-to-manually-create-android-media-player-controls/
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener {

    private static final String ACTION_PLAY = "PLAY_SPOTIFY";
    private static SpotifyPlayerService mInstance = null;

    private static MediaPlayer mMediaPlayer = null;
    private static int mBufferPercentage;

    private static ArrayList<FoundTrack> mTrackList;
    private static int mPosition;

    // Indicates the previous track was playing when interrupted
    private static boolean wasPlaying;

//    NotificationManager mNotificationManager;
//    Notification mNotification = null;
//    final int NOTIFICATION_ID = 1;

    // Indicates the state of the service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused // playback paused (media player ready!)
    }

    static State mState = State.Retrieving;

    @Override
    public void onCreate() {
        mInstance = this;
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // Don't allow binding
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // stream music from Spotify api
            initMediaPlayer();
        }
        return START_NOT_STICKY; // onStartCommand won be called again when service is killed
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mState = State.Retrieving;
    }

    public static ArrayList<FoundTrack> getTrackList() {
        return mTrackList;
    }

    public static void setTrackList(ArrayList<FoundTrack> list) {
        mTrackList = list;
    }

    public static int getPosition() {
        return mPosition;
    }

    public static void setPosition(int position) {
        mPosition = position;
    }

    private void initMediaPlayer() {

        try {
            mMediaPlayer.setDataSource(mTrackList.get(mPosition).previewUrl);
        } catch (IllegalArgumentException e) {
            // ...
            Log.d("!!!!!!!!", "1111111");
        } catch (IllegalStateException e) {
            // ...
            Log.d("!!!!!!!!", "22222222222");
        } catch (IOException e) {
            // ...
            Log.d("!!!!!!!!", "33333333");
        }

        try {
            mMediaPlayer.prepareAsync(); // prepare async not to block main thread
        } catch (IllegalStateException e) {
            // ...
            Log.d("!!!!!!!!", "444444444");
        }
        mState = State.Preparing;
    }

    public void restart() {
        // Change the current track to another
        wasPlaying = isPlaying();
        mMediaPlayer.reset();
        mState = State.Retrieving;
        initMediaPlayer();
    }

    public static SpotifyPlayerService getInstance() {
        return mInstance;
    }

    public static FoundTrack getCurrentTrack() {
        return mTrackList.get(mPosition);
    }

    /**
     * MediaPlayer.OnPreparedListener
     */
    @Override
    public void onPrepared(MediaPlayer player) {
        // MediaPlayer is ready
        mState = State.Paused;
        if (wasPlaying){
            wasPlaying = false;
            start();
        }
    }

    /**
     * MediaPlayer.OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        Log.d("!!!!!!!!", "5555555555");
        return false;
    }

    /**
     * MediaPlayer.OnBufferingUpdateListener
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferPercentage = percent * getDuration() / 100;
    }

    /**
     * MediaPlayer.OnCompletionListener
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public int getDuration() {
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            return mMediaPlayer.getDuration(); // in milliseconds
        }
        return 0;
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public boolean isPlaying() {
        return mState.equals(State.Playing);
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public void pause() {
        if (mState.equals(State.Playing)) {
            mMediaPlayer.pause();
            mState = State.Paused;
//            updateNotification(mSongTitle + "(paused)");
        }
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public void seekTo(int pos) {
        if (mState.equals(State.Playing) || mState.equals(State.Paused)) {
            mMediaPlayer.seekTo(pos);
        }
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public void start() {
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            mMediaPlayer.start();
            mState = State.Playing;
//            updateNotification(mSongTitle + "(playing)");
        }
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public boolean isReady() {
        return mState.equals(State.Playing) || mState.equals(State.Paused);
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public void previous(){
        mPosition--;
        if (mPosition < 0) {
            mPosition = mTrackList.size() - 1;
        }
        restart();
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public void next(){
        mPosition++;
        if (mPosition > mTrackList.size() - 1) {
            mPosition = 0;
        }
        restart();
    }





// notification
//    /** Updates the notification. */
//    void updateNotification(String text) {
//        // Notify NotificationManager of new intent
//    }

//    /**
//     * Configures service as a foreground service. A foreground service is a service that's doing something the user is
//     * actively aware of (such as playing music), and must appear to the user as a notification. That's why we create
//     * the notification here.
//     */
//    void setUpAsForeground(String text) {
//        PendingIntent pi =
//                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MusicActivity.class),
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//        mNotification = new Notification();
//        mNotification.tickerText = text;
//        mNotification.icon = R.drawable.ic_mshuffle_icon;
//        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
//        mNotification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), text, pi);
//        startForeground(NOTIFICATION_ID, mNotification);
//    }
}
