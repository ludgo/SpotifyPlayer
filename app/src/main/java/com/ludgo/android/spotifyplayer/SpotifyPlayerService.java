package com.ludgo.android.spotifyplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * http://sapandiwakar.in/tutorial-how-to-manually-create-android-media-player-controls/
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {

    private static final String ACTION_PLAY = "PLAY_SPOTIFY";
    private static String mUrl;
    private static SpotifyPlayerService mInstance = null;
    private static ToggleButton mControlButton;

    private static MediaPlayer mMediaPlayer = null; // The Media Player
    private static int mBufferPosition;

//    NotificationManager mNotificationManager;
//    Notification mNotification = null;
//    final int NOTIFICATION_ID = 1;


    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused // playback paused (media player ready!)
    };

    static State mState = State.Retrieving;

    @Override
    public void onCreate() {
        mInstance = this;
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d("!!!!!!!!", "onCreate");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            mMediaPlayer = new MediaPlayer(); // initialize it here
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            initMediaPlayer();
            Log.d("!!!!!!!!", "onStartCommand");
        }
        return START_STICKY;
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
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            Log.d("!!!!!!!!", "prepareAsync");
        } catch (IllegalStateException e) {
            // ...
            Log.d("!!!!!!!!", "444444444");
        }
        mState = State.Preparing;
    }

    public void restartMusic() {
        // Restart music
        Log.d("!!!!!!!!", "restartMusic");
        mControlButton.setEnabled(false);
        mState = State.Retrieving;
        mMediaPlayer.reset();
        initMediaPlayer();
    }

    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        // Begin playing music
        Log.d("!!!!!!!!", "onPrepared");
        mState = State.Paused;
        mControlButton.setEnabled(true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        Log.d("!!!!!!!!", "onError");
        return false;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            Log.d("!!!!!!!!", "onDestroy");
        }
        mState = State.Retrieving;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            mMediaPlayer.pause();
            mState = State.Paused;
//            updateNotification(mSongTitle + "(paused)");
            Log.d("!!!!!!!!", "pauseMusic");
        }
    }

    public void startMusic() {
        Log.d("!!!!!!!!", "startMusic");
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            mMediaPlayer.start();
            mState = State.Playing;
//            updateNotification(mSongTitle + "(playing)");
            Log.d("!!!!!!!!", "startMusic -----------should play");
        }
    }

    public boolean isPlaying() {
        return mState.equals(State.Playing);
    }

    public int getMusicDuration() {
        // Return current music duration
        return 0;
    }

    public int getCurrentPosition() {
        // Return current position
        return 0;
    }

    public int getBufferPercentage() {
        return mBufferPosition;
    }

    public void seekMusicTo(int pos) {
        // Seek music to pos
    }

    public static SpotifyPlayerService getInstance() {
        return mInstance;
    }

    public static void setUrl(String url) {
        mUrl = url;
        Log.d("!!!!!!!!", "setUrl --" + mUrl + "--");
    }

    public static void setControlButton(ToggleButton controlButton) {
        mControlButton = controlButton;
        Log.d("!!!!!!!!", "setControlButton");
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getMusicDuration() / 100);
        Log.d("!!!!!!!!", "onBufferingUpdate");
    }

    /** Updates the notification. */
    void updateNotification(String text) {
        // Notify NotificationManager of new intent
    }

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