package com.subhajitdas.c;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;



public class FeedbackFragment extends Fragment {

    private ImageView mScreenhot;
    private TextView mFeebackText;
    private FloatingActionButton mDone;
    private Uri mImageUri,mFileUri;
    private ProgressDialog mProgress;

    public static final int GALLERY_REQUEST =5;
    private boolean filechoosen=false;
    private String FILENAME = "newFile";
    private String RANDOMID ;

    private FirebaseUser mCurrentUser;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mScreenhot = (ImageView) getActivity().findViewById(R.id.add_image);
        mFeebackText = (TextView) getActivity().findViewById(R.id.feedback_text);
        mDone= (FloatingActionButton) getActivity().findViewById(R.id.feedback_done);
        mProgress =new ProgressDialog(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        final StorageReference feedbackRef= FirebaseStorage.getInstance().getReference().child("feedback");
        final DatabaseReference feedback= FirebaseDatabase.getInstance().getReference().child("feedback").push();
        mScreenhot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(mFeebackText.getText().toString())) {

                    RANDOMID=randomID().substring(0,8);
                    mProgress.setMessage("Submitting your feedback");
                    mProgress.setCancelable(false);
                    mProgress.show();
                    //-----MAKING FILE-----
                    FileOutputStream fos = null;
                    try {
                        fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                        fos.write(mFeebackText.getText().toString().getBytes());

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

                    //----- URI OF THE MADE TEXT FILE----
                    mFileUri = Uri.fromFile(uploadFile);

                    if(filechoosen)
                    {
                        feedbackRef.child(RANDOMID+"_image.jpg").putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot imageTaskSnapshot) {
                                final String imageUrl =imageTaskSnapshot.getDownloadUrl().toString();
                                feedbackRef.child(RANDOMID+"_text.txt").putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot textTaskSnapshot) {
                                        feedback.child("user").setValue(mCurrentUser.getDisplayName());
                                        feedback.child("email").setValue(mCurrentUser.getEmail());
                                        feedback.child("imageUrl").setValue(imageUrl);
                                        feedback.child("textUrl").setValue(textTaskSnapshot.getDownloadUrl().toString());
                                        feedback.child("filename").setValue(RANDOMID);

                                        mProgress.dismiss();

                                        Toast.makeText(getActivity(),"Thank you for your feedback.",Toast.LENGTH_SHORT).show();
                                        getFragmentManager().popBackStack();
                                    }
                                });
                            }
                        });
                    }
                    else {
                        feedbackRef.child(RANDOMID+"_text.txt").putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot textTaskSnapshot) {
                                feedback.child("user").setValue(mCurrentUser.getDisplayName());
                                feedback.child("email").setValue(mCurrentUser.getEmail());
                                feedback.child("textUrl").setValue(textTaskSnapshot.getDownloadUrl().toString());
                                feedback.child("filename").setValue(RANDOMID);

                                mProgress.dismiss();

                                Toast.makeText(getActivity(),"Thank you for your feedback.",Toast.LENGTH_SHORT).show();
                                getFragmentManager().popBackStack();
                            }
                        });
                    }

                }
                else {
                    Toast.makeText(getActivity(),"No feedback given",Toast.LENGTH_SHORT).show();
                }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode== RESULT_OK) {

            mImageUri= data.getData();
            mScreenhot.setImageURI(mImageUri);
            filechoosen =true;
        }
    }

    protected static String randomID() {
        // creating UUID
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        // checking the value of random UUID
        return uid.randomUUID().toString();
    }
}
