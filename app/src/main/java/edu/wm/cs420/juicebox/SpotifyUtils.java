package edu.wm.cs420.juicebox;

import android.content.Context;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Introduces some singletons able to be accessed everywhere
 * Created by Khauri on 2/27/2018.
 */

public class SpotifyUtils
        implements
        ConnectionStateCallback,
        Player.NotificationCallback
{
    private static String TAG = "Juicebox-SpotifyUtils";
    private static Player mPlayer;
    private static String clientId = "6098304025324b66af007d562d58830e";
    private static String accessToken;
    private static SpotifyApi spotifyApi;
    private static SpotifyService spotifyService;
    private static SpotifyUtils instance = new SpotifyUtils();
    private static String prev_track_id;

    private static boolean ready = false;

    private SpotifyUtils(){}

    public static SpotifyApi getSpotifyApi(){
        if(spotifyApi == null){
            spotifyApi = new SpotifyApi();
        }
        return spotifyApi;
    }

    public static SpotifyService getSpotifyService(){
        if(spotifyService == null){
            spotifyService = getSpotifyApi().getService();
        }
        return spotifyService;
    }

    public static String getAccessToken(){
        return accessToken;
    }

    public static void setAccessToken(String token) {
        accessToken =  token;
        getSpotifyApi().setAccessToken(accessToken);
        Log.d("good", "reached end of method");
    }

    public static void setApplicationContext(){
        // Set the application context
        // Necessary for using the music player
    }

    public static Player initializePlayer(Context context){
        if(mPlayer == null){
            Config playerConfig = new Config(context, accessToken, clientId);
            Spotify.getPlayer(playerConfig, instance, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    mPlayer = spotifyPlayer;
                    mPlayer.addConnectionStateCallback(instance);
                    mPlayer.addNotificationCallback(instance);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                }
            });
        }
        return mPlayer;
    }

    public static void playSong(String track_id, Player.OperationCallback cb){
        if(mPlayer == null){
            // player was not initialized
            Log.d(TAG, "playSong: Player not initialized");
            return;
        }
        if(mPlayer.getPlaybackState().isPlaying && track_id.equals(prev_track_id)) {
            // song is already playing
            Log.d(TAG, "playSong: song already playing");
            return;
        }
        Log.d(TAG, "playSong: " + track_id);
        mPlayer.playUri(cb, "spotify:track:" + track_id, 0, 0);
    }

    public static void pause(Player.OperationCallback cb) {
        if(mPlayer == null){
            // player was not initialized
            return;
        }
        if(mPlayer.getPlaybackState().isPlaying){
            mPlayer.pause(cb);
        };
    }

    public static void onDestroy(){
        Spotify.destroyPlayer(mPlayer);
    }

    public static String getClientId() {
        return clientId;
    }

    @Override
    public void onLoggedIn() {
        SpotifyUtils.ready = true;
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "onConnectionMessage: " + s);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "onPlaybackEvent: " + playerEvent.toString());
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "onPlaybackError: " + error.toString());
    }
}
