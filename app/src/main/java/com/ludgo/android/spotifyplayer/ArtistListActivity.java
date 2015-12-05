package com.ludgo.android.spotifyplayer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * An activity representing a list of Artists. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of artists, which when touched,
 * lead to a {@link ArtistDetailActivity} representing
 * artist top tracks. On tablets, the activity presents the list of artists and
 * artist top tracks side-by-side using two vertical panes.
 */
public class ArtistListActivity extends AppCompatActivity {

    private RecyclerView mArtistRecyclerView;
    // Whether or not the activity is in two-pane mode,
    // i.e. running on a tablet device.
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mArtistRecyclerView = (RecyclerView) findViewById(R.id.artist_list);
        assert mArtistRecyclerView != null;

        if (findViewById(R.id.artist_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts.
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

        ArtistAsyncTask task = new ArtistAsyncTask();
        task.execute("Nicki");
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
                Picasso.with(getBaseContext()).load(url).into(holder.mArtistImageView);
            }

            holder.mArtistNameView.setText(mArtists.get(position).name);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(TrackListFragment.ARG_ARTIST_ID, holder.mArtist.id);
                        arguments.putString(TrackListFragment.ARG_ARTIST_NAME, holder.mArtist.name);
                        TrackListFragment fragment = new TrackListFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.artist_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ArtistDetailActivity.class);
                        intent.putExtra(TrackListFragment.ARG_ARTIST_ID, holder.mArtist.id);

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
            mArtistRecyclerView.setAdapter(new ArtistListActivity.ArtistRecyclerViewAdapter(list));
        }
    }
}