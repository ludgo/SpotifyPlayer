package com.ludgo.android.spotifyplayer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * An activity representing an artist's top tracks preview player screen. This
 * activity is only used with narrow width devices. On tablet-size devices,
 * player is presented in front of side-by-side master-detail layout
 * in a {@link ArtistListActivity}.
 */
public class TrackPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.player_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            // Ensure that 'toolbar back' behaves just like 'bottom back'
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
