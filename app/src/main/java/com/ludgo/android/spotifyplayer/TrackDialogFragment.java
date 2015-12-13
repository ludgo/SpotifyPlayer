package com.ludgo.android.spotifyplayer;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

/**
 * UI for media player embedded in {@link TrackPlayerActivity} on handset devices
 * or presented as dialog in {@link ArtistListActivity} on tablet-size devices.
 * This informs {@link SpotifyPlayerService} about user interaction
 */
public class TrackDialogFragment extends DialogFragment implements MediaController.MediaPlayerControl,
        SeekBar.OnSeekBarChangeListener {

    private FoundTrack mCurrentTrack;
    private int mCurrentPosition;

    private boolean musicThreadFinished;

    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private ImageView mPosterImageView;
    private TextView mTrackTextView;
    private SeekBar mSeekBar;
    private TextView mCurrentTimeTextView;
    private TextView mTotalTimeTextView;
    private Button mPreviousButton;
    private ToggleButton mPlayPauseButton;
    private Button mNextButton;

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View rootView = inflater.inflate(R.layout.fragment_track_dialog, container, false);

        mArtistTextView = (TextView) rootView.findViewById(R.id.dialogArtist);
        mAlbumTextView = (TextView) rootView.findViewById(R.id.dialogAlbum);
        mPosterImageView = (ImageView) rootView.findViewById(R.id.dialogPoster);
        mTrackTextView = (TextView) rootView.findViewById(R.id.dialogTrack);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.dialogSeekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mCurrentTimeTextView = (TextView) rootView.findViewById(R.id.dialogCurrentTime);
        mTotalTimeTextView = (TextView) rootView.findViewById(R.id.dialogTotalTime);
        mPreviousButton = (Button) rootView.findViewById(R.id.dialogPrevious);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
            }
        });
        mPlayPauseButton = (ToggleButton) rootView.findViewById(R.id.dialogPlayPause);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPauseButton.isChecked()) {
                    // Pause icon visible
                    start();
                } else {
                    // Play icon visible
                    pause();
                }
            }
        });
        mNextButton = (Button) rootView.findViewById(R.id.dialogNext);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        SpotifyPlayerService.setNotificationActivity(getActivity());
        updateCurrentTrack();
        runMusicControlThread();

        return rootView;
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        // This thread means something only if UI exists
        musicThreadFinished = true;
        super.onDestroyView();
    }

    private void updateCurrentTrack() {

        mCurrentTrack = SpotifyPlayerService.getCurrentTrack();
        mCurrentPosition = SpotifyPlayerService.getPosition();
        // Show appropriate user interface
        mArtistTextView.setText(mCurrentTrack.artistName);
        mAlbumTextView.setText(mCurrentTrack.albumName);
        Picasso.with(getActivity()).load(mCurrentTrack.albumPoster).into(mPosterImageView);
        mTrackTextView.setText(mCurrentTrack.name);
    }

    private void runMusicControlThread() {

        musicThreadFinished = false;
        // Thread to check UI changes
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!musicThreadFinished) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }

                    final int currentPosition = getCurrentPosition();
                    final int duration = getDuration();

                    mSeekBar.setMax(duration);
                    mSeekBar.setProgress(currentPosition);
                    mSeekBar.setSecondaryProgress(getBufferPercentage());

                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mPlayPauseButton.setChecked(isPlaying());
                                    if (isReady()) {
                                        mCurrentTimeTextView.setText(
                                                Utilities.millisecondsToTime(currentPosition));
                                        mTotalTimeTextView.setText(
                                                Utilities.millisecondsToTime(duration));
                                        mPlayPauseButton.setEnabled(true);
                                    } else {
                                        mCurrentTimeTextView.setText("-:--");
                                        mTotalTimeTextView.setText("-:--");
                                        mPlayPauseButton.setEnabled(false);
                                    }
                                    if (mCurrentPosition != SpotifyPlayerService.getPosition()
                                            || !mCurrentTrack.equals(SpotifyPlayerService.getCurrentTrack())){
                                        updateCurrentTrack();
                                    }
                                } catch (NullPointerException e) {
                                    // This thread means something only if UI exists
                                    musicThreadFinished = true;
                                }
                            }
                        });
                    } catch (NullPointerException e) {
                        // This thread means something only if UI exists
                        musicThreadFinished = true;
                    }
                }
            }
        }).start();
    }

    /**
     * SeekBar.OnSeekBarChangeListener
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            seekTo(progress);
        }
    }

    /**
     * SeekBar.OnSeekBarChangeListener
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // User pressed seek bar
    }

    /**
     * SeekBar.OnSeekBarChangeListener
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // User stopped pressing seek bar
    }

    /**
     * MediaController.MediaPlayerControl
     */
    @Override
    public boolean canPause() {
        return true;
    }

    /**
     * MediaController.MediaPlayerControl
     */
    @Override
    public boolean canSeekBackward() {
        return true;
    }

    /**
     * MediaController.MediaPlayerControl
     */
    @Override
    public boolean canSeekForward() {
        return true;
    }

    /**
     * MediaController.MediaPlayerControl
     */
    @Override
    public int getAudioSessionId() {
        // not used
        return -1;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public int getBufferPercentage() {
        if (SpotifyPlayerService.getInstance() != null) {
            return SpotifyPlayerService.getInstance().getBufferPercentage();
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public int getCurrentPosition() {
        if (SpotifyPlayerService.getInstance() != null) {
            return SpotifyPlayerService.getInstance().getCurrentPosition();
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public int getDuration() {
        if (SpotifyPlayerService.getInstance() != null) {
            return SpotifyPlayerService.getInstance().getDuration();
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public boolean isPlaying() {
        return SpotifyPlayerService.getInstance() != null
                && SpotifyPlayerService.getInstance().isPlaying();
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public void pause() {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().pause();
        }
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public void seekTo(int pos) {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().seekTo(pos);
        }
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public void start() {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().start();
        }
    }

    /**
     * pair method {@link SpotifyPlayerService}
     */
    public boolean isReady() {
        return SpotifyPlayerService.getInstance() != null
                && SpotifyPlayerService.getInstance().isReady();
    }

    /**
     * pair method {@link SpotifyPlayerService}
     */
    public void previous() {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().previous();
        }
    }

    /**
     * pair method {@link SpotifyPlayerService}
     */
    public void next() {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().next();
        }
    }
}
