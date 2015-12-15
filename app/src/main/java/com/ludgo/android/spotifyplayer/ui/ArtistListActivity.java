package com.ludgo.android.spotifyplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ludgo.android.spotifyplayer.R;
import com.ludgo.android.spotifyplayer.service.SpotifyPlayerService;

/**
 * Master activity in master-detail layout with {@link ArtistDetailActivity}
 *
 * An activity representing a list of artists. This activity has different presentations
 * for handset and tablet-size devices.
 * On handsets, the activity presents a list of artists, which when touched,
 * lead to a {@link ArtistDetailActivity} representing artist top tracks.
 * On tablets, the activity presents the list of artists and artist top tracks side-by-side
 * using two vertical panes. Later, a music player dialog can be displayed in the forefront.
 */
public class ArtistListActivity extends AppCompatActivity {

    public static final String TRACK_DIALOG_FRAGMENT_TAG = "tdf_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (savedInstanceState == null) {

            ArtistListFragment fragment = new ArtistListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_list_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /**
         * Due to notification intent in {@link SpotifyPlayerService}
         */
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null
                && getIntent().hasExtra(SpotifyPlayerService.FOREGROUND_NOTIFICATION_TAG)){
            // User has clicked on notification in status bar
            getIntent().removeExtra(SpotifyPlayerService.FOREGROUND_NOTIFICATION_TAG);
            showDialog();
        }
    }

    /**
     * Show {@link TrackDialogFragment} in two pane mode only
     */
    public void showDialog() {
        if (getResources().getBoolean(R.bool.activity_artist_list_two_pane)) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            // Do not duplicate dialogs!
            TrackDialogFragment existingFragment =
                    (TrackDialogFragment) fragmentManager.findFragmentByTag(TRACK_DIALOG_FRAGMENT_TAG);
            if (existingFragment != null) {
                existingFragment.dismiss();
            }
            TrackDialogFragment dialogFragment = new TrackDialogFragment();
            dialogFragment.show(fragmentManager, TRACK_DIALOG_FRAGMENT_TAG);
        }
    }
}
