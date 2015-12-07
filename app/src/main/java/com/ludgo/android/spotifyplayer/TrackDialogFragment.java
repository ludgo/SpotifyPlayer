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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TrackDialogFragment extends DialogFragment {

    public static final String ARG_TRACK_LIST = "track_list";
    public static final String ARG_TRACK_POSITION = "track_position";

    // Essential data from {@link TrackListFragment}
    private ArrayList<FoundTrack> mTrackList;
    private int mPosition;

    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private ImageView mAlbumImageView;
    private TextView mTrackTextView;
    private ProgressBar mTrackProgressBar;
    private Button mPreviousButton;
    private Button mPauseButton;
    private Button mPlayButton;
    private Button mNextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrackList = new ArrayList<>();

        if (getArguments().containsKey(ARG_TRACK_LIST)
                && getArguments().containsKey(ARG_TRACK_POSITION)) {

            mTrackList = getArguments().getParcelableArrayList(ARG_TRACK_LIST);
            mPosition = getArguments().getInt(ARG_TRACK_POSITION);
        }
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View rootView = inflater.inflate(R.layout.fragment_track_dialog, container, false);

        mArtistTextView = (TextView) rootView.findViewById(R.id.artistDialog);
        mAlbumTextView = (TextView) rootView.findViewById(R.id.albumDialog);
        mAlbumImageView = (ImageView) rootView.findViewById(R.id.imageDialog);
        mTrackTextView = (TextView) rootView.findViewById(R.id.trackDialog);
        mTrackProgressBar = (ProgressBar) rootView.findViewById(R.id.progressDialog);
        mPreviousButton = (Button) rootView.findViewById(R.id.previousDialog);
        mPauseButton = (Button) rootView.findViewById(R.id.pauseDialog);
        mPlayButton = (Button) rootView.findViewById(R.id.playDialog);
        mNextButton = (Button) rootView.findViewById(R.id.nextDialog);

        playTrack();

        return  rootView;
    }

    /** The system calls this only when creating the layout in a dialog. */
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

    private void playTrack() {

        if (mTrackList != null
                && mPosition >= 0
                && mPosition < mTrackList.size()){

            FoundTrack currentTrack = mTrackList.get(mPosition);

            mArtistTextView.setText(currentTrack.artistName);
            mAlbumTextView.setText(currentTrack.albumName);
            Picasso.with(getActivity()).load(currentTrack.albumPoster).into(mAlbumImageView);
            mTrackTextView.setText(currentTrack.name);
//        mTrackProgressBar;
//        mPreviousButton;
//        mPauseButton;
//        Button mPlayButton;
//        Button mNextButton;
        }
    }
}
