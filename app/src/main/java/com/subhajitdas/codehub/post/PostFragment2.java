package com.subhajitdas.codehub.post;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.subhajitdas.codehub.Constants;
import com.subhajitdas.codehub.R;

import java.util.ArrayList;
import java.util.TimerTask;


public class PostFragment2 extends Fragment {

    private RecyclerView mPostRecyclerView;
    private PostDataAdapter mAdapter;
    private ArrayList<PostData> mDataSet;
    private ArrayList<String> mDatasetRecord;
    private DatabaseReference mProgramRef;
    private ChildEventListener mProgramDataListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public PostFragment2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataSet = new ArrayList<>();
        mDatasetRecord = new ArrayList<>();
        //Turning on the offline capabilities of the database.
        mProgramRef = FirebaseDatabase.getInstance().getReference().child(Constants.PROGRAM);
        mProgramRef.keepSynced(true);

        //Storing of data is done in this portion using an array list.
        /**
         * mDataSet - is the actual dataset.
         * mDatasetRecord - is the record for the post keys.
         *                   Needed so that later we can find out
         *                   the index where data is changed and removed.
         * */
        mProgramDataListener = mProgramRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /* Single data block is made
                    and added inside the mDataset.
                    mDatasetRecord is kept as a tracking arraylist.
                 */
                PostData data = makeDataBlock(dataSnapshot);
                mDataSet.add(data);
                mDatasetRecord.add(data.key);
                mAdapter.notifyItemInserted(mDataSet.size() - 1);
                //Removing the refreshing layout.
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
               // Log.e("Jeetu","On Added triggerd");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                /*The changed data block index is at first found.
                   Block is made and data is replaced at that index.
                */
                final int indexToReplace = mDatasetRecord.indexOf(dataSnapshot.getKey());
                PostData replaceData = makeDataBlock(dataSnapshot);
                mDataSet.remove(indexToReplace);
                mDataSet.add(indexToReplace,replaceData);
                /*The adapter is notified a bit late so that the animations \
                    of the button can complete.
                  If not done then the view changed is recreated and thus the
                    animation is lost.
                 */
                Handler handler = new Handler();
                Runnable runnable = new TimerTask() {
                    @Override
                    public void run() {
                        mAdapter.notifyItemChanged(indexToReplace);
                    }
                };
               handler.postDelayed(runnable,800);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                /*First we find out the index we need to remove.
                    Then simply remove data from the dataset and record set.
                    At last we just notify the adapter.
                */
                int indexToRemove = mDatasetRecord.indexOf(dataSnapshot.getKey());
                mDataSet.remove(indexToRemove);
                mDatasetRecord.remove(indexToRemove);
                mAdapter.notifyItemRemoved(indexToRemove);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // Making of the single block of data for each post which will later get inside the array list.
    private PostData makeDataBlock(DataSnapshot dataSnapshot) {
        /* Data fields are extracted from the JSON data snapshot
            First checked if they exist or not then they are added in the data block.
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
        if (dataSnapshot.hasChild(Constants.TITLE)) {
            returnData.data.title = dataSnapshot.child(Constants.TITLE).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.USERID)) {
            returnData.data.userId = dataSnapshot.child(Constants.USERID).getValue().toString();
        }
        if (dataSnapshot.hasChild(Constants.USERNAME)) {
            returnData.data.userName = dataSnapshot.child(Constants.USERNAME).getValue().toString();
        }
        return returnData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_2, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Initializing UI components and setting refreshing on.
        mPostRecyclerView = (RecyclerView) getActivity().findViewById(R.id.post_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setRefreshing(true);

        //Adapter and layout manager are set for recycler view.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        mPostRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new PostDataAdapter(mDataSet);
        mPostRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();

        //To handle the refresh behaviour of the layout.
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*
                 * 1st the child event listener is removed.
                 * 2nd Datasets are cleared.
                 * 3rd Adapter is notified of the dataset change.
                 * 4th All views from the recycler view are removed.
                 * 5th Again the child event listener is added.
                 */
                mProgramRef.removeEventListener(mProgramDataListener);
                mDataSet.clear();
                mAdapter.notifyDataSetChanged();
                mPostRecyclerView.removeAllViews();
                mDatasetRecord.clear();
                mProgramRef.addChildEventListener(mProgramDataListener);
            }
        });
    }
}
