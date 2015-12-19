package com.ludgo.android.spotifyplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ludgo.android.spotifyplayer.R;

/**
 * Detail activity in master-detail layout with {@link ArtistListActivity}
 *
 * An activity representing a single artist top tracks screen. This
 * activity is only used with narrow width devices. On tablet-size devices,
 * top tracks are presented side-by-side with all artists
 * in a {@link ArtistListActivity}.
 */
public class ArtistDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putString(TrackListFragment.ARTIST_ID_TAG,
                    getIntent().getStringExtra(TrackListFragment.ARTIST_ID_TAG));
            arguments.putString(TrackListFragment.ARTIST_NAME_TAG,
                    getIntent().getStringExtra(TrackListFragment.ARTIST_NAME_TAG));

            TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // This ID represents the Home or Up button.
                // Ensure that 'toolbar back' behaves just like 'bottom back'
                onBackPressed();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
