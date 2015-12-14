package com.ludgo.android.spotifyplayer.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ludgo.android.spotifyplayer.R;
import com.ludgo.android.spotifyplayer.ui.TrackDialogFragment;
import com.ludgo.android.spotifyplayer.ui.TrackListFragment;
import com.ludgo.android.spotifyplayer.model.FoundTrack;
import com.ludgo.android.spotifyplayer.util.BitmapFromUrlTask;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A service to stream and play music from Spotify web api.
 * It is foreground when the audio is heard, background otherwise.
 * Provided information accessed thanks to Spotify web api wrapper comes from {@link TrackListFragment}.
 * Can be controlled by user from UI in {@link TrackDialogFragment}.
 *
 * http://sapandiwakar.in/tutorial-how-to-manually-create-android-media-player-controls/
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener {

    public static final String FOREGROUND_NOTIFICATION_TAG = "fn_tag";
    private static final int NOTIFICATION_ID = 1; // not null integer
    private static Activity mNotificationActivity;
    private static Bitmap mNotificationLargeIcon;

    private static final String ACTION_PLAY = "PLAY_SPOTIFY";
    private static SpotifyPlayerService mInstance = null;

    private static MediaPlayer mMediaPlayer = null;
    private static int mBufferPercentage;

    private static ArrayList<FoundTrack> mTrackList;
    private static int mPosition;

    // Indicates the previous track was playing when interrupted
    private static boolean wasPlaying;

    // Indicates the state of the service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
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
        return START_NOT_STICKY; // onStartCommand won't be called again when service is killed
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

    /**
     * Prepare media player with new url
     */
    private void initMediaPlayer() {

        if (getCurrentTrack().albumThumbnail != null){
            // Prepare notification's large icon in the background
            BitmapFromUrlTask task = new BitmapFromUrlTask(){
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    mNotificationLargeIcon = bitmap;
                }
            };
            task.execute(getCurrentTrack().albumThumbnail);
        }

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

    /**
     * Stop the existing media player and force it to stream again
     */
    public void restart() {
        stopForeground(true); // ..and delete notification from status bar
        wasPlaying = isPlaying();
        // Change the current track to another
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
     * @param contextActivity will be launched when user clicks on the status bar notification
     */
    public static void setNotificationActivity(Activity contextActivity){
        mNotificationActivity = contextActivity;
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
     * @return the duration of the track in milliseconds
     */
    public int getDuration() {
        if (!mState.equals(State.Preparing) && !mState.equals(State.Retrieving)) {
            return mMediaPlayer.getDuration();
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
            stopForeground(true); // ..and delete notification from status bar
            mMediaPlayer.pause();
            mState = State.Paused;
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
            setUpAsForeground(); // ..and display notification in status bar
        }
    }

    /**
     * Indicates if the music is either playing or paused
     * pair method {@link TrackDialogFragment}
     */
    public boolean isReady() {
        return mState.equals(State.Playing) || mState.equals(State.Paused);
    }

    /**
     * To the previous track in the track list
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
     * To the following track in the track list
     * pair method {@link TrackDialogFragment}
     */
    public void next(){
        mPosition++;
        if (mPosition > mTrackList.size() - 1) {
            mPosition = 0;
        }
        restart();
    }

    /**
     * Configures service as a foreground service. The user is actively aware of playing music,
     * which appears to the user as a notification. The notification lifetime lasts until
     * the music is paused or otherwise interrupted, then stopForeground() is called and
     * the visible notification is removed.
     */
    private void setUpAsForeground() {

        FoundTrack currentTrack = mTrackList.get(mPosition);
        FoundTrack nextTrack = mTrackList.get(
                (mTrackList.size() - 1 == mPosition) ? 0 : mPosition + 1);

        // Inform user about the audio
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Spotify Player")
                .setContentText(currentTrack.artistName + ": " + currentTrack.name)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setLargeIcon(mNotificationLargeIcon)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText("Now playing..")
                        .addLine(currentTrack.artistName)
                        .addLine(currentTrack.name)
                        .addLine("album " + currentTrack.albumName)
                        .addLine("Next:")
                        .addLine(nextTrack.artistName + ": " + nextTrack.name)
                );

        // Create an intent to launch {@link TrackDialogfragment} via it's context activity
        Intent notificationIntent = new Intent(this, mNotificationActivity.getClass());
        notificationIntent.putExtra(FOREGROUND_NOTIFICATION_TAG, true); // the value means nothing
        // Flags to prefer existing activity to creating a new one
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        startForeground(NOTIFICATION_ID, builder.build());
    }
}
