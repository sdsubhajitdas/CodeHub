package com.subhajitdas.c.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private DatabaseReference mFollowDataRef;
    private FirebaseUser mCurrentUser;

    private boolean isFollowing =false;
    private String mLastActivity, mProfileId, mFabState;
    private int REQUEST_CODE = 4321;

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
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.profile_swiperefresh);
        mName = (TextView) findViewById(R.id.edit_profile_name);
        mBio = (TextView) findViewById(R.id.edit_profile_bio);
        mLocation = (TextView) findViewById(R.id.edit_profile_location);
        mWork = (TextView) findViewById(R.id.edit_profile_work);
        mEducation = (TextView) findViewById(R.id.edit_profile_education);
        mSkills = (TextView) findViewById(R.id.profile_skills);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        setMultiFab();


        mFollowDataRef = FirebaseDatabase.getInstance().getReference().child(Constants.FOLLOW).child(mCurrentUser.getUid());
        mFollowDataRef.keepSynced(true);

        DatabaseReference mUserDataRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER).child(mProfileId);

        mSwipeRefreshLayout.setRefreshing(true);
        mUserDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    mName.setText(dataSnapshot.child(Constants.USERNAME_PROFILE).getValue().toString());
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

        mFollowDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mProfileId)){
                    isFollowing = true;
                    setMultiFab();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setMultiFab() {

        if (mProfileId.equals(mCurrentUser.getUid())) {
            mFabState = Constants.FAB_EDIT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp, this.getTheme()));
            } else {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp));
            }
        }
        else if (!isFollowing) {
            mFabState = Constants.FAB_FOLLOW;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_add_black_24dp, this.getTheme()));
            } else {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
            }
        }
        else if(isFollowing){
            mFabState = Constants.FAB_FOLLOWING;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp, this.getTheme()));
            } else {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp));
            }
        }
        else {
            mFabState = Constants.FAB_NONE;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        mMultiFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabState.equals(Constants.FAB_EDIT)) {
                    Intent editProfile = new Intent(ProfileActivity.this, ProfileEdit.class);
                    editProfile.putExtra(Constants.ACTIVITY, mLastActivity);
                    editProfile.putExtra(Constants.USERID, mProfileId);
                    startActivityForResult(editProfile, REQUEST_CODE);
                }
                else if(mFabState.equals(Constants.FAB_FOLLOW)){
                    mFollowDataRef.child(mProfileId).setValue(true);
                    isFollowing =true;
                    setMultiFab();
                }
                else if(mFabState.equals(Constants.FAB_FOLLOWING)){
                    mFollowDataRef.child(mProfileId).removeValue();
                    isFollowing =false;
                    setMultiFab();
                }
                else if (mFabState.equals(Constants.FAB_NONE)) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        Snackbar.make(findViewById(R.id.profile_coo),
                                "Please wait.",
                                Snackbar.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(ProfileActivity.this, "Please wait.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
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
