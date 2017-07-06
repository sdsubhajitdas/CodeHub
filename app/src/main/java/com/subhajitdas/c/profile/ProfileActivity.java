package com.subhajitdas.c.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

import com.subhajitdas.c.editProfile.ProfileEdit;
import com.subhajitdas.c.post.PostActivity;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mCoverImage, mProfileImage;
    private FloatingActionButton mMultiFab;

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
        mProfileImage = (ImageView) findViewById(R.id.edit_profile_dp);
        mMultiFab = (FloatingActionButton) findViewById(R.id.profile_fab);

        if (mProfileId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            mFabState = Constants.FAB_EDIT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp, this.getTheme()));
            } else {
                mMultiFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp));
            }
        }
        /*
        RequestOptions profileOptions = new RequestOptions();
        profileOptions.circleCrop();
        profileOptions.placeholder(R.drawable.ic_avatar_black);

        Glide.with(this)
                .load("https://firebasestorage.googleapis.com/v0/b/codehub-7e17a.appspot.com/o/%252Ftestobj%252F%2Fprofile1.jpg?alt=media&token=a3ff591e-1f2f-46d2-b883-0f128a615df6")
                .apply(profileOptions)
                .into(mProfileImage);

        RequestOptions coverOptions = new RequestOptions();
        coverOptions.centerCrop();
        coverOptions.placeholder(R.drawable.navigation_drawer_image);

        Glide.with(this)
                .load("https://firebasestorage.googleapis.com/v0/b/codehub-7e17a.appspot.com/o/%252Ftestobj%252F%2Fcover.jpg?alt=media&token=2f73d4a0-d0ad-4d21-9070-25bb3eaffcc1")
                .apply(coverOptions)
                .into(mCoverImage);
        */
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
