package com.ludgo.android.spotifyplayer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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

import java.util.ArrayList;

public class TrackDialogFragment extends DialogFragment implements MediaController.MediaPlayerControl,
        SeekBar.OnSeekBarChangeListener {

    // Necessary to recreate after orientation change
    private static final String SAVE_TRACK_LIST_TAG = "save_track_list_tag";
    private static final String SAVE_POSITION_TAG = "save_position_tag";

    // Essential data come from {@link TrackListFragment}
    public static final String ARG_TRACK_LIST = "track_list";
    public static final String ARG_TRACK_POSITION = "track_position";

    private ArrayList<FoundTrack> mTrackList;
    private int mPosition;
    private String mPositionUrl;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrackList = new ArrayList<>();

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVE_TRACK_LIST_TAG)
                && savedInstanceState.containsKey(SAVE_POSITION_TAG)) {
            // restore previous state
            mTrackList = savedInstanceState.getParcelableArrayList(SAVE_TRACK_LIST_TAG);
            mPosition = savedInstanceState.getInt(SAVE_POSITION_TAG);
        } else if (getActivity().getIntent() != null
                && getActivity().getIntent().hasExtra(ARG_TRACK_LIST)
                && getActivity().getIntent().hasExtra(ARG_TRACK_POSITION)) {
            // one pane mode
            mTrackList = getActivity().getIntent().getParcelableArrayListExtra(ARG_TRACK_LIST);
            mPosition = getActivity().getIntent().getIntExtra(ARG_TRACK_POSITION, 0);
        } else if (getArguments().containsKey(ARG_TRACK_LIST)
                && getArguments().containsKey(ARG_TRACK_POSITION)) {
            // two pane mode
            mTrackList = getArguments().getParcelableArrayList(ARG_TRACK_LIST);
            mPosition = getArguments().getInt(ARG_TRACK_POSITION);
        }
    }

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
                if (mTrackList != null) {
                    mPosition--;
                    if (mPosition < 0) {
                        mPosition = mTrackList.size() - 1;
                    }
                    setCurrentTrack();
                    restart();
                }
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
                if (mTrackList != null) {
                    mPosition++;
                    if (mPosition > mTrackList.size() - 1) {
                        mPosition = 0;
                    }
                    setCurrentTrack();
                    restart();
                }
            }
        });

        setCurrentTrack();
        if (SpotifyPlayerService.getInstance() == null) {
            SpotifyPlayerService.setUrl(mPositionUrl);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("PLAY_SPOTIFY");
                    intent.setPackage("com.ludgo.android.spotifyplayer");
                    getActivity().startService(intent);
                }
            }).start();
        } else if (!mPositionUrl.equals(SpotifyPlayerService.getUrl())) {
            restart();
        }
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
    public void onSaveInstanceState(Bundle outState) {
        if (mTrackList != null) {
            outState.putParcelableArrayList(SAVE_TRACK_LIST_TAG, mTrackList);
            outState.putInt(SAVE_POSITION_TAG, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void setCurrentTrack() {

        if (mPosition >= 0 && mPosition < mTrackList.size()) {

            final FoundTrack currentTrack = mTrackList.get(mPosition);

            mArtistTextView.setText(currentTrack.artistName);
            mAlbumTextView.setText(currentTrack.albumName);
            Picasso.with(getActivity()).load(currentTrack.albumPoster).into(mPosterImageView);
            mTrackTextView.setText(currentTrack.name);

            mPositionUrl = currentTrack.previewUrl;
        }
    }

    private void runMusicControlThread() {

        musicThreadFinished = false;
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
                                mPlayPauseButton.setChecked(isPlaying());
                                if (isReady()) {
                                    mCurrentTimeTextView.setText(
                                            Utilities.millisecondsToTime(currentPosition));
                                    mTotalTimeTextView.setText(
                                            Utilities.millisecondsToTime(duration));
                                    mPlayPauseButton.setEnabled(true);
                                    if (isCompleted()) {
                                        mNextButton.performClick();
                                    }
                                } else {
                                    mCurrentTimeTextView.setText("-:--");
                                    mTotalTimeTextView.setText("-:--");
                                    mPlayPauseButton.setEnabled(false);
                                }
                            }
                        });
                    } catch (NullPointerException e) {
                        musicThreadFinished = true;
                        Log.d("****", "musicThreadFinished");
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
        // TODO Auto-generated method stub
    }

    /**
     * SeekBar.OnSeekBarChangeListener
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
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
            return SpotifyPlayerService.getInstance().getBufferPercentageMusic();
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public int getCurrentPosition() {
        if (SpotifyPlayerService.getInstance() != null) {
            return SpotifyPlayerService.getInstance().getCurrentPositionMusic();
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public int getDuration() {
        if (SpotifyPlayerService.getInstance() != null) {
            return SpotifyPlayerService.getInstance().getDurationMusic();
        }
        return 0;
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public boolean isPlaying() {
        return SpotifyPlayerService.getInstance() != null
                && SpotifyPlayerService.getInstance().isPlayingMusic();
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public void pause() {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().pauseMusic();
        }
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public void seekTo(int pos) {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().seekToMusic(pos);
        }
    }

    /**
     * MediaController.MediaPlayerControl, pair method {@link SpotifyPlayerService}
     */
    @Override
    public void start() {
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().startMusic();
        }
    }

    /**
     * pair method {@link SpotifyPlayerService}
     */
    public boolean isReady() {
        return SpotifyPlayerService.getInstance() != null
                && SpotifyPlayerService.getInstance().isReadyMusic();
    }

    /**
     * pair method {@link SpotifyPlayerService}
     */
    public boolean isCompleted() {
        return SpotifyPlayerService.getInstance() != null
                && SpotifyPlayerService.getInstance().isCompletedMusic();
    }

    /**
     * pair method {@link SpotifyPlayerService}
     */
    public void restart() {
        SpotifyPlayerService.setUrl(mPositionUrl);
        if (SpotifyPlayerService.getInstance() != null) {
            SpotifyPlayerService.getInstance().restartMusic();
        }
    }
}
