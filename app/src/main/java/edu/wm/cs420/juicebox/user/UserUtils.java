package edu.wm.cs420.juicebox.user;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import edu.wm.cs420.juicebox.NewPartyActivity;
import edu.wm.cs420.juicebox.SpotifyUtils;
import edu.wm.cs420.juicebox.database.DatabaseUtils;
import edu.wm.cs420.juicebox.database.models.JuiceboxParty;
import edu.wm.cs420.juicebox.database.models.JuiceboxPlaylist;
import edu.wm.cs420.juicebox.database.models.JuiceboxTrack;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.client.Response;

/**
 * A Class that store's information about the user of the application
 * Currently this class is a singleton but it doesn't necessarily have to be
 * Created by Khauri on 3/2/2018.
 */

public class UserUtils {
    private static  String TAG = "UserUtils";

    public static void joinParty(JuiceboxParty party) {
        setParty(party);
        update("party", EVENT_TYPES.UserJoinedParty, party);
    }

    public static void getCurrTrack(DatabaseUtils.DatabaseCallback<JuiceboxTrack> cb) {
        if(isInParty()){
            DatabaseUtils.peekTrack(party, cb);
        }
    }

    private enum    EVENT_TYPES {UserUpdated, EnterPartyZone, ExitPartyZone, UserPartyUpdated, UserJoinedParty, UserExitedParty, PlaylistUpdated};

    private static Location location;
    private static  JuiceboxUser user;
    private static  JuiceboxParty party;
    private static JuiceboxPlaylist playlist;
    private static GeoQuery geoQuery;

    private UserUtils(){};

