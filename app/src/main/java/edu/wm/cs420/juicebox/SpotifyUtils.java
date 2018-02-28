package edu.wm.cs420.juicebox;

import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Introduces some singletons able to be accessed everywhere
 * Created by Khauri on 2/27/2018.
 */

public class SpotifyUtils {
    private static Player mPlayer;
    private static String accessToken;
    private static SpotifyApi spotifyApi;
    private static SpotifyService spotifyService;

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
    }

    public static void setApplicationContext(){
        // Set the application context
        // Necessary for using the music player
    }

    public static Player player(){
//        if(mPlayer == null){
//            Config playerConfig = new Config(this, accessToken, CLIENT_ID);
//            Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//                @Override
//                public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                    mPlayer = spotifyPlayer;
//                    mPlayer.addConnectionStateCallback(EntryActivity.this);
//                    mPlayer.addNotificationCallback(EntryActivity.this);
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
//                }
//            });
//        }
        return mPlayer;
    }
}
