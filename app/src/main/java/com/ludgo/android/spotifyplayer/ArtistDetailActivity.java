package com.ludgo.android.spotifyplayer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;

/**
 * An activity representing a single Artist top tracks screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ArtistListActivity}.
 */
public class ArtistDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TrackListFragment.ARG_ARTIST_ID,
                    getIntent().getStringExtra(TrackListFragment.ARG_ARTIST_ID));
            arguments.putString(TrackListFragment.ARG_ARTIST_NAME,
                    getIntent().getStringExtra(TrackListFragment.ARG_ARTIST_NAME));
            TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            // Ensure that 'toolbar back' behaves just like bottom back'
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void launchDialog(List<FoundTrack> list, int position){
        FoundTrack fd = list.get(position);
        Log.d("!!!!!!", fd.name);
        Log.d("!!!!!!", fd.duration + "");
        Log.d("!!!!!!", fd.previewUrl);
        Log.d("!!!!!!", fd.artistName);
        Log.d("!!!!!!", fd.albumName);
        Log.d("!!!!!!", fd.albumThumbnail);
        Log.d("!!!!!!", fd.albumPoster);
    }
}
