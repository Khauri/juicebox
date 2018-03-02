package edu.wm.cs420.juicebox.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.wm.cs420.juicebox.database.models.JuiceboxParty;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

/**
 * Created by Khauri on 2/27/2018.
 */

public class DatabaseUtils {

    private static DatabaseReference mDatabase;
    private static String usersEndpoint = "users/";
    private static String partyEndpoint = "parties/";
    private static String TAG = "JuiceboxDatabaseUtils";

    public static DatabaseReference getDatabase(){
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mDatabase;
    }

    public static JuiceboxParty createParty(){
        String id = getDatabase().child(partyEndpoint).push().getKey();
        JuiceboxParty party = new JuiceboxParty(id, "MyParty", "Here", 2);
        getDatabase().child("parties").child(id).setValue(party);
        return party;
    }

    public static void addTrackToParty(String partyId, Track track){

    }

    public static JuiceboxUser createUser(UserPrivate userPrivate) {
        JuiceboxUser juiceboxUser = new JuiceboxUser(userPrivate.display_name, userPrivate.images, userPrivate.followers);
        // Add it to the database
        getDatabase().child(usersEndpoint + userPrivate.id).setValue(juiceboxUser);
        return juiceboxUser;
    }

    public static void getParty(String id, final DatabaseCallback<JuiceboxParty> callback){
        getDatabase().child(partyEndpoint + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JuiceboxParty juiceboxParty = dataSnapshot.getValue(JuiceboxParty.class);
                callback.success(juiceboxParty);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.failure();
            }
        });
    }
    /**
     * Gets a user given their spotify id from the firebase RTD
     * @param id
     * @param callback
     * @return
     */
    public static void getUser(String id, final DatabaseCallback<JuiceboxUser> callback){
        getDatabase().child(usersEndpoint + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JuiceboxUser user = dataSnapshot.getValue(JuiceboxUser.class);
                callback.success(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.failure();
            }
        });
    }

    public static abstract class DatabaseCallback<T>{
        public abstract void success(T result);
        public abstract void failure();
    }
}
