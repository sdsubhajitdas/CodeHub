package com.subhajitdas.c;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Subhajit Das on 12-01-2017.
 */

public class PostFragment extends Fragment {

    private TextView mPostTitle, mPostContent;
    private ProgressDialog mProgress;
    private Bundle bundle;
    private Handler handler;


    private DatabaseReference mProgRef;
    private DatabaseReference mRootRef;
    private StorageReference mProgramFile;

    private String mKey = "blank";

    public PostFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPostTitle = (TextView) getActivity().findViewById(R.id.post_title);
        mPostContent = (TextView) getActivity().findViewById(R.id.post_content);
        mProgress = new ProgressDialog(getActivity());
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceCodePro-Regular.ttf");
        mPostContent.setTypeface(custom_font);
        try {
            mKey = getArguments().get("key").toString();
            mProgRef = FirebaseDatabase.getInstance().getReference().child("program").child(mKey);                //TODO use for update||Remove .child(test)
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mProgramFile = FirebaseStorage.getInstance().getReference().child("programs");
        } catch (Exception e) {

        }
        bundle = new Bundle();
        handler = new Handler();

    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);

        //  IMPLEMENTING INTERSTITIAL ADS
        final InterstitialAd interstitialAd;
        interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId("ca-app-pub-8238050563187834/5268170101");
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        interstitialAd.loadAd(adRequest);

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                } else {
                    handler.postDelayed(this, 10000);
                }
            }
        };

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });
        handler.postDelayed(runnable, 75000);

        // ADS ENDS
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgress.setMessage("Loading content");
        mProgress.setCancelable(false);
        mProgress.show();
        mProgRef.child("title").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPostTitle.setText(dataSnapshot.getValue().toString());
                bundle.putString("title", dataSnapshot.getValue().toString());
                mProgress.setMessage("Loading Post Contents");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Sorry post cannot be loaded", Toast.LENGTH_LONG).show();
            }
        });

        mProgRef.child("fileUid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final File localFile = new File(getActivity().getFilesDir() + "/cfile.txt");
                mProgramFile.child(dataSnapshot.getValue().toString() + ".txt").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        FileInputStream fis = null;
                        StringBuffer stringBuffer = new StringBuffer();
                        try {
                            fis = getActivity().openFileInput(localFile.getName());
                            int read = -1;
                            while ((read = fis.read()) != -1) {
                                stringBuffer.append((char) read);
                            }
                            mPostContent.setText(stringBuffer.toString());
                            bundle.putString("postContent", stringBuffer.toString());
                            mProgress.dismiss();
                        } catch (IOException e) {
                            Toast.makeText(getActivity(), "File Reading Error", Toast.LENGTH_SHORT).show();
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                    mProgress.dismiss();
                                } catch (IOException e) {
                                    Toast.makeText(getActivity(), "File Reading Error", Toast.LENGTH_SHORT).show();
                                    mProgress.dismiss();
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getActivity(), "File not downloaded ", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_post, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_edit:

                bundle.putString("key", mKey);
                EditFragment editFragment = new EditFragment();
                editFragment.setArguments(bundle);
                FragmentTransaction tempTransaction = getActivity().getFragmentManager().beginTransaction();
                tempTransaction.replace(R.id.main_activity_frag_container, editFragment);
                tempTransaction.addToBackStack(null);
                tempTransaction.commit();
                return true;
            case R.id.action_back:
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_del:
                delPost();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void delPost() {

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //Log.e("jeetu",currentUser.getUid());
        mProgress.setMessage("Deleting post");
        mProgress.setCancelable(false);
        mProgress.show();
        mProgRef.child("userId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (currentUser.getUid().equals(dataSnapshot.getValue().toString())) {                   // CHECKING IF USER IS OWNER OR NOT

                    mProgRef.child("fileUid").addListenerForSingleValueEvent(new ValueEventListener() {  // GETTING THE FILE NAME TO DELETE IT
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            StorageReference delPath = FirebaseStorage.getInstance().getReference().child("programs")
                                    .child(dataSnapshot.getValue().toString() + ".txt");
                            delPath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {       // DELETING THE TXT FILE
                                    delPostData();

                                }
                            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Sorry file not deleted", Toast.LENGTH_SHORT).show();
                                    delPostData();

                                }
                            });


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mProgress.dismiss();
                            delPostData();
                        }
                    });
                } else {
                    mProgress.dismiss();
                    Toast.makeText(getActivity(), "You cannot delete this post.\nYou are not the owner", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
            }
        });


    }

    private void delPostData() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRootRef.child("like").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            // REMOVING THE LIKE DATA
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mKey)) {
                    mRootRef.child("like").child(mKey).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
            }
        });

        mRootRef.child("bookmark").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override                                               // REMOVING BOOKMARK DATA
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mKey)) {
                    mRootRef.child("bookmark").child(mKey).removeValue();

                }

                mProgRef.removeValue();                         // REMOVING DATA FROM PROGRAM BRANCH

                mProgress.dismiss();
                Toast.makeText(getActivity(), "Post deleted", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
            }
        });
    }

}


