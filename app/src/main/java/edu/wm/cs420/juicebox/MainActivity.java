package edu.wm.cs420.juicebox;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

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

public class MainActivity
        extends AppCompatActivity
        implements QueueFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        SocialFragment.OnFragmentInteractionListener{
        //SpotifyPlayer.NotificationCallback, ConnectionStateCallback{

    private static final int NUM_ITEMS = 3;

    private static final String CLIENT_ID = "6098304025324b66af007d562d58830e";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";

    private ViewPager mPager;
    private MyAdapter mAdapter;
    private BottomNavigationView bottomNavigationView;
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Authentifications
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);



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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("Juicebox", "Recieved activity result");
//
//        // Check if result comes from the correct activity
//        // The next 19 lines of the code are what you need to copy & paste! :)
//        if (requestCode == REQUEST_CODE) {
//            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
//            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
//                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
//                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//                    @Override
//                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                        mPlayer = spotifyPlayer;
//                        mPlayer.addConnectionStateCallback(MainActivity.this);
//                        mPlayer.addNotificationCallback(MainActivity.this);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
//                    }
//                });
//            }
//        }
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

//    // Authentification callbacks
//    @Override
//    public void onLoggedIn() {
//        Log.d("MainActivity", "User logged in");
//    }
//
//    @Override
//    public void onLoggedOut() {
//        Log.d("MainActivity", "User logged out");
//    }
//
//    @Override
//    public void onLoginFailed(Error var1) {
//        Log.d("MainActivity", "Login failed");
//    }
//
//    @Override
//    public void onTemporaryError() {
//        Log.d("MainActivity", "Temporary error occurred");
//    }
//
//    @Override
//    public void onConnectionMessage(String message) {
//        Log.d("MainActivity", "Received connection message: " + message);
//    }
//}
}
