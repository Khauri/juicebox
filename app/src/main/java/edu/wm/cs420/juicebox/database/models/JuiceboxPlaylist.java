package edu.wm.cs420.juicebox.database.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Khauri on 3/4/2018.
 */
@IgnoreExtraProperties
public class JuiceboxPlaylist {
    @Exclude
    public String id;

    public List<JuiceboxTrack> upcoming_tracks;   // The current queue
    public List<JuiceboxTrack> finished_tracks;   // Songs that have already been played

    public int num_queued = 0;

    public JuiceboxPlaylist(){
        upcoming_tracks = new ArrayList<>();
        finished_tracks = new ArrayList<>();
    }

    // Create a playlist using a list of tracks
    public JuiceboxPlaylist(List<JuiceboxTrack> tracks){}
}
