package com.subhajitdas.c.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;
import com.subhajitdas.c.editProfile.ProfileEdit;
import com.subhajitdas.c.post.PostActivity;
import com.subhajitdas.c.post.PostData;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mCoverImage, mDpImage;
    private FloatingActionButton mEditFab;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mName, mBio, mLocation, mWork, mEducation, mSkills, mEmptyText;
    private RecyclerView mPostRecyclerView;
    private ProfilePostAdapter mAdapter;


    private DatabaseReference mUserDataRef, mProgramRef;
    private ChildEventListener mProgramDataListener;
    private FirebaseUser mCurrentUser;
    private ValueEventListener mRefreshData;

    private ArrayList<PostData> mDataSet;
    private String mLastActivity, mProfileId;
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
        mEditFab = (FloatingActionButton) findViewById(R.id.profile_fab);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.profile_swiperefresh);
        mName = (TextView) findViewById(R.id.edit_profile_name);
        mBio = (TextView) findViewById(R.id.edit_profile_bio);
        mLocation = (TextView) findViewById(R.id.edit_profile_location);
        mWork = (TextView) findViewById(R.id.edit_profile_work);
        mEducation = (TextView) findViewById(R.id.edit_profile_education);
        mSkills = (TextView) findViewById(R.id.profile_skills);
        mEmptyText = (TextView) findViewById(R.id.no_post_label);
        mPostRecyclerView = (RecyclerView) findViewById(R.id.profile_posts_view);
        mDataSet = new ArrayList<>();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mPostRecyclerView.setLayoutManager(linearLayoutManager);
        mPostRecyclerView.setNestedScrollingEnabled(false);
        mPostRecyclerView.setHasFixedSize(false);
        mPostRecyclerView.setVisibility(View.GONE);
        mAdapter = new ProfilePostAdapter(mDataSet);
        mPostRecyclerView.setAdapter(mAdapter);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        setEditFab();

        mUserDataRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER);
        mProgramRef = FirebaseDatabase.getInstance().getReference().child(Constants.PROGRAM);
        mProgramRef.keepSynced(true);

        // User data is loaded.
        mSwipeRefreshLayout.setRefreshing(true);
        mRefreshData = new ValueEventListener() {
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
                } else {
                    mDpImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar_black));
                }
                if (dataSnapshot.hasChild(Constants.DP_THUMB_URL)) {
                    mAdapter.setDpUrl(dataSnapshot.child(Constants.DP_THUMB_URL).getValue().toString());
                    mAdapter.notifyDataSetChanged();
                }

                if (dataSnapshot.hasChild(Constants.COVER_URL)) {
                    String coverUrl = dataSnapshot.child(Constants.COVER_URL).getValue().toString();
                    RequestOptions coverOptions = new RequestOptions();
                    coverOptions.fitCenter();
                    Glide.with(getApplicationContext())
                            .load(coverUrl)
                            .into(mCoverImage);

                } else {
                    mCoverImage.setImageDrawable(getResources().getDrawable(R.drawable.navigation_drawer_image));
                }
                if (dataSnapshot.hasChild(Constants.USERNAME_PROFILE)) {

                    mName.setText(dataSnapshot.child(Constants.USERNAME_PROFILE).getValue().toString());
                } else {
                    mName.setText("Nothing to show");
                }
                if (dataSnapshot.hasChild(Constants.BIO)) {
                    mBio.setText(dataSnapshot.child(Constants.BIO).getValue().toString());
                } else {
                    mBio.setText("Nothing to show");
                }
                if (dataSnapshot.hasChild(Constants.LOCATION)) {
                    mLocation.setText(dataSnapshot.child(Constants.LOCATION).getValue().toString());
                } else {
                    mLocation.setText("Nothing to show");
                }
                if (dataSnapshot.hasChild(Constants.WORK)) {
                    mWork.setText(dataSnapshot.child(Constants.WORK).getValue().toString());
                } else {
                    mWork.setText("Nothing to show");
                }
                if (dataSnapshot.hasChild(Constants.EDUCATION)) {
                    mEducation.setText(dataSnapshot.child(Constants.EDUCATION).getValue().toString());
                } else {
                    mEducation.setText("Nothing to show");
                }
                if (dataSnapshot.hasChild(Constants.SKILLS)) {
                    mSkills.setText(dataSnapshot.child(Constants.SKILLS).getValue().toString());
                } else {
                    mSkills.setText("Nothing to show");
                }
                mSwipeRefreshLayout.setRefreshing(false);
                mSwipeRefreshLayout.setEnabled(false);
                setEditFab();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUserDataRef.child(mProfileId).addListenerForSingleValueEvent(mRefreshData);

        mProgramDataListener = mProgramRef
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        PostData data = makeDataBlock(dataSnapshot);
                        if (data.data.userId.equals(mProfileId)) {
                            mDataSet.add(data);
                            mAdapter.notifyItemInserted(mDataSet.size() - 1);
                            Log.e("ADD", "\t" + data.data.title);
                        } else {
                            Log.e("NOT ADDED", "\t\t\t" + data.data.title);
                        }

                        //Removing the empty text
                        if ((mEmptyText.getVisibility() == View.VISIBLE) && (!mDataSet.isEmpty())) {

                            mPostRecyclerView.setVisibility(View.VISIBLE);
                            mEmptyText.setVisibility(View.INVISIBLE);
                            Log.e("Tag", "did");
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        Log.e("Tag", "call 1");
        mProgramRef.removeEventListener(mProgramDataListener);
        mProgramRef.addChildEventListener(mProgramDataListener);

    }

    // Making of the single block of postData for each post which will later get inside the array list.
    private PostData makeDataBlock(DataSnapshot dataSnapshot) {
        /* Data fields are extracted from the JSON postData snapshot
            First checked if they exist or not then they are added in the postData block.
        */
        PostData returnData = new PostData();
        returnData.key = dataSnapshot.getKey();

        if (dataSnapshot.hasChild(Constants.DATE)) {
            returnData.data.date = dataSnapshot.child(Constants.DATE).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.FILEUID)) {
            returnData.data.fileUid = dataSnapshot.child(Constants.FILEUID).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.FILEURI)) {
            returnData.data.fileUri = dataSnapshot.child(Constants.FILEURI).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.LIKES)) {
            returnData.data.likes = dataSnapshot.child(Constants.LIKES).getValue().toString();
        }

        if (dataSnapshot.hasChild(Constants.COMMENTS)) {
            returnData.data.comments = dataSnapshot.child(Constants.COMMENTS).getValue().toString();
        } else {
            returnData.data.comments = "0";
        }
        if (dataSnapshot.hasChild(Constants.TITLE)) {
            returnData.data.title = dataSnapshot.child(Constants.TITLE).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.USERID)) {
            returnData.data.userId = dataSnapshot.child(Constants.USERID).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.USERNAME)) {
            returnData.data.userName = dataSnapshot.child(Constants.USERNAME).getValue().toString();
        }

        if (dataSnapshot.hasChild(Constants.LANGUAGE)) {
            returnData.data.language = dataSnapshot.child(Constants.LANGUAGE).getValue().toString();
        }

        if (dataSnapshot.hasChild(Constants.DESCRIPTION)) {
            returnData.data.description = dataSnapshot.child(Constants.DESCRIPTION).getValue().toString();
        }
        return returnData;
    }

    private void setEditFab() {
        if (!mProfileId.equals(mCurrentUser.getUid())) {
            mEditFab.setEnabled(false);
            mEditFab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEditFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfile = new Intent(ProfileActivity.this, ProfileEdit.class);
                editProfile.putExtra(Constants.ACTIVITY, mLastActivity);
                editProfile.putExtra(Constants.USERID, mProfileId);
                startActivityForResult(editProfile, REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mLastActivity = data.getStringExtra(Constants.ACTIVITY);
            mProfileId = data.getStringExtra(Constants.USERID);
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(true);
            mUserDataRef.child(mProfileId).addListenerForSingleValueEvent(mRefreshData);
            setEditFab();

            mProgramRef.removeEventListener(mProgramDataListener);
            mPostRecyclerView.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
            mDataSet.clear();
            mAdapter.notifyDataSetChanged();
            Log.e("Tag", "call2");
            mProgramRef.addChildEventListener(mProgramDataListener);
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
                break;
            case R.id.profile_search:
                Intent searchProfile = new Intent(ProfileActivity.this, SearchUserActivity.class);
                searchProfile.putExtra(Constants.ACTIVITY, mLastActivity);
                searchProfile.putExtra(Constants.USERID, mProfileId);
                startActivityForResult(searchProfile, REQUEST_CODE);
                break;
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
