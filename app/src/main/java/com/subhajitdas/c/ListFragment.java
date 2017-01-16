package com.subhajitdas.c;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ListFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private ProgressDialog mProgress;
    private TextView mEmptyView;

    AddFileFragment addFile;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser = null;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mProgramRef;
    private DatabaseReference mLikeRef;
    private DatabaseReference mBookmarkRef;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;
    private boolean mProcessLike = false;
    private boolean mProcessBookmark = false;

    public ListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mProgramRef = FirebaseDatabase.getInstance().getReference().child("program");
        mLikeRef = FirebaseDatabase.getInstance().getReference().child("Like");
        mBookmarkRef = FirebaseDatabase.getInstance().getReference().child("bookmark");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser != null) {
                    // User is signed in
                } else {
                    SharedPreferences loginState = getActivity().getSharedPreferences("LOGIN_STATE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = loginState.edit();
                    editor.putInt("LOGIN_STATE", 0);
                    editor.apply();
                    mProgress.hide();
                    Intent intent = new Intent(getActivity(), com.subhajitdas.c.LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgress = new ProgressDialog(getActivity());


        mEmptyView = (TextView) getActivity().findViewById(R.id.empty_view);

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.INVISIBLE);

        //----CONNECTION CHECKING AND PROGRESS DIALOGUE HIDING-----
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            mProgress.hide();
            Toast.makeText(getActivity(), "Sorry no network connection", Toast.LENGTH_LONG).show();
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Posts");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mProgress.setMessage("Loading content");
        mProgress.setCancelable(false);
        mProgress.show();
        mAuth.addAuthStateListener(mAuthListener);
        setHasOptionsMenu(true);
        mProgramRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgress.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);


    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentUser = mAuth.getCurrentUser();

        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.post_layout,
                PostViewHolder.class,
                mProgramRef
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, final Post model, final int position) {
                viewHolder.setPostTitle(model.getTitle());
                viewHolder.setPostPoster(model.getUserName());
                viewHolder.setPostDate(model.getDate());
                viewHolder.setPostLike(model.getLikes());
                viewHolder.setLikeButton(getRef(position).getKey(), mCurrentUser.getUid());
                viewHolder.setBookmarkButton(getRef(position).getKey(),mCurrentUser.getUid());

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

                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        if (mProcessLike) {
                            mLikeRef.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mProcessLike) {
                                        if (dataSnapshot.hasChild(mCurrentUser.getUid())) {
                                            mLikeRef.child(getRef(position).getKey()).child(mCurrentUser.getUid()).removeValue();
                                            int tempLike = Integer.parseInt(model.getLikes());
                                            tempLike = tempLike - 1;
                                            mProgramRef.child(getRef(position).getKey()).child("likes").setValue(Integer.toString(tempLike));
                                            mProcessLike = false;
                                        } else {
                                            mLikeRef.child(getRef(position).getKey()).child(mCurrentUser.getUid()).setValue(mCurrentUser.getDisplayName());
                                            int tempLike = Integer.parseInt(model.getLikes());
                                            tempLike = tempLike + 1;
                                            mProgramRef.child(getRef(position).getKey()).child("likes").setValue(Integer.toString(tempLike));
                                            mProcessLike = false;
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

                viewHolder.mBookmarkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessBookmark = true;
                        if (mProcessBookmark) {
                            mBookmarkRef.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mProcessBookmark) {
                                        if (dataSnapshot.hasChild(mCurrentUser.getUid())) {
                                            mBookmarkRef.child(getRef(position).getKey()).child(mCurrentUser.getUid()).removeValue();
                                            mProcessBookmark = false;
                                        } else {
                                            mBookmarkRef.child(getRef(position).getKey()).child(mCurrentUser.getUid()).setValue("yes");
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
        //----HIDING THE PROGRESS DIALOGUE WHEN WE RETRIEVE THE DATA----
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
                mProgress.hide();
                adapter.unregisterAdapterDataObserver(this);
            }

        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        adapter.cleanup();
        mRecyclerView.setAdapter(null);
        setHasOptionsMenu(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                mProgress.setMessage("Logging Out");
                mProgress.show();
                mProgress.setCancelable(false);
                mAuth.signOut();
                return true;

            case R.id.action_add_program:
                addFile = new AddFileFragment();
                FragmentTransaction tempTransaction = getActivity().getFragmentManager().beginTransaction();
                tempTransaction.replace(R.id.main_activity_frag_container, addFile);
                tempTransaction.addToBackStack(null);
                tempTransaction.commit();
                return true;
            case R.id.action_bookmarks_page:
                BookmarksFragment bookmarksFragment =new BookmarksFragment();
                FragmentTransaction tempTransaction2=getActivity().getFragmentManager().beginTransaction();
                tempTransaction2.replace(R.id.main_activity_frag_container,bookmarksFragment);
                tempTransaction2.addToBackStack(null);
                tempTransaction2.commit();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        addFile.onActivityResult(requestCode, resultCode, data);

    }
}
