package edu.wm.cs420.juicebox.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.wm.cs420.juicebox.database.models.JuiceboxParty;
import edu.wm.cs420.juicebox.database.models.JuiceboxPlaylist;
import edu.wm.cs420.juicebox.database.models.JuiceboxTrack;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

/**
 * Created by Khauri on 2/27/2018.
 */

public class DatabaseUtils {
    private static String TAG = "Juicebox-DatabaseUtils";

    private static DatabaseReference mDatabase;
    public static String usersEndpoint = "users/";
    public static String partyEndpoint = "parties/";
    public static String playlistEndpoint = "playlists/";

    public static DatabaseReference getDatabase(){
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mDatabase;
    }

    public static JuiceboxParty createParty(String hostId, String partyName, String partyDesc, String latLong, int radius, int privacy){
        String id = getDatabase().child(partyEndpoint).push().getKey();
        JuiceboxParty party = new JuiceboxParty(hostId, partyName, partyDesc, latLong, radius, privacy);
        getDatabase().child("parties").child(id).setValue(party);
        return party;
    }

    public static void addTrackToParty(final String partyId, final Track track, final DatabaseCallback<JuiceboxTrack> callback){
        getParty(partyId, new DatabaseCallback<JuiceboxParty>() {
            @Override
            public void success(JuiceboxParty result) {
                if(result == null){
                    callback.failure();
                }else{
                    // The party exists so let's add the the track to the queue
                    JuiceboxTrack juiceboxTrack = new JuiceboxTrack(track);
                    getDatabase().child(partyEndpoint + partyId).child("queue").push().setValue(track);
                    callback.success(juiceboxTrack);
                }
            }
            @Override
            public void failure() {
                Log.d(TAG, "Track could not be added. Party does not exist!");
            }
        });
    }

    public static JuiceboxUser createUser(UserPrivate userPrivate) {
        JuiceboxUser juiceboxUser = new JuiceboxUser(userPrivate.display_name, userPrivate.images, userPrivate.followers);
        // Add it to the database
        getDatabase().child(usersEndpoint + userPrivate.id).setValue(juiceboxUser);
        return juiceboxUser;
    }

    /**
     * Retrieves a party from the firebase RTD
     * @param id the id of the party
     * @param callback
     */
    public static void getParty(String id, final DatabaseCallback<JuiceboxParty> callback){
        getDatabase().child(partyEndpoint + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JuiceboxParty juiceboxParty = dataSnapshot.getValue(JuiceboxParty.class);
                if(juiceboxParty == null){
                    callback.failure();
                }else{
                    callback.success(juiceboxParty);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.cancelled();
            }
        });
    }
    /**
     * Gets a user once given their spotify id from the firebase RTD
     * @param id
     * @param callback
     * @return
     */
    public static void getUser(String id, final DatabaseCallback<JuiceboxUser> callback){
        getDatabase().child(usersEndpoint + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JuiceboxUser user = dataSnapshot.getValue(JuiceboxUser.class);
                if(user == null){
                    callback.failure();
                }else{
                    callback.success(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.cancelled();
            }
        });
    }

    public static void watchUser(String id, final DatabaseCallback<JuiceboxUser> callback){
        getDatabase().child(usersEndpoint + id).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Create a new playlist and return its id
     * @return
     */
    public static String createPlaylist() {
        JuiceboxPlaylist juiceboxPlaylist = new JuiceboxPlaylist();
        DatabaseReference ref = getDatabase().child(playlistEndpoint).push();
        juiceboxPlaylist.id = ref.getKey();
        ref.setValue(juiceboxPlaylist);
        return juiceboxPlaylist.id;
    }

    public static abstract class DatabaseCallback<T>{
        public abstract void success(T result);
        public abstract void failure();
        public void cancelled(){
            Log.d(TAG, "Request was cancelled");
        };
    }
}
