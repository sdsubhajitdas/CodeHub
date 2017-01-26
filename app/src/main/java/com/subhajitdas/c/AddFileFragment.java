package com.subhajitdas.c;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Subhajit Das on 12-01-2017.
 */

public class AddFileFragment extends Fragment {

    private EditText mEditor;
    private EditText mProgTitle;
    private Button mSubmitEditorButton;
    private CheckBox mFileUploadCheck;
    private ProgressDialog mProgress;
    private DrawerLayout mDrawerLayout;

    private StorageReference mStorageRef;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser = null;

    private String mEditorText;
    private String FILENAME = "newFile";
    private String FILEUID = "";
    private String mFileUriText;
    private static final int TEXT_REQUEST = 1;
    private Uri mFileUri = null;
    private boolean fileReady = false;

    public AddFileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        //-----AUTH STATE LISTENER----
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(getActivity(), "Sorry some error occurred please sign in again", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_file, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mEditor = (EditText) getActivity().findViewById(R.id.editorText);
        mSubmitEditorButton = (Button) getActivity().findViewById(R.id.submitTextButton);
        mFileUploadCheck = (CheckBox) getActivity().findViewById(R.id.fileUploadCheck);
        mProgTitle = (EditText) getActivity().findViewById(R.id.programTitle);
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mProgress = new ProgressDialog(getActivity());

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(null);
            actionBar.setTitle("Add Post");
        } else {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mSubmitEditorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FILEUID = randomID();
                StorageReference programsRef = mStorageRef.child("programs/" + FILEUID + ".txt");

                if (mFileUploadCheck.isChecked()) {

                    if (fileReady && !TextUtils.isEmpty(mProgTitle.getText().toString())) {
                        mProgress.setMessage("Uploading content");
                        mProgress.show();
                        mProgress.setCancelable(false);
                        uploadFirebase(mFileUri, programsRef);
                    } else if (TextUtils.isEmpty(mProgTitle.getText().toString()))
                        Toast.makeText(getActivity(), "Title is empty", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Uploading Failed", Toast.LENGTH_SHORT).show();


                } else {
                    mEditorText = mEditor.getText().toString();
                    if (!TextUtils.isEmpty(mEditorText) && !TextUtils.isEmpty(mProgTitle.getText().toString())) {


                        mProgress.setMessage("Uploading content");
                        mProgress.show();
                        mProgress.setCancelable(false);
                        //-----MAKING FILE-----
                        FileOutputStream fos = null;
                        try {
                            fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                            fos.write(mEditorText.getBytes());

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
                        mFileUri = Uri.fromFile(uploadFile);
                        uploadFirebase(mFileUri, programsRef);


                    } else if (TextUtils.isEmpty(mProgTitle.getText().toString()))
                        Toast.makeText(getActivity(), "Title is empty", Toast.LENGTH_SHORT).show();
                    else if (TextUtils.isEmpty(mEditorText))
                        Toast.makeText(getActivity(), "No text", Toast.LENGTH_SHORT).show();

                }
            }
        });

        mFileUploadCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFileUploadCheck.isChecked()) {
                    //----CHOOSE TXT FILE TO UPLOAD----
                    Intent textIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    textIntent.setType("text/*");
                    startActivityForResult(textIntent, TEXT_REQUEST);
                } else {
                    fileReady = false;
                    mFileUploadCheck.setText(R.string.upload_from_storage);
                    mEditor.setClickable(true);
                    mEditor.setFocusable(true);
                    mEditor.setFocusableInTouchMode(true);
                }
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEXT_REQUEST && resultCode == RESULT_OK && data != null) {
            mFileUri = data.getData();
            fileReady = true;
            mFileUploadCheck.setText(R.string.file_chosen);
            mEditor.setText("");
            mEditor.setClickable(false);
            mEditor.setFocusable(false);
        } else {
            Toast.makeText(getActivity(), "File not chosen ", Toast.LENGTH_LONG).show();
            fileReady = false;
            mFileUploadCheck.setChecked(false);
            mFileUploadCheck.setText(R.string.upload_from_storage);
            mEditor.setClickable(true);
            mEditor.setFocusable(true);
            mEditor.setFocusableInTouchMode(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }


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

    protected void uploadFirebase(Uri fileUri, StorageReference reference) {

        final String date;

        date = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());

        final DatabaseReference[] progDatabaseRef = {null};
        reference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        try {
                            mFileUriText = taskSnapshot.getDownloadUrl().toString();
                        } catch (NullPointerException e) {
                            Toast.makeText(getActivity(), "Problem\n" + e.toString(), Toast.LENGTH_LONG).show();
                        }
                        progDatabaseRef[0] = FirebaseDatabase.getInstance().getReference("program").push();
                        progDatabaseRef[0].child("fileUri").setValue(mFileUriText);
                        progDatabaseRef[0].child("fileUid").setValue(FILEUID);
                        progDatabaseRef[0].child("date").setValue(date);
                        progDatabaseRef[0].child("likes").setValue("0");
                        progDatabaseRef[0].child("userId").setValue(mCurrentUser.getUid());
                        progDatabaseRef[0].child("userName").setValue(mCurrentUser.getDisplayName());
                        progDatabaseRef[0].child("title").setValue(mProgTitle.getText().toString());

                        Toast.makeText(getActivity(), "Upload Successful", Toast.LENGTH_LONG).show();
                        mProgress.hide();
                        getActivity().getFragmentManager().popBackStack();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getActivity(), "Upload Unsuccessful", Toast.LENGTH_SHORT).show();
                        mProgress.hide();
                    }
                });
    }

    protected static String randomID() {
        // creating UUID
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        // checking the value of random UUID
        return uid.randomUUID().toString();
    }

}
