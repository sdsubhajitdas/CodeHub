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
    ImageButton mBookmarkButton;
    TextView mLikeText;


    private DatabaseReference mLikeRef;
    private DatabaseReference mBookmarkRef;
    private DatabaseReference mProgRef;

    public PostViewHolder(View itemView) {
        super(itemView);
        mView=itemView;
        mLikeButton= (ImageButton) itemView.findViewById(R.id.like_button);
        mBookmarkButton=(ImageButton)itemView.findViewById(R.id.bookmark_button);
        mLikeText=(TextView)itemView.findViewById(R.id.post_like) ;

        mLikeRef=FirebaseDatabase.getInstance().getReference().child("like");
        mBookmarkRef =FirebaseDatabase.getInstance().getReference().child("bookmark");
        mProgRef =FirebaseDatabase.getInstance().getReference().child("program");
        mLikeRef.keepSynced(true);
        mBookmarkRef.keepSynced(true);
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
        mLikeRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void setBookmarkButton(String postKey, final String userId)
    {
            mBookmarkRef.child(postKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(userId))
                        mBookmarkButton.setImageResource(R.drawable.ic_bookmark_black_24px);
                    else
                        mBookmarkButton.setImageResource(R.drawable.ic_bookmark_border_black_24px);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    public void setBookmarkTitle(final String postKey) {
        mProgRef.child(postKey).child("title").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if(dataSnapshot.hasChild("title")) {
                    TextView postTitle = (TextView) mView.findViewById(R.id.post_title);
                    postTitle.setText(dataSnapshot.getValue().toString());
                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setBookmarkPoster(String postKey) {
        mProgRef.child(postKey).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               // if(dataSnapshot.hasChild("userName")) {
                    TextView postPoster = (TextView) mView.findViewById(R.id.poster_name);
                    postPoster.setText(dataSnapshot.getValue().toString());
              //  }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setBookmarkDate(String postKey) {
        mProgRef.child(postKey).child("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               // if(dataSnapshot.hasChild("date")) {
                    TextView postDate = (TextView) mView.findViewById(R.id.post_date);
                    postDate.setText(dataSnapshot.getValue().toString());
              //  }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
