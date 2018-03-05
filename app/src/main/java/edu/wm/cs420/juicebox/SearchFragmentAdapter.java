package edu.wm.cs420.juicebox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import edu.wm.cs420.juicebox.database.models.JuiceboxTrack;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by alanz on 3/2/18.
 */

public class SearchFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = "Juicebox-ListAdapter";

    public enum ViewType {SEARCH, BIG_QUEUE};

    // Important instance data
    private List<?> data;           // This class isn't concerned with the list type
    private ViewType viewType;      // The type of view for this adapter

    // TODO: Fix small memory leak?
    private static Context context;

    public void setData(List<?> data) {
        this.data = data;
    }

    /**
     * A base class viewholder implementation so that this adapter can be re-used
     * a little easier given its bindData method.
     * @param <T>
     */
    public static abstract class myViewHolder<T> extends RecyclerView.ViewHolder{
        public myViewHolder(View itemView){super(itemView);}
        public abstract void bindData(T data);
    }

    /**
     * A Viewholder for the search items
     */
    public static class SearchItemHolder extends myViewHolder<Track>{
        private TextView track_name;
        private TextView track_artists;
        private TextView track_duration;
        private ImageView track_album_img;

        public SearchItemHolder(View itemView) {
            super(itemView);
            track_name = itemView.findViewById(R.id.track_name_text);
            track_artists = itemView.findViewById(R.id.track_artists_text);
            track_duration = itemView.findViewById(R.id.track_duration_text);
            track_album_img = itemView.findViewById(R.id.track_album_img);
        }

        @Override
        public void bindData(final Track track) {
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
        // TODO: Depending on how the layout pans out this info might have to be exported somehow
        // Track info
        private TextView    track_name;
        private TextView    track_artists;
        private TextView    track_duration;
        private ImageView   track_album_img;
        // Meta data
        private TextView    reputation_count;
        private TextView    requester_name;
        // Controls(?)
        private Button      upvote_btn;
        private Button      downvote_btn;
        public QueueItemHolder(View itemView) {
            super(itemView);
            track_name = itemView.findViewById(R.id.track_name_text);
            track_artists = itemView.findViewById(R.id.track_artists_text);
            track_duration = itemView.findViewById(R.id.track_duration_text);
            track_album_img = itemView.findViewById(R.id.track_album_img);
        }

        public void bindData(final JuiceboxTrack track) {
//            track_name.setText(track.name);
//            // Combine the artists
//            final StringJoiner joiner = new StringJoiner(", ");
//            track.artists.forEach(new Consumer<ArtistSimple>() {
//                @Override
//                public void accept(ArtistSimple artistSimple){
//                    joiner.add(artistSimple.name);
//                }
//            });
//            track_artists.setText(joiner.toString());
//            track_duration.setText(DateUtils.formatElapsedTime(track.duration_ms / 1000));
//            if(track.album.images.size() > 0)
//                Picasso.with(context).load(track.album.images.get(0).url).into(track_album_img);
        }
    }

    public SearchFragmentAdapter(ViewType viewType){
        this.viewType = viewType;
        this.data = new ArrayList<>();
    }

    public SearchFragmentAdapter(ViewType viewType, List<Object> tracks){
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
            default:
                return 0;
        }
    }

//    public class ViewHolder {
//        TextView tv;
//        Button btn;
//    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent){
//        LayoutInflater mInflater = (LayoutInflater) context.
//                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//        ViewHolder holder;
//        if (convertView == null){
//           convertView = mInflater.inflate(R.layout.search_list_item, null);
//           holder = new ViewHolder();
//           holder.tv = (TextView) convertView.findViewById(R.id.song_name);
//           holder.btn = (Button) convertView.findViewById(R.id.add_to_queue_button);
//           convertView.setTag(holder);
//        }
//        else{
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        final Track currentTrack = trackList.get(position);
//        holder.tv.setText(currentTrack.name);
//        holder.btn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                SongListItem itemToAdd = new SongListItem(currentTrack.name);
//                fragment.getListener().onAddToQueue(itemToAdd);
//            }
//        });
//        return convertView;
//    }
}
