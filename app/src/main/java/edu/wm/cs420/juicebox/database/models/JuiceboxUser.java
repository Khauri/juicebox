package edu.wm.cs420.juicebox.database.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Followers;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Users Schema
 * Created by Khauri on 2/27/2018.
 */
@IgnoreExtraProperties
public class JuiceboxUser {

    @Exclude
    public String id; // JuiceboxUser's spotify id

    public String name;
    public String party_id; // id of party user is in
    public List<Image> images;
    public String href;
    public int reputation = 0;
    public int followers;

    public JuiceboxUser(){}

    public JuiceboxUser(String display_name, List<Image> images, Followers followers){
        this.name = display_name;
        this.images = images;
        this.followers = followers.total;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("reputation", reputation);
        result.put("images", images);
        result.put("followers", followers);
        return result;
    }
}
