package com.subhajitdas.c.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

import com.subhajitdas.c.editProfile.ProfileEdit;
import com.subhajitdas.c.post.PostActivity;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mCoverImage, mDpImage;
    private FloatingActionButton mMultiFab;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mName, mBio, mLocation, mWork, mEducation, mSkills;

    private DatabaseReference mUserDataRef;

    private String mLastActivity, mProfileId, mFabState;
    private int REUEST_CODE = 4321;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent gotIntent = getIntent();
        mLastActivity = gotIntent.getStringExtra(Constants.ACTIVITY);
        mProfileId = gotIntent.getStringExtra(Constants.USERID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mCoverImage = (ImageView) findViewById(R.id.edit_profile_cover);
        mDpImage = (ImageView) findViewById(R.id.edit_profile_dp);
        mMultiFab = (FloatingActionButton) findViewById(R.id.profile_fab);
        mSwipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.profile_swiperefresh);
        mName= (TextView) findViewById(R.id.edit_profile_name);
        mBio = (TextView) findViewById(R.id.edit_profile_bio);
        mLocation = (TextView) findViewById(R.id.edit_profile_location);
        mWork= (TextView) findViewById(R.id.edit_profile_work);
        mEducation= (TextView) findViewById(R.id.edit_profile_education);
        mSkills = (TextView) findViewById(R.id.profile_skills);

        if (mProfileId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            mFabState = Constants.FAB_EDIT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp, this.getTheme()));
            } else {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp));
            }
        }

        mUserDataRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER).child(mProfileId);

        mSwipeRefreshLayout.setRefreshing(true);
        mUserDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Constants.DP_URL)) {
                    String dpUrl = dataSnapshot.child(Constants.DP_URL).getValue().toString();
                    RequestOptions profileOptions = new RequestOptions();
                    profileOptions.circleCrop();
                    profileOptions.placeholder(R.drawable.ic_avatar_black);
                    Glide.with(getApplicationContext())
                            .load(dpUrl)
                            .apply(profileOptions)
                            .into(mDpImage);
                }
                if (dataSnapshot.hasChild(Constants.COVER_URL)) {
                    String coverUrl = dataSnapshot.child(Constants.COVER_URL).getValue().toString();
                    RequestOptions coverOptions = new RequestOptions();
                    coverOptions.fitCenter();
                    Glide.with(getApplicationContext())
                            .load(coverUrl)
                            .into(mCoverImage);
                }
                if (dataSnapshot.hasChild(Constants.USERNAME_PROFILE)) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    mName.setText(user.getDisplayName());
                }
                if (dataSnapshot.hasChild(Constants.BIO)) {
                    mBio.setText(dataSnapshot.child(Constants.BIO).getValue().toString());
                }
                if (dataSnapshot.hasChild(Constants.LOCATION)) {
                    mLocation.setText(dataSnapshot.child(Constants.LOCATION).getValue().toString());
                }
                if (dataSnapshot.hasChild(Constants.WORK)) {
                    mWork.setText(dataSnapshot.child(Constants.WORK).getValue().toString());
                }
                if (dataSnapshot.hasChild(Constants.EDUCATION)) {
                    mEducation.setText(dataSnapshot.child(Constants.EDUCATION).getValue().toString());
                }
                if (dataSnapshot.hasChild(Constants.SKILLS)) {
                    mSkills.setText(dataSnapshot.child(Constants.SKILLS).getValue().toString());
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mSwipeRefreshLayout.setEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        mMultiFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabState.equals(Constants.FAB_EDIT)) {
                    Intent editProfile =  new Intent(ProfileActivity.this, ProfileEdit.class);
                    editProfile.putExtra(Constants.ACTIVITY,mLastActivity);
                    editProfile.putExtra(Constants.USERID,mProfileId);
                    startActivityForResult(editProfile,REUEST_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REUEST_CODE && resultCode == RESULT_OK){
            mLastActivity = data.getStringExtra(Constants.ACTIVITY);
            mProfileId = data.getStringExtra(Constants.USERID);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent backIntent = new Intent();
                backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (mLastActivity.equals(Constants.POST_ACTIVITY)) {
                    backIntent.setClass(this, PostActivity.class);
                    startActivity(backIntent);
                }
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backIntent = new Intent();
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (mLastActivity.equals(Constants.POST_ACTIVITY)) {
            backIntent.setClass(this, PostActivity.class);
            startActivity(backIntent);
        }
    }
}
