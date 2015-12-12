package com.ludgo.android.spotifyplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * http://sapandiwakar.in/tutorial-how-to-manually-create-android-media-player-controls/
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener {

    private static final String ACTION_PLAY = "PLAY_SPOTIFY";
    private static SpotifyPlayerService mInstance = null;

    // To be provided for every single track
    private static String mUrl;

    private static MediaPlayer mMediaPlayer = null;
    private static int mBufferPercentage;

    private static boolean isCompleted;
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
        Log.d("!!!!!!!!", "onCreate");
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
            Log.d("!!!!!!!!", "ACTION_PLAY");
        }
        Log.d("!!!!!!!!", "onStartCommand");
        return START_NOT_STICKY; // onStartCommand will be called again when service is killed
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            Log.d("!!!!!!!!", "release..MediaPlayer");
        }
        mState = State.Retrieving;
        Log.d("!!!!!!!!", "onDestroy");
    }

    public static String getUrl() {
        Log.d("!!!!!!!!", "getUrl " + mUrl);
        return mUrl;
    }

    public static void setUrl(String url) {
        mUrl = url;
        Log.d("!!!!!!!!", "setUrl " + mUrl);
    }

    private void initMediaPlayer() {

        try {
            mMediaPlayer.setDataSource(mUrl);
            Log.d("!!!!!!!!", "setDataSource" + mUrl);
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
            Log.d("!!!!!!!!", "prepareAsync");
        } catch (IllegalStateException e) {
            // ...
            Log.d("!!!!!!!!", "444444444");
        }
        mState = State.Preparing;
    }

    public static SpotifyPlayerService getInstance() {
        return mInstance;
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
            startMusic();
        }
        Log.d("!!!!!!!!", "onPrepared");
    }

    /**
     * MediaPlayer.OnErrorListener
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        Log.d("!!!!!!!!", "onError");
        return false;
    }

    /**
     * MediaPlayer.OnBufferingUpdateListener
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferPercentage = percent * getDurationMusic() / 100;
        Log.d("!!!!!!!!", "onBufferingUpdate");
    }

    /**
     * MediaPlayer.OnCompletionListener
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        isCompleted = true;
        Log.d("!!!!!!!!", "onCompletion");
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public int getBufferPercentageMusic() {
        Log.d("!!!!!!!!", "getBufferPercentageMusic");
        return mBufferPercentage;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public int getCurrentPositionMusic() {
        Log.d("!!!!!!!!", "getCurrentPositionMusic");
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public int getDurationMusic() {
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            Log.d("!!!!!!!!", "getDurationMusic");
            return mMediaPlayer.getDuration(); // in milliseconds
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public boolean isPlayingMusic() {
        Log.d("!!!!!!!!", "isPlayingMusic");
        return mState.equals(State.Playing);
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            mMediaPlayer.pause();
            mState = State.Paused;
//            updateNotification(mSongTitle + "(paused)");
            Log.d("!!!!!!!!", "pauseMusic");
        }
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public void seekToMusic(int pos) {
        if (mState.equals(State.Playing) || mState.equals(State.Paused)) {
            mMediaPlayer.seekTo(pos);
            Log.d("!!!!!!!!", "seekToMusic");
        }
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link TrackDialogFragment}
     */
    public void startMusic() {
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            mMediaPlayer.start();
            mState = State.Playing;
//            updateNotification(mSongTitle + "(playing)");
            Log.d("!!!!!!!!", "startMusic -----------should play");
        }
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public boolean isReadyMusic() {
        return mState.equals(State.Playing) || mState.equals(State.Paused);
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public boolean isCompletedMusic() {
        return isCompleted;
    }

    /**
     * pair method {@link TrackDialogFragment}
     */
    public void restartMusic() {
        isCompleted = false;
        wasPlaying = isPlayingMusic();
        // Change the current track to another
        mMediaPlayer.reset();
        mState = State.Retrieving;
        initMediaPlayer();
        Log.d("!!!!!!!!", "restartMusic");
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
