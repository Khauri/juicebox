package edu.wm.cs420.juicebox;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by alanz on 3/2/18.
 */

public class SearchFragmentAdapter extends ArrayAdapter {

    private Context context;
    private List<Track> trackList;

    public SearchFragmentAdapter(Context context, List<Track> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        trackList = items;
    }

    public class ViewHolder {
        TextView tv;
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
           convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Track currentTrack = trackList.get(position);
        holder.tv.setText(currentTrack.name);
        return convertView;
    }
}
