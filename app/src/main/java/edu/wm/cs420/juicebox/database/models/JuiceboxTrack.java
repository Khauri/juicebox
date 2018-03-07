package edu.wm.cs420.juicebox.database.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.StringJoiner;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Khauri on 2/28/2018.
 */

@IgnoreExtraProperties
public class JuiceboxTrack {

    // metadata
    public String   user_id;        // id of user that selected the song
    public String   user_name;      // name of user that selected the song
    public String   user_pic_url;   // url to a small image of the user
    public int      reputation;     // the song's reputation

    // cached information about the song
    public String   track_id;     // id of the song on spotify
    public String   album_name;
    public String   track_artists;
    public String   track_name;
    public String   album_img;
    public long     duration = 0;

    public JuiceboxTrack(){

    }

    public JuiceboxTrack(Track track, JuiceboxUser user){
        this.user_id = user.id;
        this.user_name = user.name;
        this.user_pic_url = user.images.get(0).url;
        this.reputation = 0;

        this.track_id = track.id;
        this.track_name = track.name;
        this.album_name = track.album.name;
        // Combine artists names
        StringJoiner joiner = new StringJoiner(", ");
        for(ArtistSimple a : track.artists){
            joiner.add(a.name);
        }
        this.track_artists = joiner.toString();
        this.album_img = track.album.images.get(0).url;
        this.duration = track.duration_ms;
    }
}
