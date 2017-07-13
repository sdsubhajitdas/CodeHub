package com.subhajitdas.c.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SearchUserActivity extends AppCompatActivity {

    private EditText mSearchText;
    private ImageButton mSearchOk;
    private RecyclerView mUserView;

    private DatabaseReference mUserRef;

    private UserAdapter adapter;

    private ArrayList<UserData> mDataset;
    private String mLastActivity, mProfileId;

    public class UserData {
        public String name, bio, key, dpUrl;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        Intent gotIntent = getIntent();
        mLastActivity = gotIntent.getStringExtra(Constants.ACTIVITY);
        mProfileId = gotIntent.getStringExtra(Constants.USERID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.setTitle("Search For Other Coders");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDataset = new ArrayList<>();
        mSearchText = (EditText) findViewById(R.id.search_text);
        mSearchOk = (ImageButton) findViewById(R.id.search_ok);
        mUserView = (RecyclerView) findViewById(R.id.search_result);

        adapter = new UserAdapter(mDataset);
        mUserView.setLayoutManager(new LinearLayoutManager(this));
        mUserView.setAdapter(adapter);

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER);

        mUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserData data = new UserData();

                data.key = dataSnapshot.getKey();

                if (dataSnapshot.hasChild(Constants.USERNAME_PROFILE)) {
                    data.name = dataSnapshot.child(Constants.USERNAME_PROFILE).getValue().toString();
                } else {
                    data.name = null;
                }

                if (dataSnapshot.hasChild(Constants.BIO)) {
                    data.bio = dataSnapshot.child(Constants.BIO).getValue().toString();
                } else {
                    data.bio = null;
                }

                if (dataSnapshot.hasChild(Constants.DP_THUMB_URL)) {
                    data.dpUrl = dataSnapshot.child(Constants.DP_THUMB_URL).getValue().toString();
                } else {
                    data.dpUrl = null;
                }
                mDataset.add(data);
                adapter.notifyItemInserted(mDataset.size() - 1);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSearchOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = mSearchText.getText().toString();
                searchText = searchText.trim();
                if (TextUtils.isEmpty(searchText)) {
                    adapter.setFilter(mDataset);
                } else {
                    ArrayList<UserData> newDataset = new ArrayList<UserData>();
                    for (int i = 0; i < mDataset.size(); i++) {
                        String temp = mDataset.get(i).name.toLowerCase();
                        searchText = searchText.toLowerCase();
                        if (temp.contains(searchText)){
                            newDataset.add(mDataset.get(i));
                        }
                    }
                    adapter.setFilter(newDataset);
                }
            }
        });
    }

    //If the hardware back button is pressed.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendBackData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            // If the back toolbar back is pressed.
            case android.R.id.home:
                sendBackData();
                return true;
        }
        return false;
    }

    private void sendBackData() {
        Intent backData = new Intent();
        backData.putExtra(Constants.ACTIVITY, mLastActivity);
        backData.putExtra(Constants.USERID, mProfileId);
        setResult(RESULT_OK, backData);
        finish();
    }


    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        private ArrayList<UserData> mDataSet;

        public UserAdapter(ArrayList<UserData> datset) {
            mDataSet = datset;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_layout, parent, false);

            return new UserAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.name.setText("No Name");
            if (mDataSet.get(position).name != null) {
                holder.name.setText(mDataSet.get(position).name);
            }
            holder.bio.setText("No Bio");
            if (mDataSet.get(position).bio != null) {
                holder.bio.setText(mDataSet.get(position).bio);
            }
            holder.dp.setImageDrawable(holder.context.getResources().getDrawable(R.drawable.ic_avatar_black));
            if (mDataSet.get(position).dpUrl != null) {
                RequestOptions dpOptions = new RequestOptions();
                dpOptions.circleCrop();
                dpOptions.placeholder(R.drawable.ic_avatar_black);

                Glide.with(holder.context)
                        .load(mDataSet.get(position).dpUrl)
                        .apply(dpOptions)
                        .into(holder.dp);
            }

            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mProfileId = mDataSet.get(position).key;
                    sendBackData();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        public void setFilter(ArrayList<UserData> dataset) {
            mDataSet = dataset;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView name, bio;
            private ImageView dp;
            private Context context;
            private ConstraintLayout constraintLayout;

            public ViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.user_name);
                bio = (TextView) v.findViewById(R.id.user_bio);
                dp = (ImageView) v.findViewById(R.id.user_dp);
                constraintLayout = (ConstraintLayout) v.findViewById(R.id.user_layout);
                context = v.getContext();
            }
        }
    }
}
