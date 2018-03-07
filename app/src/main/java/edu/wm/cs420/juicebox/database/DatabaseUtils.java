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
        getDatabase().child(partyEndpoint).child(id).setValue(party);
        return party;
    }

    public static void createParty(JuiceboxParty party) {
        DatabaseReference ref = getDatabase().child(partyEndpoint).push();
        String id = ref.getKey();
        party.id = id;
        ref.setValue(party);
    }

    public static void addTrackToParty(final JuiceboxParty party, final JuiceboxUser user, final Track track){
        JuiceboxTrack juiceboxTrack = new JuiceboxTrack(track, user);
        getDatabase().child(playlistEndpoint + party.playlist_id).child("upcoming").push().setValue(juiceboxTrack);
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

    /**
     * Gets a playlist given the id
     * @param id id of the playlist
     * @param callback the callback that will pass the value
     * @return
     */
    public static void getPlaylist(String id, final DatabaseCallback<JuiceboxPlaylist> callback){
        getDatabase().child(playlistEndpoint + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JuiceboxPlaylist playlist = dataSnapshot.getValue(JuiceboxPlaylist.class);
                if(playlist == null){
                    callback.failure();
                }else {
                    callback.success(playlist);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {callback.cancelled();}
        });
    }

    /**
     * Stop watching a party and its associated playlist for updates
     * @param party the party
     * @param partyUpdateSubscriber the callback for party updates
     * @param playlistUpdateSubscriber the callback for playlist updates
     */
    public static void unwatchParty(JuiceboxParty party, ValueEventListener partyUpdateSubscriber,
                                    ValueEventListener playlistUpdateSubscriber) {
        getDatabase().child(partyEndpoint + party.id).removeEventListener(partyUpdateSubscriber);
        getDatabase().child(playlistEndpoint + party.playlist_id).removeEventListener(playlistUpdateSubscriber);
    }

    /**
     * Start watching a party and its associated playlist for updates
     * @param party the party to watch
     * @param partyUpdateSubscriber the callback for party updates
     * @param playlistUpdateSubscriber the callback for playlist updates
     */
    public static void watchParty(JuiceboxParty party, ValueEventListener partyUpdateSubscriber,
                                  ValueEventListener playlistUpdateSubscriber) {
        getDatabase().child(partyEndpoint + party.id).addValueEventListener(partyUpdateSubscriber);
        getDatabase().child(playlistEndpoint + party.playlist_id).addValueEventListener(playlistUpdateSubscriber);
    }

    public static abstract class DatabaseCallback<T>{
        public abstract void success(T result);
        public abstract void failure();
        public void cancelled(){
            Log.d(TAG, "Request was cancelled");
        };
    }
}
