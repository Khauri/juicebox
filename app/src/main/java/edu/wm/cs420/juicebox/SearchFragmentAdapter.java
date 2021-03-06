package edu.wm.cs420.juicebox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import edu.wm.cs420.juicebox.database.DatabaseUtils;
import edu.wm.cs420.juicebox.database.models.JuiceboxParty;
import edu.wm.cs420.juicebox.database.models.JuiceboxTrack;
import edu.wm.cs420.juicebox.database.models.JuiceboxUser;
import edu.wm.cs420.juicebox.user.UserUtils;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by alanz on 3/2/18.
 */

public class SearchFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = "Juicebox-ListAdapter";

    public enum ViewType {SEARCH, PARTY_LIST, BIG_QUEUE};

    // Important instance data
    private List<?> data;           // This class isn't concerned with the list type
    private ViewType viewType;      // The type of view for this adapter

    // TODO: Fix small memory leak?
    private static Context context;

    public void setData(List<?> data) {
        this.data = data;
    }
    public List<?> getData(){ return this.data; }

    /**
     * A base class viewholder implementation so that this adapter can be re-used
     * a little easier given its bindData method.
     * TODO: Something to consider is data binding, but it's a little too late for that
     * @param <T>
     */
    public static abstract class myViewHolder<T> extends RecyclerView.ViewHolder{
        public myViewHolder(View itemView){super(itemView);}
        public abstract void bindData(T data);
    }

    public static class NearbyPartiesHolder extends myViewHolder<JuiceboxParty>{
        private JuiceboxParty party;
        private View join_party_btn;
        private TextView party_name;
        private TextView party_description;
        private TextView host_name;
        private ImageView host_image;

        public NearbyPartiesHolder(View itemView) {
            super(itemView);
            party_name = itemView.findViewById(R.id.party_name_text);
            party_description = itemView.findViewById(R.id.party_desc_text);
            host_name = itemView.findViewById(R.id.party_host_name_text);
            host_image = itemView.findViewById(R.id.party_host_img);
            join_party_btn = itemView.findViewById(R.id.join_party_btn);
        }

        @Override
        public void bindData(final JuiceboxParty party) {
            party_name.setText(TextUtils.isEmpty(party.name) ? "Epic Party!" : party.name);
            party_description.setText(TextUtils.isEmpty(party.description) ? "WOW PARTY" : party.description);
            DatabaseUtils.getUser(party.host_id, new DatabaseUtils.DatabaseCallback<JuiceboxUser>() {
                @Override
                public void success(JuiceboxUser result) {
                    Log.d(TAG, "success: GET THE RESULT");
                    host_name.setText(TextUtils.isEmpty(result.name) ? "Party Pete" : result.name);
                    if(result.images != null && result.images.size() > 0)
                        Picasso.with(context).load(result.images.get(0).url).into(host_image);
                }

                @Override
                public void failure() {
                    // cry
                }
            });
            join_party_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserUtils.joinParty(party);
                }
            });
        }
    }

    /**
     * A Viewholder for the search items
     */
    public static class SearchItemHolder extends myViewHolder<Track>{
        private Track       track;
        private View        view;
        private TextView    track_name;
        private TextView    track_artists;
        private TextView    track_duration;
        private ImageView   track_album_img;
        private ImageButton context_menu_btn;

        private void addTrack(Context context, Track track){
            // TODO: Open a dialogue menu with options.
            // add the track to user's current party
            if(UserUtils.isInParty()) {
                // Simply adds this track to the party
                UserUtils.addTrackToCurrentParty(track.id, new UserUtils.UserUtilsCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: Added track!");
                    }

                    @Override
                    public void onFailure(ERROR E, String error) {
                    }
                });
            } else {
                // Opens up a new intent allowing the creation of a party
                UserUtils.createPartyWithTrack(context, track);
            }
        }

        public SearchItemHolder(final View itemView) {
            super(itemView);
            track_name = itemView.findViewById(R.id.track_name_text);
            track_artists = itemView.findViewById(R.id.track_artists_text);
            track_duration = itemView.findViewById(R.id.track_duration_text);
            track_album_img = itemView.findViewById(R.id.track_album_img);
            context_menu_btn = itemView.findViewById(R.id.context_menu_btn);
            // TODO: Perform some action (potentially the same actions) on these events
            // Long press on the view itself
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.d(TAG, "searchItemHolder/onLongClick");
                    // TODO: Open a dialogue menu with options
                    addTrack(itemView.getContext(), track);
                    return true;
                }
            });
            // press the context menu button
            context_menu_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "searchItemHolder/onClick");
                    addTrack(itemView.getContext(), track);
                }
            });
        }

        @Override
        public void bindData(final Track track) {
            this.track = track;
            track_name.setText(track.name);
            // Combine the artists into comma separated string
            final StringJoiner joiner = new StringJoiner(", ");
            track.artists.forEach(new Consumer<ArtistSimple>() {
                @Override
                public void accept(ArtistSimple artistSimple){
                    joiner.add(artistSimple.name);
                }
            });
            track_artists.setText(joiner.toString());
            // Format from ms to MM:SS
            track_duration.setText(DateUtils.formatElapsedTime(track.duration_ms / 1000));
            // Load album art
            // This is a pretty lazy method as it just gets the first image
            if(track.album.images.size() > 0)
                Picasso.with(context).load(track.album.images.get(0).url).into(track_album_img);
        }
    }

    /**
     * A Viewholder for the queue items
     */
    public static class QueueItemHolder extends myViewHolder<JuiceboxTrack>{
        // User info
        private ImageView   user_img;
        private TextView    requester_name;
        // Track info
        private TextView    track_name;
        private TextView    track_artists;
        private TextView    track_duration;
        private ImageView   track_album_img;
        // Meta data
        private TextView    reputation_count;
        // Controls(?)
        private ImageButton      upvote_btn;
        private ImageButton      downvote_btn;
        public QueueItemHolder(View itemView) {
            super(itemView);
            track_name = itemView.findViewById(R.id.track_name_text);
            track_artists = itemView.findViewById(R.id.track_artists_text);
            track_duration = itemView.findViewById(R.id.track_duration_text);
            track_album_img = itemView.findViewById(R.id.track_album_img);;
            requester_name = itemView.findViewById(R.id.track_requester_text);
            reputation_count = itemView.findViewById(R.id.track_rep_count);
            user_img = itemView.findViewById(R.id.user_profile_pic);
            upvote_btn = itemView.findViewById(R.id.upvote_btn);
            downvote_btn = itemView.findViewById(R.id.downvote_btn);
        }

        public void bindData(final JuiceboxTrack track) {
            track_name.setText(track.track_name);
            track_artists.setText(track.track_artists);
            track_duration.setText(DateUtils.formatElapsedTime(track.duration / 1000));
            requester_name.setText(track.user_name);
            reputation_count.setText(NumberFormat.getInstance().format(track.reputation));
            // load user profile pic
            Picasso.with(context).load(track.user_pic_url).into(user_img);
            // load album img
            Picasso.with(context).load(track.album_img).into(track_album_img);
            // TODO
//            upvote_btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    upvote_btn.setChecked(true);
//                    downvote_btn.setChecked(false);
//                }
//            });
//            downvote_btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    upvote_btn.setChecked(false);
//                    downvote_btn.setChecked(true);
//                }
//            });
        }
    }

    public SearchFragmentAdapter(ViewType viewType){
        this.viewType = viewType;
        this.data = new ArrayList<>();
    }

    public SearchFragmentAdapter(ViewType viewType, List<?> tracks){
        this.viewType = viewType;
        this.data = tracks;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        context = view.getContext();    // This is slightly problematic
        switch(viewType){
            case R.layout.song_list_item:
                return new SearchItemHolder(view);
            case R.layout.queue_list_item:
                return new QueueItemHolder(view);
            case R.layout.nearby_party_list_item:
                return new NearbyPartiesHolder(view);
            default:
                Log.e(TAG, "onCreateViewHolder: unrecognized or unsupported viewType");
                return null;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // This is an unavoidable,un-inspected cast and unchecked call
        // do not remove the following line unless you like lint warnings
        //noinspection unchecked
        ((myViewHolder) holder).bindData(data.get(position));
        setFadeAnimation(holder.itemView);
    }


    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(final int position) {
        switch(viewType){
            case SEARCH:
                return R.layout.song_list_item;
            case BIG_QUEUE:
                return R.layout.queue_list_item;
            case PARTY_LIST:
                return R.layout.nearby_party_list_item;
            default:
                return 0;
        }
    }

    private void addTrack(){

    }

    public void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }
}
