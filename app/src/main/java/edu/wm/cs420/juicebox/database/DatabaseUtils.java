package edu.wm.cs420.juicebox.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.wm.cs420.juicebox.database.models.JuiceboxParty;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;

/**
 * Created by Khauri on 2/27/2018.
 */

public class DatabaseUtils {

    private static DatabaseReference mDatabase;

    public static DatabaseReference getDatabase(){
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mDatabase;
    }

    public static JuiceboxUser createUser(String spotifyID){
        JuiceboxUser juiceboxUser = new JuiceboxUser();
        return juiceboxUser;
    }

    public static JuiceboxParty createParty(){
        String id = getDatabase().child("parties").push().getKey();
        JuiceboxParty party = new JuiceboxParty(id, "MyParty", "Here", 2);
        getDatabase().child("parties").child(id).setValue(party);
        return party;
    }
}
