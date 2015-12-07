package com.ludgo.android.spotifyplayer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

/**
 * An activity representing a list of Artists. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of artists, which when touched,
 * lead to a {@link ArtistDetailActivity} representing
 * artist top tracks. On tablets, the activity presents the list of artists and
 * artist top tracks side-by-side using two vertical panes.
 */
public class ArtistListActivity extends AppCompatActivity {

    // Whether or not the activity is in two-pane mode,
    // i.e. running on a tablet device.
    static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (savedInstanceState == null) {
            // Create the main fragment and add it to the activity
            // using a fragment transaction.
            ArtistListFragment fragment = new ArtistListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_list_container, fragment)
                    .commit();
        }

        if (findViewById(R.id.artist_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts.
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    void launchDialog(ArrayList<FoundTrack> list, int position){

        if (mTwoPane) {
            // The device is using a large layout, so show the fragment as a dialog
            FragmentManager fragmentManager = getSupportFragmentManager();
            TrackDialogFragment dialogFragment = new TrackDialogFragment();

            Bundle arguments = new Bundle();
            arguments.putParcelableArrayList(TrackDialogFragment.ARG_TRACK_LIST, list);
            arguments.putInt(TrackDialogFragment.ARG_TRACK_POSITION, position);
            dialogFragment.setArguments(arguments);

            dialogFragment.show(fragmentManager, "dialog_tag");
        }
    }
}
