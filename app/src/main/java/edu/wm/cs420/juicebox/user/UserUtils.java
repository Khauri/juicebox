package edu.wm.cs420.juicebox.user;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import edu.wm.cs420.juicebox.database.DatabaseUtils;
import edu.wm.cs420.juicebox.database.models.JuiceboxParty;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

/**
 * A Class that store's information about the user of the application
 * Currently this class is a singleton but it doesn't necessarily have to be
 * Created by Khauri on 3/2/2018.
 */

public class UserUtils {
    private static  String TAG = "UserUtils";

    private enum    EVENT_TYPES {UserUpdated};
    private static  JuiceboxUser user;
    private static  JuiceboxParty party;

    private static List<UserUpdateListener> listeners = new ArrayList<>();

    public static void initializeUser(final UserPrivate userPrivate){
        // Create the user if none exists
        DatabaseUtils.getUser(userPrivate.id, new DatabaseUtils.DatabaseCallback<JuiceboxUser>(){
            @Override
            public void success(JuiceboxUser result) {
                // Update the user's shit
                Log.d(TAG, "Found the user! Updating information!");
                // result = DatabaseUtils.updateUser(userPrivate);
                user = result;
                // store the user for use in the application
                update(EVENT_TYPES.UserUpdated);
            }
            @Override
            public void failure() {
                Log.d(TAG, "User Does not exist, creating user!");
                user = DatabaseUtils.createUser(userPrivate);
                update(EVENT_TYPES.UserUpdated);
            }
        });
        // Watch the user for changes
        DatabaseUtils.getDatabase()
                .child(DatabaseUtils.usersEndpoint + userPrivate.id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(JuiceboxUser.class);
                        update(EVENT_TYPES.UserUpdated);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Do nothing I guess
                    }
                });
    }

    public static JuiceboxUser getUser(){
        return user;
    }

    /**
     * Gets the current party that user is in
     * @return JuiceboxParty or null if not in a party
     */
    public static JuiceboxParty getParty(){
        return party;
    }

    /**
     * Determines if the current user is in a party
     * @return
     */
    public static boolean isInParty(){
        return party != null;
    }

    /**
     * Creates a party and sets this user as the host
     * If already in a party then this will do nothing (for now)
     * @param partyName
     * @param partyDesc
     * @param latLong
     * @param radius
     * @param privacy
     */
    public static void hostParty(String partyName, String partyDesc, String latLong, int radius, int privacy){
        if(user == null){
            // welp
            Log.d(TAG, "hostParty: User Not initialized");
            return;
        }
        Log.d(TAG, "hostParty: Creating Party");
        party = DatabaseUtils.createParty(user.id, partyName, partyDesc, latLong, radius, privacy);
        Log.d(TAG, "hostParty: Adding User to the party");
//        DatabaseUtils.watchParty(party.id);
    }

    /**
     * Joins a party and sets this user as a participant
     * @param id the id of the party
     */
    public static void joinParty(String id){

    }

    /**
     * Tries to add a track ot the queue of current party
     * @param track
     */
    public static void requestTrack(Track track){

    }

    public static void addUpdateListener(UserUpdateListener listener) {
        listeners.add(listener);
    }

    private static void update(final EVENT_TYPES type){
        listeners.forEach(new Consumer<UserUpdateListener>() {
            @Override
            public void accept(UserUpdateListener userUpdateListener) {
                switch(type){
                    case UserUpdated:
                        userUpdateListener.userUpdated(user);
                        break;
                    default:
                        // Not implemented yet
                        break;
                }
            }
        });
    }
}
