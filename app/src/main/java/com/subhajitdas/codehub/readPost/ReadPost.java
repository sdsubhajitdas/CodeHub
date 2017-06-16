package com.subhajitdas.codehub.readPost;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.subhajitdas.codehub.Constants;
import com.subhajitdas.codehub.R;
import com.subhajitdas.codehub.post.PostData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadPost extends AppCompatActivity {

    private TextView mTitle, mDescription, mDate, mPosterName, mPostContent;
    private PostData mPostData = new PostData();
    private SwipeRefreshLayout mRefreshLayout;
    private File mLocalFile;

    private StorageReference mProgramFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_post);
        //Getting data from Post Fragment.
        Intent intent = getIntent();
        if (intent != null) {
            //Whole post block is created here.
            mPostData.key = intent.getStringExtra(Constants.KEY);
            mPostData.data.title = intent.getStringExtra(Constants.TITLE);
            mPostData.data.userName = intent.getStringExtra(Constants.USERNAME);
            mPostData.data.userId = intent.getStringExtra(Constants.USERID);
            mPostData.data.date = intent.getStringExtra(Constants.DATE);
            mPostData.data.likes = intent.getStringExtra(Constants.LIKES);
            mPostData.data.fileUid = intent.getStringExtra(Constants.FILEUID);
            mPostData.data.fileUri = intent.getStringExtra(Constants.FILEURI);
            mPostData.data.description= intent.getStringExtra(Constants.DESCRIPTION);
            mPostData.data.language=intent.getStringExtra(Constants.LANGUAGE);
        }
        //Database is initialized.
        mProgramFile = FirebaseStorage.getInstance().getReference().child(Constants.PROGRAMS);

        //UI elements are initialized.
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mTitle = (TextView) findViewById(R.id.read_title);
        mDescription = (TextView) findViewById(R.id.read_description);
        mPosterName = (TextView) findViewById(R.id.read_poster_name);
        mPostContent = (TextView) findViewById(R.id.read_content);
            // Toolbar work done.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.read_toolbar);
        mToolbar.setTitle(mPostData.data.title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // UI elements are filled with data according to the post data.
        mTitle.setText(mPostData.data.title);
        mDate = (TextView) findViewById(R.id.read_date);
        mDate.setText(mPostData.data.date);
        mPosterName.setText(mPostData.data.userName);
        mDescription.setText(mPostData.data.description);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //File is created in the app internal data location.
        mLocalFile = new File(getFilesDir() + "/" + mPostData.data.fileUid + ".txt");

        /* Cache system.
                First checked if the file exists or not if yes
                    then we simply display the contents of the file.
                else we download the file and show the contents.
        */
        if (mLocalFile.exists()) {
            displayFile(mLocalFile);
            //Toast.makeText(this, "File exists" + mLocalFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            mRefreshLayout.setRefreshing(true);
            downloadFileAndDisplay(mLocalFile);
            //Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Handling the pull down to swipe action.
            //Re downloads the file and displays the content.
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(true);
                downloadFileAndDisplay(mLocalFile);
            }
        });
    }

    private void displayFile(File localFile) {
        // A file with the name of fileUid is created and kept as a cache.
        FileInputStream fis = null;
        StringBuilder stringBuffer = new StringBuilder();
        try {
            fis = openFileInput(localFile.getName());
            int read = -1;
            //Reading the whole file till end.
            while ((read = fis.read()) != -1) {
                stringBuffer.append((char) read);
            }
            //Finally setting the content of the file in textView
            mPostContent.setText(stringBuffer.toString());
        } catch (IOException e) {
            //Error msg shown.
            if (Build.VERSION.SDK_INT >= 21) {
                Snackbar.make(findViewById(R.id.read_coordinator),
                        "File reading error",
                        Snackbar.LENGTH_LONG).show();
            } else
                Toast.makeText(ReadPost.this, "File reading error", Toast.LENGTH_LONG).show();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    //Error msg shown.
                    if (Build.VERSION.SDK_INT >= 21) {
                        Snackbar.make(findViewById(R.id.read_coordinator),
                                "File reading error",
                                Snackbar.LENGTH_LONG).show();
                    } else
                        Toast.makeText(ReadPost.this, "File reading error", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private void downloadFileAndDisplay(final File localFile) {
        //File is downloaded from Firebase.
        mProgramFile.child(mPostData.data.fileUid + ".txt").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //File sent for display.
                displayFile(localFile);
                mRefreshLayout.setRefreshing(false);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //If failure occurs then show a display msg.
                if (Build.VERSION.SDK_INT >= 21) {
                    Snackbar.make(findViewById(R.id.read_coordinator),
                            "File not downloaded",
                            Snackbar.LENGTH_LONG).show();
                } else
                    Toast.makeText(ReadPost.this, "File not downloaded", Toast.LENGTH_LONG).show();
            }
        });
    }
}
