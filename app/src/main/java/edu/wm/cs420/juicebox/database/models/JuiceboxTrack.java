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
    public String   album_title;
    public String   song_artist;
    public String   song_title;
    public long     duration;

    public JuiceboxTrack(){

    }

    public JuiceboxTrack(Track track) {
        this.album_title = track.album.name;
        this.song_artist = track.artists.get(0).name;
        this.song_title  = track.name;
        this.duration    = track.duration_ms;
        this.spotify_id  = track.id;
    }
}
