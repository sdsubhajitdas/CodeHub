package com.subhajitdas.c.post;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookmarkFragment extends Fragment {

    private TextView mNoBookmark;
    private RecyclerView mBookmarkRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private DatabaseReference mProgramRef, mBookmarkRef;
    private ChildEventListener mProgramRefListener, mBookmarkRefListener;
    private ValueEventListener mCheckDataListener;
    private FirebaseUser mCurrentUser;

    private ArrayList<String> mDataKey;         //To store the key values of posts.
    private ArrayList<PostData> mDataSet;       //Actual data of the post.
    private PostDataAdapter mAdapter;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataKey = new ArrayList<>();
        mDataSet = new ArrayList<>();
        mBookmarkRef = FirebaseDatabase.getInstance().getReference().child(Constants.BOOKMARK);     //Getting bookmark branch.
        mBookmarkRef.keepSynced(true);
        mProgramRef = FirebaseDatabase.getInstance().getReference().child(Constants.PROGRAM);       //TODO maybe change during update check
        mProgramRef.keepSynced(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // To get keys values of the posts we need to display.
        mBookmarkRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mDataKey.add(dataSnapshot.getKey());        // Post keys are added.
                if (mDataKey.size() == 1) {
                    mProgramRef.addChildEventListener(mProgramRefListener);     // At the 1st key retrieval of data for post is started.
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
        };

        //Making a query.
        Query query = mBookmarkRef
                .orderByChild(mCurrentUser.getUid())
                .equalTo("yes");

        //For checking if no data then remove pull down to refresh.
        mCheckDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If datasnapshot is null then it means no data is present.
                if (dataSnapshot.getValue() == null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mNoBookmark.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        query.addChildEventListener(mBookmarkRefListener);              // Getting the key values.
        query.addListenerForSingleValueEvent(mCheckDataListener);       // Checking for no data.

        // Getting actual data of the posts.
        mProgramRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (mDataKey.indexOf(dataSnapshot.getKey()) >= 0) {     // Checking if the post is needed or not.
                    mDataSet.add(makeDataBlock(dataSnapshot));          // Added in dataset.
                    mAdapter.notifyItemInserted(mDataSet.size() - 1);
                    if (mSwipeRefreshLayout.isRefreshing()) {           // Turning off the pull down to refresh.
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
                // Showing msg if there is no bookmark.
                if (mDataKey.size() < 0) {
                    mNoBookmark.setVisibility(View.VISIBLE);
                } else {
                    mNoBookmark.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (mDataKey.indexOf(dataSnapshot.getKey()) >= 0) {     // Checking if the post is needed or not.
                    int indexToReplace = -1;
                    for (int i = 0; i < mDataSet.size(); i++) {                     // Searching for the index.
                        if (dataSnapshot.getKey().equals(mDataSet.get(i).key)) {
                            indexToReplace = i;
                            break;
                        }
                    }
                    PostData replaceData = makeDataBlock(dataSnapshot);         // Making of the data block.
                    if (indexToReplace != -1) {
                        mDataSet.remove(indexToReplace);
                        mDataSet.add(indexToReplace, replaceData);
                        /*The adapter is notified a bit late so that the animations
                            of the button can complete.
                          If not done then the view changed is recreated and thus the
                            animation is lost.
                        */
                        Handler handler = new Handler();
                        final int finalIndexToReplace = indexToReplace;
                        Runnable runnable = new TimerTask() {
                            @Override
                            public void run() {
                                mAdapter.notifyItemChanged(finalIndexToReplace);
                            }
                        };
                        handler.postDelayed(runnable, 800);
                    }

                }

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
        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Initializing UI components.
        mBookmarkRecyclerView = (RecyclerView) getActivity().findViewById(R.id.bookmark_recycler_view);
        mNoBookmark = (TextView) getActivity().findViewById(R.id.no_bookmarks);
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swiperefresh2);
        mSwipeRefreshLayout.setRefreshing(true);

        //App Toolbar work.
        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.post_toolbar);
        mToolbar.setTitle("Bookmarks");

        //Adapter and layout manager are set for recycler view.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        mBookmarkRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new PostDataAdapter(mDataSet);
        mBookmarkRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        /*  To handle pull down to refresh behaviour.
                1.  All the listeners are removed.
                2.  All the data sets are cleared.
                3.  Adapter is notified and all the views are removed.
                4.  Again the listeners are attached.
        */
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mProgramRef.removeEventListener(mProgramRefListener);
                mBookmarkRef
                        .orderByChild(mCurrentUser.getUid())
                        .equalTo("yes")
                        .removeEventListener(mBookmarkRefListener);
                mDataSet.clear();
                mDataKey.clear();
                mAdapter.notifyDataSetChanged();
                mBookmarkRecyclerView.removeAllViews();
                mBookmarkRef
                        .orderByChild(mCurrentUser.getUid())
                        .equalTo("yes")
                        .addChildEventListener(mBookmarkRefListener);
                mBookmarkRef
                        .orderByChild(mCurrentUser.getUid())
                        .equalTo("yes")
                        .addListenerForSingleValueEvent(mCheckDataListener);
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

        if (dataSnapshot.hasChild(Constants.LANGUAGE)) {
            returnData.data.language = dataSnapshot.child(Constants.LANGUAGE).getValue().toString();
        }

        if (dataSnapshot.hasChild(Constants.DESCRIPTION)) {
            returnData.data.description = dataSnapshot.child(Constants.DESCRIPTION).getValue().toString();
        }
        return returnData;
    }

}
