package edu.wm.cs420.juicebox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by alanz on 3/2/18.
 */

public class SearchFragmentAdapter extends RecyclerView.Adapter<SearchFragmentAdapter.ViewHolder> {
    private List<Track> results;
    private static Context context;

    public void setResults(List<Track> results) {
        this.results = results;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView track_name;
        private TextView track_artists;
        private TextView track_duration;
        private ImageView track_album_img;
        public ViewHolder(View itemView) {
            super(itemView);
            track_name = itemView.findViewById(R.id.track_name_text);
            track_artists = itemView.findViewById(R.id.track_artists_text);
            track_duration = itemView.findViewById(R.id.track_duration_text);
            track_album_img = itemView.findViewById(R.id.track_album_img);
        }

        public void bindData(final Track track) {
            track_name.setText(track.name);
            // Combine the artists
            final StringJoiner joiner = new StringJoiner(", ");
            track.artists.forEach(new Consumer<ArtistSimple>() {
                @Override
                public void accept(ArtistSimple artistSimple){
                    joiner.add(artistSimple.name);
                }
            });
            track_artists.setText(joiner.toString());
            track_duration.setText(DateUtils.formatElapsedTime(track.duration_ms / 1000));
            if(track.album.images.size() > 0)
                Picasso.with(context).load(track.album.images.get(0).url).into(track_album_img);
        }
    }

    public SearchFragmentAdapter(){
        this.results = new ArrayList<>();
    }

    public SearchFragmentAdapter(List<Track> tracks){
        this.results = tracks;
    }

    @Override
    public SearchFragmentAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        context = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(results.get(position));
    }


    @Override
    public int getItemCount() {
        return results == null ? 0 : results.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.song_list_item;
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
