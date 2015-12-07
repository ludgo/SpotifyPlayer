package com.ludgo.android.spotifyplayer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;


public class ArtistListFragment extends Fragment {

    private static final String SAVE_ARTISTS_TAG = "save_artists_tag";

    private ArrayList<FoundArtist> mFoundArtists;
    private RecyclerView mArtistRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

        mFoundArtists = new ArrayList<>();

        mArtistRecyclerView = (RecyclerView) rootView.findViewById(R.id.artist_list);
        assert mArtistRecyclerView != null;

        EditText editText = (EditText) rootView.findViewById(R.id.searchArtist);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    String userInput = v.getText().toString();
                    if (!userInput.equals("")) {
                        searchArtist(userInput);
                        return true;
                    }
                }
                return false;
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVE_ARTISTS_TAG)){
            mFoundArtists = savedInstanceState.getParcelableArrayList(SAVE_ARTISTS_TAG);
            // Populate RecyclerView without search, saved data will be used
            mArtistRecyclerView.setAdapter(
                    new ArtistListFragment.ArtistRecyclerViewAdapter(null));
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFoundArtists != null){
            outState.putParcelableArrayList(SAVE_ARTISTS_TAG, mFoundArtists);
        }
        super.onSaveInstanceState(outState);
    }

    public class ArtistRecyclerViewAdapter
            extends RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder> {

        private final List<Artist> mArtists;

        public ArtistRecyclerViewAdapter(List<Artist> items) {
            mArtists = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.artist_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            if (mArtists != null) {
                // Search was performed
                Artist artist = mArtists.get(position);
                holder.mFoundArtist = new FoundArtist();
                holder.mFoundArtist.id = artist.id;
                holder.mFoundArtist.name = artist.name;
                List<Image> artistImages = artist.images;
                if (artistImages.size() > 0){
                    holder.mFoundArtist.thumbnail = artistImages.get(artistImages.size() - 1).url;
                }
                mFoundArtists.add(holder.mFoundArtist);
            }
            else {
                // Restore saved state
                holder.mFoundArtist = mFoundArtists.get(position);
            }

            Picasso.with(getContext()).load(holder.mFoundArtist.thumbnail).into(holder.mArtistImageView);
            holder.mArtistNameView.setText(holder.mFoundArtist.name);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ArtistListActivity.mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(TrackListFragment.ARG_ARTIST_ID, holder.mFoundArtist.id);
                        arguments.putString(TrackListFragment.ARG_ARTIST_NAME, holder.mFoundArtist.name);
                        TrackListFragment fragment = new TrackListFragment();
                        fragment.setArguments(arguments);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.artist_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ArtistDetailActivity.class);
                        intent.putExtra(TrackListFragment.ARG_ARTIST_ID, holder.mFoundArtist.id);
                        intent.putExtra(TrackListFragment.ARG_ARTIST_NAME, holder.mFoundArtist.name);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mArtists == null) ? mFoundArtists.size() : mArtists.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mArtistImageView;
            public final TextView mArtistNameView;
            public FoundArtist mFoundArtist;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mArtistImageView = (ImageView) view.findViewById(R.id.artistImage);
                mArtistNameView = (TextView) view.findViewById(R.id.artistName);
            }
        }
    }

    /**
     * Fetch artists matching specified String phrase from the Spotify web api
     * and display them to the user
     */
    private class ArtistAsyncTask extends AsyncTask<String, Void, List<Artist>> {

        @Override
        protected List<Artist> doInBackground(String... params) {

            String searchPhrase = params[0];

            // Let the integrated Spotify web api wrapper to request data on demand
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(searchPhrase);
            Pager<Artist> pager = artistsPager.artists;
            return pager.items;
        }

        @Override
        protected void onPostExecute(List<Artist> list) {

            // Populate RecyclerView with found artists
            mArtistRecyclerView.setAdapter(new ArtistListFragment.ArtistRecyclerViewAdapter(list));
        }
    }

    private void searchArtist(String phrase){
        mFoundArtists = new ArrayList<>();
        ArtistAsyncTask task = new ArtistAsyncTask();
        task.execute(phrase);
    }
}
