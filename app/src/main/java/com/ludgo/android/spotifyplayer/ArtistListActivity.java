package com.ludgo.android.spotifyplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * An activity representing a list of artists. This activity has different presentations
 * for handset and tablet-size devices.
 * On handsets, the activity presents a list of artists, which when touched,
 * lead to a {@link ArtistDetailActivity} representing artist top tracks.
 * On tablets, the activity presents the list of artists and artist top tracks side-by-side
 * using two vertical panes. Later, a music player dialog can be displayed in the forefront.
 */
public class ArtistListActivity extends AppCompatActivity {

    public static final String TRACK_DIALOG_FRAGMENT_TAG = "tdf_tag";

    // Whether or not the activity is in two-pane mode,
    static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (savedInstanceState == null) {

            ArtistListFragment fragment = new ArtistListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_list_container, fragment)
                    .commit();
        }

        if (findViewById(R.id.artist_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts.
            mTwoPane = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null
                && getIntent().hasExtra(SpotifyPlayerService.FOREGROUND_NOTIFICATION_TAG)){

            getIntent().removeExtra(SpotifyPlayerService.FOREGROUND_NOTIFICATION_TAG);
            showDialog();
        }
    }

    /**
     * Show {@link TrackDialogFragment} in two pane mode only
     */
    public void showDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackDialogFragment existingFragment =
                (TrackDialogFragment) fragmentManager.findFragmentByTag(TRACK_DIALOG_FRAGMENT_TAG);
        if (existingFragment != null){
            existingFragment.dismiss();
        }
        TrackDialogFragment dialogFragment = new TrackDialogFragment();
        dialogFragment.show(fragmentManager, TRACK_DIALOG_FRAGMENT_TAG);
    }
}