    public static void setLocation(Location location) {
        UserUtils.location = location;
        if(geoQuery == null){
            geoQuery = DatabaseUtils.getGeoFire()
                    .queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.01);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Log.d(TAG, "onKeyEntered: " + key);
                    // Add key to nearby parties
                    DatabaseUtils.getParty(key, new DatabaseUtils.DatabaseCallback<JuiceboxParty>() {
                        @Override
                        public void success(JuiceboxParty result) {
                            update("party", EVENT_TYPES.EnterPartyZone, result);
                        }
                        @Override
                        public void failure() {
                        }
                    });
                }
                @Override
                public void onKeyExited(String key) {
                    // remove from nearby parties
                    DatabaseUtils.getParty(key, new DatabaseUtils.DatabaseCallback<JuiceboxParty>() {
                        @Override
                        public void success(JuiceboxParty result) {
                            update("party", EVENT_TYPES.ExitPartyZone, result);
                        }
                        @Override
                        public void failure() {
                        }
                    });
                }
                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }
                @Override
                public void onGeoQueryReady() {
                }
                @Override
                public void onGeoQueryError(DatabaseError error) {
                }
            });
        }else{
            double radius = UserUtils.isInParty() ? party.radius / 100 : 0.01;
            geoQuery.setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), radius);
        }
    }

    public static Location getLocation(){
        if(location == null){
            // Try to get the location of the user
        }
        return location;
    }

    public static boolean isHost() {
        if(user == null || party == null || TextUtils.isEmpty(user.id) || TextUtils.isEmpty(party.host_id))
            return false;
        return user.id.equals(party.host_id);
    }
    // A Value event listener for the current playlist
    private static ValueEventListener playlistUpdateSubscriber = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            JuiceboxPlaylist juiceboxPlaylist = new JuiceboxPlaylist();
            for(DataSnapshot data : dataSnapshot.child("upcoming").getChildren()){
                juiceboxPlaylist.upcoming_tracks.add(data.getValue(JuiceboxTrack.class));
            }
            update("playlist", EVENT_TYPES.PlaylistUpdated, juiceboxPlaylist);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private static ValueEventListener partyUpdateSubscriber = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dataSnapshot.getValue(JuiceboxParty.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Do something?
        }
    };

    public static void setParty(JuiceboxParty juiceboxParty) {
        if(UserUtils.party != null)
            DatabaseUtils.unwatchParty(party, partyUpdateSubscriber, playlistUpdateSubscriber);
        UserUtils.party = juiceboxParty;
        DatabaseUtils.watchParty(party, partyUpdateSubscriber, playlistUpdateSubscriber);
        // notify watchers and shit
    }

    public static void leaveParty(){
        UserUtils.party = null;
        update("party", EVENT_TYPES.UserExitedParty, null);
    }

    public interface UserUtilsCallback{
        enum ERROR {NO_USER, SPOTIFY_ERROR, NOT_IN_PARTY, DATABASE_ERROR};
        void onSuccess();
        void onFailure(ERROR E, String error);
    }


    public static void createPartyWithTrack(Context context, Track track) {
        // Create a new intent
        Intent intent = new Intent(context, NewPartyActivity.class);
        intent.putExtra("track_id", track.id);
        context.startActivity(intent);
    }

    public static void hostParty(JuiceboxParty juiceboxParty, String track_id, final UserUtilsCallback callback) {
        // Check if user was initialized
        if(user == null){
            callback.onFailure(UserUtilsCallback.ERROR.NO_USER, "User was not initialized");
            return;
        }
        juiceboxParty.host_id = user.id;
        Location l = getLocation();
        if(l != null){
            juiceboxParty.latitude = l.getLatitude();
            juiceboxParty.longitude = l.getLongitude();
        }
        // add the party to the database
        DatabaseUtils.createParty(juiceboxParty);
        setParty(juiceboxParty);
        // Add track to current party
        if(!TextUtils.isEmpty(track_id)){
            addTrackToCurrentParty(track_id, callback);
        }else{
            callback.onSuccess();
        }

    }

    public static void addTrackToCurrentParty(String track_id, final UserUtilsCallback callback) {
        if(UserUtils.party == null){
            callback.onFailure(UserUtilsCallback.ERROR.NOT_IN_PARTY, "The user is not in a party!");
            return;
        }
        SpotifyUtils.getSpotifyService().getTrack(track_id, new SpotifyCallback<Track>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                callback.onFailure(UserUtilsCallback.ERROR.SPOTIFY_ERROR, spotifyError.getMessage());
            }

            @Override
            public void success(Track track, Response response) {
                DatabaseUtils.addTrackToParty(UserUtils.party, UserUtils.user, track);
                callback.onSuccess();
            }
        });
    }



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
                update("user", EVENT_TYPES.UserUpdated, user);
            }
            @Override
            public void failure() {
                Log.d(TAG, "User Does not exist, creating user!");
                user = DatabaseUtils.createUser(userPrivate);
                update("user", EVENT_TYPES.UserUpdated, user);
            }
        });
        // Watch the user for changes
        DatabaseUtils.getDatabase()
                .child(DatabaseUtils.usersEndpoint + userPrivate.id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(JuiceboxUser.class);
                        if(user != null) {
                            user.id = dataSnapshot.getKey();
                            update("user", EVENT_TYPES.UserUpdated, user);
                        }
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
     * Joins a party and sets this user as a participant
     * @param id the id of the party
     */
    public static void joinParty(String id){

    }

    private static HashMap<String, List<DatabaseUpdateListener>> eventListeners = new HashMap<>();

    public static void addUpdateListener(String type, DatabaseUpdateListener listener) {
        String key = type.toLowerCase();
        if(!eventListeners.containsKey(key)){
            eventListeners.put(key, new ArrayList<DatabaseUpdateListener>());
        }
        eventListeners.get(key).add(listener);
    }

    private static void update(String key, EVENT_TYPES type, Object o){
        for(DatabaseUpdateListener dul : eventListeners.get(key)){
            switch(type){
                case EnterPartyZone:
                    if(dul instanceof PartyUpdateListener)
                        ((PartyUpdateListener) dul).enterPartyZone((JuiceboxParty) o);
                    break;
                case ExitPartyZone:
                    if(dul instanceof PartyUpdateListener)
                        ((PartyUpdateListener) dul).exitPartyZone((JuiceboxParty) o);
                    break;
                case UserPartyUpdated:
                    if(dul instanceof PartyUpdateListener)
                        ((PartyUpdateListener) dul).userPartyUpdated((JuiceboxParty) o);
                    break;
                case UserJoinedParty:
                    if(dul instanceof PartyUpdateListener)
                        ((PartyUpdateListener) dul).userJoinedParty((JuiceboxParty) o);
                    break;
                case UserExitedParty:
                    if(dul instanceof PartyUpdateListener)
                        ((PartyUpdateListener) dul).userExitedParty();
                    break;
                default:
                    dul.onUpdate(o);
            }
        }
    }
    // I need a base class but
    // I don't want others to use it
    private interface DatabaseUpdateListener<T>{
        void onUpdate(T data);
    }

    public interface PartyUpdateListener extends DatabaseUpdateListener<JuiceboxParty>{
        void enterPartyZone(JuiceboxParty party);
        void exitPartyZone(JuiceboxParty party);
        void userPartyUpdated(JuiceboxParty party);
        void userJoinedParty(JuiceboxParty o);
        void userExitedParty();
    };

    // Instead they have to use these pre-defined subclasses
    public interface PlaylistUpdateListener extends DatabaseUpdateListener<JuiceboxPlaylist>{}

    public interface UserUpdateListener extends DatabaseUpdateListener<JuiceboxUser>{}
}
