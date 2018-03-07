package edu.wm.cs420.juicebox;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

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
    private GeofencingClient mGeofencingClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private double longitude;
    private double latitude;
    private LocationRequest mLocationRequest;
    private LocationRequest locationRequest;
    private long lastUpdatedTime = 0;

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
            private int prev = -1;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                // change selected menu item
                if(prev >= 0)
                    bottomNavigationView.getMenu().getItem(prev).setChecked(false);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prev = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });
        mPager.setCurrentItem(1);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestPermissions();

        //getAlbum();
    }
    //public void requestPermissions (Activity activity, String[] permissions, int requestCode){
    public void requestPermissions () {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {//Can add more as per requirement
            //Toast.makeText(activity, "yes", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);

        }else{
        // Create the location request to start receiving updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    return;
        //}

        getLocationOnce();
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        latitude = locationResult.getLastLocation().getLatitude();
                        longitude = locationResult.getLastLocation().getLongitude();
                        //Toast.makeText(MainActivity.this, "longitude is " + longitude + ", latitude is " + latitude, Toast.LENGTH_SHORT).show();
                        onLocationChanged(locationResult.getLastLocation(), false);
                    }
                },
                Looper.myLooper());}
    }

    private void getLocationOnce() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    MainActivity.this.onLocationChanged(location, true);
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    //Toast.makeText(MainActivity.this, "longitude is " + longitude + ", latitude is " + latitude, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) { }
                @Override
                public void onProviderEnabled(String s) { }
                @Override
                public void onProviderDisabled(String s) { }

            }, Looper.getMainLooper());
        } catch (SecurityException e) {

        } catch (Exception e) {

        }
    }

    public void onLocationChanged(Location location, boolean forceUpdate) {
        long currentTime = System.currentTimeMillis();
        if (forceUpdate || currentTime - lastUpdatedTime > 10000) {
            //Log.d("TIMES", currentTime + " " + lastUpdatedTime + " " + (currentTime - lastUpdatedTime));
            lastUpdatedTime = currentTime;
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Toast.makeText(EntryActivity.this, "got here" , Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRequestPermissionsResult");
        // Create the location request to start receiving updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation(), false);
                        latitude = locationResult.getLastLocation().getLatitude();
                        longitude = locationResult.getLastLocation().getLongitude();
                        //Toast.makeText(MainActivity.this, "longitude is " + longitude + ", latitude is " + latitude, Toast.LENGTH_SHORT).show();
                    }
                },
                Looper.myLooper());

    }

    public void getLocation(){
        Log.d("check","got here2");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                    123);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d("check","got here4");
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d("location", "latitude is" + latitude + ", longitude is" + longitude);
                            }
                        }
                    });
        }
        Log.d("tag","got here3");
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

    public static class MyAdapter extends FragmentPagerAdapter {
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

    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
}
