package edu.wm.cs420.juicebox;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.bluetooth.BluetoothDevice.ERROR;
import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class MainActivity
        extends AppCompatActivity
        implements QueueFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        SocialFragment.OnFragmentInteractionListener,
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private String TAG = "MainActivity";

    private static final int NUM_ITEMS = 3;

    private static final String CLIENT_ID = "6098304025324b66af007d562d58830e";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "juicebox.redirect.uri://callback/";

    private ViewPager mPager;
    private MyAdapter mAdapter;
    private BottomNavigationView bottomNavigationView;
    // Spotify stuff
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private String accessToken;

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
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                // change selected menu item
                // be careful of recursive accidents
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });
        mPager.setCurrentItem(1);
    }

    public void getAlbum(){
        Log.d(TAG, "getAlbum: Getting Album");
        SpotifyApi api = new SpotifyApi();

// Most (but not all) of the Spotify Web API endpoints require authorisation.
// If you know you'll only use the ones that don't require authorisation you can skip this step
        String accToken = getAccessToken();
        api.setAccessToken(accToken);

        SpotifyService spotify = api.getService();
        spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                Log.d(TAG, playlistSimplePager.toString());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
//        Log.d("playlist", playlist.toString());

        spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                Log.d("Album success", album.name);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }

    public String getAccessToken(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + accessToken);
                    }
                })
                .build();

        SpotifyService spotify = restAdapter.create(SpotifyService.class);
        Log.d(TAG, "getAccessToken: " + accessToken);
        return accessToken;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("Juicebox", "Recieved activity result");
        // Check if result comes from the correct activity
        // The next 19 lines of the code are what you need to copy & paste! :)
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d("Juicebox", response.getError() + "  ");
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    accessToken = response.getAccessToken();
                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                        @Override
                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
                            mPlayer = spotifyPlayer;
                            mPlayer.addConnectionStateCallback(MainActivity.this);
                            mPlayer.addNotificationCallback(MainActivity.this);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        }
                    });
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d("Error", "Error");
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }

        }

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
    public void onFragmentInteraction(Uri uri) {
        // Does nothing but is required
    }

    @Override
    public void onLoggedIn() {
        mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
        getAlbum();
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("jukebox", "Authentification error " + error.toString());
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

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

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return SearchFragment.newInstance("Hello", "World");
                case 1:
                    return QueueFragment.newInstance("Hello", "World");
                case 2:
                    return SocialFragment.newInstance("Hello", "World");
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}
