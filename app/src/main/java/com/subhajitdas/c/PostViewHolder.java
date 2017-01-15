package com.subhajitdas.c;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Subhajit Das on 10-01-2017.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

    View mView;
    ImageButton mLikeButton;

    private DatabaseReference mLikeRef;

    public PostViewHolder(View itemView) {
        super(itemView);
        mView=itemView;
        mLikeButton= (ImageButton) itemView.findViewById(R.id.like_button);

        mLikeRef=FirebaseDatabase.getInstance().getReference().child("Like");
        mLikeRef.keepSynced(true);
    }

    public void setPostTitle(String title)
    {
        TextView postTitle = (TextView)mView.findViewById(R.id.post_title);
        postTitle.setText(title);
    }

    public void setPostPoster(String poster)
    {
        TextView postPoster = (TextView)mView.findViewById(R.id.poster_name);
        postPoster.setText(poster);
    }

    public void setPostDate(String date)
    {
        TextView postDate = (TextView)mView.findViewById(R.id.post_date);
        postDate.setText(date);
    }

    public void setPostLike(String date)
    {
        TextView postLike = (TextView)mView.findViewById(R.id.post_like);
        postLike.setText(date);
    }

    public void setLikeButton(String postKey, final String userId)
    {
        mLikeRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userId))
                    mLikeButton.setImageResource(R.drawable.ic_thumb_up_yellow_48px);
                else
                    mLikeButton.setImageResource(R.drawable.ic_thumb_up_gray_48px);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
