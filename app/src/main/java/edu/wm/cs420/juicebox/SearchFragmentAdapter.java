package edu.wm.cs420.juicebox;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by alanz on 3/2/18.
 */

public class SearchFragmentAdapter extends ArrayAdapter {

    private Context context;
    private List<Track> trackList;
    private SearchFragment fragment;

    public SearchFragmentAdapter(Context context, List<Track> items, SearchFragment frag) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        trackList = items;
        fragment = frag;
    }

    public class ViewHolder {
        TextView tv;
        Button btn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater mInflater = (LayoutInflater) context.
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;
        if (convertView == null){
           convertView = mInflater.inflate(R.layout.search_list_item, null);
           holder = new ViewHolder();
           holder.tv = (TextView) convertView.findViewById(R.id.song_name);
           holder.btn = (Button) convertView.findViewById(R.id.add_to_queue_button);
           convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        final Track currentTrack = trackList.get(position);
        holder.tv.setText(currentTrack.name);
        holder.btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SongListItem itemToAdd = new SongListItem(currentTrack.name);
                fragment.getListener().onAddToQueue(itemToAdd);
            }
        });
        return convertView;
    }
}
