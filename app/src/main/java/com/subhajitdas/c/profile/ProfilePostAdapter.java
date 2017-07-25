package com.subhajitdas.c.profile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.subhajitdas.c.R;
import com.subhajitdas.c.post.PostData;

import java.util.ArrayList;

/**
 * Created by Subhajit Das on 24-07-2017.
 */

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ViewHolder> {
    private ArrayList<PostData> mDataset;
    private String dpUrl = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView dp;
        Context context;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.post_title);
            desc = (TextView) v.findViewById(R.id.post_desc);
            dp = (ImageView) v.findViewById(R.id.post_dp);
            context = v.getContext();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ProfilePostAdapter(ArrayList<PostData> myDataset) {
        mDataset = myDataset;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ProfilePostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        //A new view is created.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_post_layout, parent, false);

        //Returning the new created view.
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.dp.setImageDrawable(holder.context.getResources().getDrawable(R.drawable.ic_avatar_black));
        if (dpUrl != null) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(holder.context.getResources().getDrawable(R.drawable.ic_avatar_black));
            requestOptions.circleCrop();

            Glide.with(holder.context)
                    .load(dpUrl)
                    .apply(requestOptions)
                    .into(holder.dp);
        }

        holder.title.setText(mDataset.get(position).data.title);

        if (mDataset.get(position).data.description != null
                &&
                !mDataset.get(position).data.description.isEmpty()) {

            holder.desc.setText(mDataset.get(position).data.description);
        }
        else{
            holder.desc.setText("No Description");
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setDpUrl(String url) {
        dpUrl = url;
    }
}

