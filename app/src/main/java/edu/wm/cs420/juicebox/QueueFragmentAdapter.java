package edu.wm.cs420.juicebox;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by alanz on 3/1/18.
 */

public class QueueFragmentAdapter extends ArrayAdapter {

    private Context context;

    public QueueFragmentAdapter(Context context, List items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView titleText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        SongListItem item = (SongListItem) getItem(position);
        View viewToUse = null;

        LayoutInflater mInflater = (LayoutInflater) context.
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            viewToUse = mInflater.inflate(R.layout.song_list_item, null);
            holder = new ViewHolder();
            holder.titleText = (TextView) viewToUse.findViewById(R.id.track_name_text);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }
        holder.titleText.setText(item.getName());
        viewToUse.setTag(holder);
        return viewToUse;
    }

}
