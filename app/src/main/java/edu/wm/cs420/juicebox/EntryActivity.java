package edu.wm.cs420.juicebox;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import static com.spotify.sdk.android.authentication.AuthenticationResponse.Type.TOKEN;

public class EntryActivity extends AppCompatActivity
        implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final int NUM_ITEMS = 3;

    private static final String CLIENT_ID = "6098304025324b66af007d562d58830e";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "juicebox.redirect.uri://callback/";

    private ViewPager mPager;
    private MainActivity.MyAdapter mAdapter;
    private BottomNavigationView bottomNavigationView;
    // Spotify stuff
    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        Button signInButton = (Button) findViewById(R.id.button);
        signInButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v){
                initiateLoginScreen();
            }
        });
    }

    private void initiateLoginScreen(){
        setContentView(R.layout.activity_entry);
        // Authentifications
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
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
                            mPlayer.addConnectionStateCallback(EntryActivity.this);
                            mPlayer.addNotificationCallback(EntryActivity.this);
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

    @Override
    public void onLoggedIn() {
        Intent nextActivity = new Intent(getBaseContext(), MainActivity.class);
        //give the access token to the next activity so that we can make API calls
        nextActivity.putExtra("token", accessToken);
        startActivity(nextActivity);
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
}
