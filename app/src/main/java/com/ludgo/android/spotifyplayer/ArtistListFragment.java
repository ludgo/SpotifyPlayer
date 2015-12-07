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

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;


public class ArtistListFragment extends Fragment {

    private static final String SEARCH_TAG = "search_tag";
    private String mSearchPhrase;

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

        mArtistRecyclerView = (RecyclerView) rootView.findViewById(R.id.artist_list);
        assert mArtistRecyclerView != null;

        EditText editText = (EditText) rootView.findViewById(R.id.searchArtist);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    mSearchPhrase = v.getText().toString();
                    if (!mSearchPhrase.equals("")) {
                        searchArtist(mSearchPhrase);
                        return true;
                    }
                }
                return false;
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.containsKey(SEARCH_TAG)){
            mSearchPhrase = savedInstanceState.getString(SEARCH_TAG);
            searchArtist(mSearchPhrase);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSearchPhrase != null){
            outState.putString(SEARCH_TAG, mSearchPhrase);
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

            holder.mArtist = mArtists.get(position);

            List<Image> artistImages = mArtists.get(position).images;
            if (artistImages != null && artistImages.size() > 0){
                String url = artistImages.get(artistImages.size() - 1).url;
                Picasso.with(getActivity()).load(url).into(holder.mArtistImageView);
            }

            holder.mArtistNameView.setText(mArtists.get(position).name);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ArtistListActivity.mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(TrackListFragment.ARG_ARTIST_ID, holder.mArtist.id);
                        arguments.putString(TrackListFragment.ARG_ARTIST_NAME, holder.mArtist.name);
                        TrackListFragment fragment = new TrackListFragment();
                        fragment.setArguments(arguments);
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.artist_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ArtistDetailActivity.class);
                        intent.putExtra(TrackListFragment.ARG_ARTIST_ID, holder.mArtist.id);
                        intent.putExtra(TrackListFragment.ARG_ARTIST_NAME, holder.mArtist.name);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArtists.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mArtistImageView;
            public final TextView mArtistNameView;
            public Artist mArtist;

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
        ArtistAsyncTask task = new ArtistAsyncTask();
        task.execute(phrase);
    }
}
