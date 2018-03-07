package edu.wm.cs420.juicebox;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import edu.wm.cs420.juicebox.user.UserUtils;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class EntryActivity extends AppCompatActivity
        implements ConnectionStateCallback {

    private static final String CLIENT_ID = "6098304025324b66af007d562d58830e";

    private static final String REDIRECT_URI = "juicebox.redirect.uri://callback/";
    private static final String TAG = "Juicebox-EntryActivity";

    // Spotify stuff
    private static final int REQUEST_CODE = 1337;
    private String accessToken;
    double latitude;
    double longitude;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

//        Log.d("tag","got here3");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //requestPermissions(EntryActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
         //       123);
        requestPermissions();
        //int MyVersion = Build.VERSION.SDK_INT;
        //if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            //if (!checkIfAlreadyhavePermission()) {
              //  requestForSpecificPermission();

            //}
        //}
        //Toast.makeText(EntryActivity.this, "latitude is" + latitude + ", longitude is" + longitude, Toast.LENGTH_SHORT).show();
        //Log.d("location", "latitude is" + latitude + ", longitude is" + longitude);
        //openMainActivity();
        Button signInButton = (Button) findViewById(R.id.button);
        signInButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v){
                initiateLoginScreen();
            }
        });
    }

    //public void requestPermissions (Activity activity, String[] permissions, int requestCode){
    public void requestPermissions (){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement
            //Toast.makeText(activity, "yes", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);

        }
        //Toast.makeText(activity, "no", Toast.LENGTH_SHORT).show();
//        ActivityCompat.requestPermissions(this,
//                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                123);
    }
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Toast.makeText(EntryActivity.this, "got here" , Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onRequestPermissionsResult");
//        switch (requestCode) {
//            case 123:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //granted
//                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
//                    Log.d("tag3","permission granted");
//                    try{
//                    mLocationRequest = LocationRequest.create()
//                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                            .setInterval(0);
//                    mFusedLocationClient.getLastLocation()
//                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    Log.d("check","got here4");
//                                    // Got last known location. In some rare situations this can be null.
//                                    if (location != null) {
//                                        latitude = location.getLatitude();
//                                        longitude = location.getLongitude();
//                                        Toast.makeText(EntryActivity.this, "latitude is" + latitude + ", longitude is" + longitude, Toast.LENGTH_SHORT).show();
//                                        Log.d("location", "latitude is" + latitude + ", longitude is" + longitude);
//                                    }
//                                }
//                            });
//                    }catch (SecurityException e) {
//
//                    } catch (Exception e) {
//
//                    }
//
//                } else {
//                    //not granted
//                    Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
//                    Log.d("tag4","permission not granted");
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }

    }


    private void initiateLoginScreen(){
        // Authentifications
        AuthenticationRequest.Builder builder;
        builder = new AuthenticationRequest.Builder(CLIENT_ID, TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-library-read", "user-top-read",
                "playlist-read-private", "user-read-recently-played", "streaming"});
        AuthenticationRequest request = builder.build();
        Log.d("initiateLoginScreen", "opening login activity");
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        Log.d("initateLoginScreen", "login activity opened");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Log.d(TAG, "onActivityResult: Received Response Token");
                    accessToken = response.getAccessToken();
                    SpotifyUtils.setAccessToken(accessToken);
                    // For some reason I had to call this manually
                    onLoggedIn();
                    break;
                    // Auth flow returned an error
                case ERROR:
                    Log.d("Juicebox", response.getError() + "  ");
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("OnActivityResult", "Fallen to default case");
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
    }

    private void openMainActivity(){

        Intent nextActivity = new Intent(getBaseContext(), MainActivity.class);
        //give the access token to the next activity so that we can make API calls
        nextActivity.putExtra("token", accessToken);
        startActivity(nextActivity);
    }

    @Override
    public void onLoggedIn(){
        Log.d(TAG, "onLoggedIn: Performing Post-authentication checks.");
        // Query the databse
        SpotifyUtils.getSpotifyService().getMe(new Callback<UserPrivate>() {
            @Override
            public void success(final UserPrivate userPrivate, Response response) {
                if(!userPrivate.product.equals("premium")){
                    // If the user doesn't have a premium account here is where it's detected!
                    Log.d(TAG, "User does not have premium subscription! Subscription is type: "+ userPrivate.product);
                }else{
                    Log.d(TAG, "User had a premium account! Good to go!");
                }
                // Initialize and store information about the user
                UserUtils.initializeUser(userPrivate);
                openMainActivity();
            }

            @Override
            public void failure(RetrofitError error) {
                // I dunno what we should do here
            }
        });
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "onLoggedOut: JuiceboxUser Logged Out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(TAG, "Authentification error " + error.toString());
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "onTemporaryError: Temporary error");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "onConnectionMessage: " + s);
    }
}
