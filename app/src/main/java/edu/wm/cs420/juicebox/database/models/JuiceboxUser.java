package edu.wm.cs420.juicebox.database.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Users Schema
 * Created by Khauri on 2/27/2018.
 */
@IgnoreExtraProperties
public class JuiceboxUser {
    @Exclude
    public String s_id; // JuiceboxUser's spotify id

    public String name;
    public int reputation;

    public JuiceboxUser(){
        // Default constructor required for calls to DataSnapshot.getValue(JuiceboxUser.class)
    }

    public JuiceboxUser(String name, int reputation){
        this.name = name;
        this.reputation = reputation;
    }
}
