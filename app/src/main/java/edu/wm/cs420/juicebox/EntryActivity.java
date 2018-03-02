package edu.wm.cs420.juicebox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import edu.wm.cs420.juicebox.database.DatabaseUtils;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;
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
        // Authentifications
        AuthenticationRequest.Builder builder;
        builder = new AuthenticationRequest.Builder(CLIENT_ID, TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-library-read", "user-top-read",
                "playlist-read-private", "user-read-recently-played", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
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
                    // Handle other cases
            }

        }
    }

    private void openMainActivity(){
        Intent nextActivity = new Intent(getBaseContext(), MainActivity.class);
        //give the access token to the next activity so that we can make API calls
        nextActivity.putExtra("token", accessToken);
        startActivity(nextActivity);
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "onLoggedIn: Opening new activity");
        // Query the databse
        SpotifyUtils.getSpotifyService().getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                if(userPrivate.product != "premium"){
                    Log.d(TAG, "User does not have premium subscription!");
                };
                DatabaseUtils.createUser(userPrivate.id, userPrivate.display_name, userPrivate.images);
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
