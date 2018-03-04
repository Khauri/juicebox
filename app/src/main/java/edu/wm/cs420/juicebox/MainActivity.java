package edu.wm.cs420.juicebox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.List;
import java.util.function.Consumer;

import edu.wm.cs420.juicebox.database.DatabaseUtils;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity
        extends AppCompatActivity
        implements QueueFragment.OnFragmentInteractionListener,
        SearchFragment.OnAddToQueueListener,
        SocialFragment.OnFragmentInteractionListener,
        SpotifyPlayer.NotificationCallback {

    private String TAG = "Juicebox-MainActivity";

    private static final int NUM_ITEMS = 3;

    private ViewPager mPager;
    private MyAdapter mAdapter;
    private BottomNavigationView bottomNavigationView;
    // Spotify stuff
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private String accessToken;

    private static QueueFragment queueFragment;
    private static SearchFragment searchFragment;
    private static SocialFragment socialFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = getIntent().getStringExtra("token");
        setContentView(R.layout.activity_main);

        // set up adapter and view pager
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);

        // Change fragment on click
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        return loadFragment(item.getItemId());
                    }
                }
        );

        // change menu item on scroll
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private MenuItem prevItem;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                if(prevItem != null)
                    prevItem.setChecked(false);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });
        mPager.setCurrentItem(1);

        //
        getAlbum();
        //DatabaseUtils.createParty();
    }

    public void getAlbum(){
        Log.d(TAG, "getAlbum: Retrieving my playlists");
        SpotifyUtils.getSpotifyService().getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                List<PlaylistSimple> playlists =  playlistSimplePager.items;
                // Prints out the names for the top 100 albums from the user
                // If we switch to Java 8 we can use a lambda here instead
                playlists.forEach(new Consumer<PlaylistSimple>() {
                    @Override
                    public void accept(PlaylistSimple playlistSimple) {
                        Log.d(TAG, "accept: " + playlistSimple.name);
                    }
                });
//                Log.d(TAG, Arrays.toString(playlistSimplePager.items.toArray()));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "Recieved activity result");
        // Check if result comes from the correct activity
        // The next 19 lines of the code are what you need to copy & paste! :)
    }

    private boolean loadFragment(int id){
        //Fragment fragment = null;
        switch(id){
            case R.id.bottombar_search:
                mPager.setCurrentItem(0, true);
                break;
            case R.id.bottombar_queue:
                mPager.setCurrentItem(1, true);
                break;
            case R.id.bottombar_social:
                mPager.setCurrentItem(2, true);
                break;
            default:
                return false;

        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //do nothing for now
    }

    @Override
    public void onAddToQueue(SongListItem song) {
        if (queueFragment == null){
            Log.d("onAddToQueue", "queueFragment is null!");
        }
        else{
            Log.d("onAddToQueue", "queueFragment is NOT null!");
        }
        queueFragment.updateQueue(song);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    if (searchFragment == null) {
                        searchFragment = SearchFragment.newInstance("Hello", "World");
                    }
                    return searchFragment;
                case 1:
                    if (queueFragment == null) {
                        queueFragment = QueueFragment.newInstance("Hello", "World");
                    }
                    return queueFragment;
                case 2:
                    if (socialFragment == null) {
                        socialFragment = SocialFragment.newInstance("Hello", "World");
                    }
                    return socialFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}
