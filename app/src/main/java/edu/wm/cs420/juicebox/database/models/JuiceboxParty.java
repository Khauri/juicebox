package edu.wm.cs420.juicebox.database.models;

import android.icu.util.Calendar;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wm.cs420.juicebox.database.DatabaseUtils;

/**
 * JuiceboxParty Schema
 * Created by Khauri on 2/27/2018.
 */

@IgnoreExtraProperties
public class JuiceboxParty {
    @Exclude
    public String id;                   // The id of this party (haven't figured out how to save this yet)

    public String host_id;              // The host of the party's id
    public String playlist_id;          // The party queue

    public String name;                 // The name of the party
    public String description;          // A Short description of the party
    public String location;             // The location of the party
    public String state;                // active|paused|atopped
    public int radius;                  // The radius around the location
    public int privacy;                 // The privacy setting (0=friendly,1=invite,2=public)
    public long created_at;             // The precise time in ms this party was created at
    public List<String> participants;   // A list of current participants

    public JuiceboxParty(){}

    public JuiceboxParty(String hostId, String partyName, String partyDesc, String latLong, int radius, int privacy){
        this.host_id = hostId;
        this.name = partyName;
        this.description = partyDesc;
        this.location = latLong;
        this.radius = radius;
        this.privacy = privacy;
        this.created_at = Calendar.getInstance().getTimeInMillis();
        this.state = "paused";
        this.playlist_id = DatabaseUtils.createPlaylist();
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("location", location);
        result.put("radius", radius);
        return result;
    }
}
