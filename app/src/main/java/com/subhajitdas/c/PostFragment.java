package com.subhajitdas.c;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Subhajit Das on 12-01-2017.
 */

public class PostFragment extends Fragment {

    TextView mPostTitle, mPostContent;
    ProgressDialog mProgress;

    DatabaseReference mProgRef;
    StorageReference mProgramFile;

    String mKey = "blank";

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
        try {
            mKey = getArguments().get("key").toString();
            mProgRef = FirebaseDatabase.getInstance().getReference().child("program").child(mKey);
            mProgramFile = FirebaseStorage.getInstance().getReference().child("programs");
        } catch (Exception e) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgress.setMessage("Loading content");
        mProgress.setCancelable(false);
        mProgress.show();
        mProgRef.child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPostTitle.setText(dataSnapshot.getValue().toString());
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

                final File localFile = new File(getActivity().getFilesDir()+"/cfile.txt");
                mProgramFile.child(dataSnapshot.getValue().toString() + ".txt").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        FileInputStream fis = null;
                        StringBuffer stringBuffer = new StringBuffer();
                        try {
                            fis=getActivity().openFileInput(localFile.getName());
                            int read=-1;
                            while ((read=fis.read())!=-1){
                                stringBuffer.append((char)read);
                            }
                            mPostContent.setText(stringBuffer.toString());
                            mProgress.hide();
                        } catch (IOException e) {
                            Toast.makeText(getActivity(),"File Reading Error",Toast.LENGTH_SHORT).show();
                        }
                        finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                    mProgress.hide();
                                } catch (IOException e) {
                                    Toast.makeText(getActivity(),"File Reading Error",Toast.LENGTH_SHORT).show();
                                    mProgress.hide();
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getActivity(), "File not downloaded ", Toast.LENGTH_SHORT).show();
                        mProgress.hide();
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
    }

}


