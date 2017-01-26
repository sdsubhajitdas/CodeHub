package com.subhajitdas.c;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Subhajit Das on 16-01-2017.
 */

public class BookmarksFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    private DatabaseReference mBookRef;
    private Query mBookmarksRef;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;
    private FirebaseUser mCurrentUser;

    private boolean mProcessBookmark = false;

    public BookmarksFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookRef = FirebaseDatabase.getInstance().getReference().child("bookmark");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.bookmark_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyView = (TextView) getActivity().findViewById(R.id.empty_bookmark_view);
        mEmptyView.setVisibility(View.VISIBLE);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Bookmarks");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //setHasOptionsMenu(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mBookmarksRef = mBookRef.orderByChild(mCurrentUser.getUid()).equalTo("yes");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.post_layout,
                PostViewHolder.class,
                mBookmarksRef
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, final Post model, final int position) {
                viewHolder.mLikeButton.setVisibility(View.INVISIBLE);
                viewHolder.mLikeText.setVisibility(View.INVISIBLE);
                viewHolder.setBookmarkTitle(getRef(position).getKey());
                viewHolder.setBookmarkPoster(getRef(position).getKey());
                viewHolder.setBookmarkDate(getRef(position).getKey());
                viewHolder.setBookmarkButton(getRef(position).getKey(), mCurrentUser.getUid());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("key", getRef(position).getKey());
                        PostFragment post = new PostFragment();
                        post.setArguments(bundle);
                        FragmentTransaction tempTransaction = getActivity().getFragmentManager().beginTransaction();
                        tempTransaction.replace(R.id.main_activity_frag_container, post);
                        tempTransaction.addToBackStack(null);
                        tempTransaction.commit();
                    }
                });

                viewHolder.mBookmarkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessBookmark = true;
                        if (mProcessBookmark) {
                            mBookRef.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mProcessBookmark) {
                                        if (dataSnapshot.hasChild(mCurrentUser.getUid())) {
                                            mBookRef.child(getRef(position).getKey()).child(mCurrentUser.getUid()).removeValue();
                                            mBookmarksRef = mBookRef.orderByChild(mCurrentUser.getUid()).equalTo("yes");
                                            mProcessBookmark = false;
                                        } else {
                                            mBookRef.child(getRef(position).getKey()).child(mCurrentUser.getUid()).setValue("yes");
                                            mProcessBookmark = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
                adapter.unregisterAdapterDataObserver(this);
            }

        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu2, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_back:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
