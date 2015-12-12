package com.ludgo.android.spotifyplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * UI for recycler view populated with single artist top tracks via {@link TrackRecyclerViewAdapter}
 * This informs {@link SpotifyPlayerService} what to play
 */
public class TrackListFragment extends Fragment {

    // Necessary to recreate after orientation change
    // or essential data come from {@link ArtistListFragment}
    public static final String TRACKS_TAG = "tracks_tag";
    public static final String ARTIST_ID_TAG = "artist_id_tag";
    public static final String ARTIST_NAME_TAG = "artist_name_tag";

    // The fragment arguments representing chosen artist,
    // required only for an initial fetch, not to be stored later
    private String mArtistId;
    private String mArtistName;

    private ArrayList<FoundTrack> mFoundTracks;
    private RecyclerView mTrackRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFoundTracks = new ArrayList<>();

        if (getArguments().containsKey(ARTIST_ID_TAG)
                && getArguments().containsKey(ARTIST_NAME_TAG)) {

            mArtistId = getArguments().getString(ARTIST_ID_TAG);
            mArtistName = getArguments().getString(ARTIST_NAME_TAG);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mArtistName);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        mFoundTracks = new ArrayList<>();

        mTrackRecyclerView = (RecyclerView) rootView.findViewById(R.id.artist_detail);
        assert mTrackRecyclerView != null;

        if (savedInstanceState != null
                && savedInstanceState.containsKey(TRACKS_TAG)){
            mFoundTracks = savedInstanceState.getParcelableArrayList(TRACKS_TAG);
            // Populate recycler view without search, saved data will be used
            mTrackRecyclerView.setAdapter(
                    new TrackListFragment.TrackRecyclerViewAdapter(null));
        }
        else if (mArtistId != null) {
            // Initiate async task right in the beginning
            TrackAsyncTask task = new TrackAsyncTask();
            task.execute(mArtistId);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFoundTracks != null){
            outState.putParcelableArrayList(TRACKS_TAG, mFoundTracks);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * An adapter to populate this class' recycler view with single artists top tracks
     */
    public class TrackRecyclerViewAdapter
            extends RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder> {

        private final List<Track> mTracks;

        public TrackRecyclerViewAdapter(List<Track> items) {
            mTracks = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.track_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            if (mTracks != null) {
                // Search was performed
                Track track = mTracks.get(position);
                holder.mFoundTrack = new FoundTrack();
                holder.mFoundTrack.name = track.name;
                holder.mFoundTrack.duration = track.duration_ms;
                holder.mFoundTrack.previewUrl = track.preview_url;
                holder.mFoundTrack.artistName = mArtistName;
                holder.mFoundTrack.albumName = track.album.name;
                List<Image> albumImages = track.album.images;
                if (albumImages.size() > 0) {
                    holder.mFoundTrack.albumThumbnail = albumImages.get(albumImages.size() - 1).url;
                    for (int i = albumImages.size() - 1; i >= 0; i--) {
                        if (albumImages.get(i).width >= 200) {
                            holder.mFoundTrack.albumPoster = albumImages.get(i).url;
                            break;
                        }
                    }
                }
                mFoundTracks.add(holder.mFoundTrack);
            }
            else {
                // Restore saved state
                holder.mFoundTrack = mFoundTracks.get(position);
            }

            Picasso.with(getContext()).load(holder.mFoundTrack.albumThumbnail).into(holder.mAlbumImageView);
            holder.mTrackNameView.setText(holder.mFoundTrack.name);
            holder.mAlbumNameView.setText(holder.mFoundTrack.albumName);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SpotifyPlayerService.getInstance() == null) {
                        SpotifyPlayerService.setTrackList(mFoundTracks);
                        SpotifyPlayerService.setPosition(position);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent("PLAY_SPOTIFY");
                                intent.setPackage("com.ludgo.android.spotifyplayer");
                                getActivity().startService(intent);
                            }
                        }).start();
                    }
                    else if (position != SpotifyPlayerService.getPosition()
                            || !mFoundTracks.equals(SpotifyPlayerService.getTrackList())) {
                        SpotifyPlayerService.setTrackList(mFoundTracks);
                        SpotifyPlayerService.setPosition(position);
                        SpotifyPlayerService.getInstance().restart();
                    }

                    if(ArtistListActivity.mTwoPane){
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        TrackDialogFragment dialogFragment = new TrackDialogFragment();
                        dialogFragment.show(fragmentManager, "unused_tag");
                    }
                    else {
                        Context context = v.getContext();
                        context.startActivity(new Intent(context, TrackPlayerActivity.class));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mTracks == null) ? mFoundTracks.size() : mTracks.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mAlbumImageView;
            public final TextView mTrackNameView;
            public final TextView mAlbumNameView;
            public FoundTrack mFoundTrack;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mAlbumImageView = (ImageView) view.findViewById(R.id.albumImage);
                mTrackNameView = (TextView) view.findViewById(R.id.trackName);
                mAlbumNameView = (TextView) view.findViewById(R.id.albumName);
            }
        }
    }

    /**
     * Fetch single artist top tracks by specified artist from the Spotify web api
     * and display them to the user
     */
    private class TrackAsyncTask extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... params) {

            String artistId = params[0];

            // Let the integrated Spotify web api wrapper to request demanded data
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Tracks artistTracks = spotify.getArtistTopTrack(artistId, "us");
            return artistTracks.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> list) {

            // Populate recycler view with found top tracks
            mTrackRecyclerView.setAdapter(new TrackListFragment.TrackRecyclerViewAdapter(list));
        }
    }
}
