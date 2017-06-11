package com.subhajitdas.codehub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Subhajit Das on 26-01-2017.
 */

public class CustomArrayAdapter extends ArrayAdapter<Options> {

    public CustomArrayAdapter(Context context, ArrayList<Options> options) {
        super(context, 0, options);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Options options =getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.nav_drawerlayout, parent, false);
        }
        // Lookup view for data population
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        TextView text = (TextView) convertView.findViewById(R.id.nav_text);
        // Populate the data into the template view using the data object
        icon.setImageResource(options.image);
        text.setText(options.text);
        // Return the completed view to render on screen
        return convertView;
    }
}
