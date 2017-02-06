package com.subhajitdas.c;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class EditFragment extends Fragment {

    private String mKey, mTitle, mContent;
    private String FILENAME = "newFile",FILEUID;
    private FirebaseUser mUser;

    private EditText mPostTitle, mPostContent;
    private Typeface custom_font;
    private ProgressDialog mProgress;


    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPostTitle = (EditText) getActivity().findViewById(R.id.edit_post_title);
        mPostContent = (EditText) getActivity().findViewById(R.id.edit_post_content);
        custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceCodePro-Regular.ttf");
        mPostContent.setTypeface(custom_font);
        try {
            mKey = getArguments().get("key").toString();
            mTitle = getArguments().get("title").toString();
            mContent = getArguments().get("postContent").toString();
        }catch (NullPointerException e){
            Toast.makeText(getActivity(),"Please restart the app",Toast.LENGTH_SHORT).show();
        }

        mPostTitle.setText(mTitle);
        mPostContent.setText(mContent);

        mProgress = new ProgressDialog(getActivity());

    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(null);
            actionBar.setTitle("Edit Post");
        } else {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(null);
            actionBar.setTitle("Posts");
        } else {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_back:
                getFragmentManager().popBackStack();
                return true;

            case R.id.action_save:
                mProgress.setMessage("Editing contents");
                mProgress.setCancelable(false);
                mProgress.show();
                docChange();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void docChange() {
        final DatabaseReference mProgRef = FirebaseDatabase.getInstance().getReference().child("test").child("program").child(mKey);  //TODO  remove .child(test)
        FILEUID = randomID();
        final StorageReference programsRef = FirebaseStorage.getInstance().getReference().child("programs/" + FILEUID + ".txt");
        mProgRef.child("userId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mUser.getUid().equals(dataSnapshot.getValue().toString())) {

                    mProgRef.child("fileUid").addListenerForSingleValueEvent(new ValueEventListener() {  // GETTING THE FILE NAME TO DELETE IT
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            StorageReference delPath = FirebaseStorage.getInstance().getReference().child("programs")
                                    .child(dataSnapshot.getValue().toString() + ".txt");
                            delPath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {       // DELETING THE TXT FILE
                                    mProgRef.child("title").setValue(mPostTitle.getText().toString());
                                    mProgress.setMessage("Re-uploading your post");
                                    Uri fileUri =makeFile();

                                    programsRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            mProgRef.child("fileUid").setValue(FILEUID);
                                            mProgRef.child("fileUri").setValue(taskSnapshot.getDownloadUrl().toString());
                                            mProgress.dismiss();
                                            getFragmentManager().popBackStack();
                                        }
                                    });


                                }
                            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Sorry file not deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }


                    });

                } else

                {
                    mProgress.dismiss();
                    Toast.makeText(getActivity(), "Sorry you cannot edit this post.\nYou are not the owner", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public Uri makeFile(){

        Uri fileUri;
        //-----MAKING FILE-----
        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(mPostContent.getText().toString().getBytes());

        } catch (IOException e) {
            Toast.makeText(getActivity(), "File Creation Error", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Toast.makeText(getActivity(), "File Creation Error", Toast.LENGTH_SHORT).show();
            }
        }

        // -----GETTING FILE DIRECTORY----
        File uploadFile = new File(getActivity().getFilesDir(), FILENAME);

        //----- UPLOADING THE MADE TEXT FILE----
        fileUri = Uri.fromFile(uploadFile);
        return fileUri;

    }

    protected static String randomID() {
        // creating UUID
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        // checking the value of random UUID
        return uid.randomUUID().toString();
    }
}
