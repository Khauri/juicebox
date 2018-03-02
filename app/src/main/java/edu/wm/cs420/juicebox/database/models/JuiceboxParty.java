package edu.wm.cs420.juicebox.database.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JuiceboxParty Schema
 * Created by Khauri on 2/27/2018.
 */

@IgnoreExtraProperties
public class JuiceboxParty {
    @Exclude
    public String uid;

    public String name;
    public String host_id; // The host of the party
    public List<JuiceboxTrack> queue;
    public String location;
    public int radius;
    public String pl_id; // id of party playlist

    public JuiceboxParty(){

    }

    public JuiceboxParty(String uid, String name, String location, int radius){
        this.uid = uid;
        this.name = name;
        this.location = location;
        this.radius = radius;
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
