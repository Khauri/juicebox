package edu.wm.cs420.juicebox.database.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Khauri on 2/28/2018.
 */

@IgnoreExtraProperties
public class JuiceboxTrack {

    // metadata
    public String   spotify_id;     // id of the song on spotify
    public String   user_id;        // id of user that selected the song
    public String   user_name;      // name of user that selected the song
    public int      reputation;     // the song's reputation

    // cached information about the song
    public String   album_name;
    public String   track_artists;
    public String   track_name;
    public String   album_img;
    public long     duration;

    public JuiceboxTrack(){

    }

    public JuiceboxTrack(Track track) {
        this.album_name = track.album.name;
        this.track_artists = track.artists.get(0).name;
        this.track_name  = track.name;
        this.duration    = track.duration_ms;
        this.spotify_id  = track.id;
    }
}
