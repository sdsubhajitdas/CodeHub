package com.subhajitdas.c.read;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;
import com.subhajitdas.c.edit.EditPostActivity;
import com.subhajitdas.c.post.PostData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class ReadPostActivity extends AppCompatActivity {

    private TextView mTitle, mDescription, mDate, mPosterName, mPostContent;
    private PostData mPostData = new PostData();
    private SwipeRefreshLayout mRefreshLayout;
    private File mLocalFile;
    private ImageView mLangView;
    private final int REQUEST_CODE = 123;

    private StorageReference mProgramFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_post);
        //Getting data from Post Fragment.
        Intent intent = getIntent();
        if (intent != null) {
            //Whole post block is created here.
            if (intent.getStringExtra(Constants.KEY) != null) {
                mPostData.key = intent.getStringExtra(Constants.KEY);
                mPostData.data.title = intent.getStringExtra(Constants.TITLE);
                mPostData.data.userName = intent.getStringExtra(Constants.USERNAME);
                mPostData.data.userId = intent.getStringExtra(Constants.USERID);
                mPostData.data.date = intent.getStringExtra(Constants.DATE);
                mPostData.data.likes = intent.getStringExtra(Constants.LIKES);
                mPostData.data.fileUid = intent.getStringExtra(Constants.FILEUID);
                mPostData.data.fileUri = intent.getStringExtra(Constants.FILEURI);
                mPostData.data.description = intent.getStringExtra(Constants.DESCRIPTION);
                mPostData.data.language = intent.getStringExtra(Constants.LANGUAGE);
            } else {
                mPostData = new PostData();
            }
        }
        //Database is initialized.
        mProgramFile = FirebaseStorage.getInstance().getReference().child(Constants.PROGRAMS);

        //UI elements are initialized.
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mTitle = (TextView) findViewById(R.id.read_title);
        mDescription = (TextView) findViewById(R.id.read_description);
        mPosterName = (TextView) findViewById(R.id.read_poster_name);
        mPostContent = (TextView) findViewById(R.id.read_content);
        mDate = (TextView) findViewById(R.id.read_date);
        mLangView = (ImageView) findViewById(R.id.read_lang);
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
        mDate.setText(mPostData.data.date);
        mPosterName.setText(mPostData.data.userName);
        mDescription.setText(mPostData.data.description);

        //Setting the language image view.
        if (mPostData.data.language.equals(Constants.C)) {
            mLangView.setImageResource(R.drawable.c);
        } else if (mPostData.data.language.equals(Constants.CPP)) {
            mLangView.setImageResource(R.drawable.cpp);
        } else if (mPostData.data.language.equals(Constants.JAVA)) {
            mLangView.setImageResource(R.drawable.java);
        } else if (mPostData.data.language.equals(Constants.PYTHON)) {
            mLangView.setImageResource(R.drawable.python);
        } else {
            mLangView.setVisibility(View.INVISIBLE);
        }
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

    //Displays a file already in storage.
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
                Toast.makeText(ReadPostActivity.this, "File reading error", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ReadPostActivity.this, "File reading error", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //Downloads and shows the file.
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
                    Toast.makeText(ReadPostActivity.this, "File not downloaded", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        switch (id) {

            case R.id.read_action_edit:
                // Opening the edit page
                if (currentUser.equals(mPostData.data.userId)) {        //Checking if authorized to edit or not.
                    Intent editIntent = new Intent(ReadPostActivity.this, EditPostActivity.class);
                    editIntent.putExtra(Constants.KEY, mPostData.key);
                    editIntent.putExtra(Constants.TITLE, mPostData.data.title);
                    editIntent.putExtra(Constants.DESCRIPTION, mPostData.data.description);
                    editIntent.putExtra(Constants.LANGUAGE, mPostData.data.language);
                    editIntent.putExtra(Constants.EDITOR, mPostContent.getText());
                    editIntent.putExtra(Constants.USERNAME, mPostData.data.userName);
                    editIntent.putExtra(Constants.USERID, mPostData.data.userId);
                    editIntent.putExtra(Constants.DATE, mPostData.data.date);
                    editIntent.putExtra(Constants.LIKES, mPostData.data.likes);
                    editIntent.putExtra(Constants.FILEUID, mPostData.data.fileUid);
                    editIntent.putExtra(Constants.FILEURI, mPostData.data.fileUri);
                    startActivityForResult(editIntent, REQUEST_CODE);
                } else {                                                //If not authorized.
                    if (Build.VERSION.SDK_INT >= 21) {
                        Snackbar.make(findViewById(R.id.read_coordinator),
                                "Sorry you are not authorized to edit this.",
                                Snackbar.LENGTH_LONG).show();
                    } else
                        Toast.makeText(ReadPostActivity.this, "Sorry you are not authorized to edit this.", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.read_action_del:
                // Showing a dialog asking if sure about deletion ??
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Post Delete alert")
                        .setMessage("Are you sure you want to delete this post ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {                        //If yes .
                                if (currentUser.equals(mPostData.data.userId)) {                // Authorized to del the post.
                                    // Showing the progress dialog
                                    final ProgressDialog progressDialog = new ProgressDialog(ReadPostActivity.this);
                                    progressDialog.setTitle("Please Wait");
                                    progressDialog.setMessage("We are deleting your post");
                                    progressDialog.show();
                                    progressDialog.setCancelable(false);

                                    //Deleting the file from storage.
                                    FirebaseStorage.getInstance().getReference()
                                            .child(Constants.PROGRAMS)
                                            .child(mPostData.data.fileUid + ".txt")
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.setMessage("Deleting post records.");
                                                    //Deleting from BOOKMARK branch.
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(Constants.BOOKMARK)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.hasChild(mPostData.key)) {
                                                                        FirebaseDatabase.getInstance().getReference()
                                                                                .child(Constants.BOOKMARK)
                                                                                .child(mPostData.key)
                                                                                .removeValue();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                    // Deleting from LIKE branch.
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(Constants.LIKE)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.hasChild(mPostData.key)) {
                                                                        FirebaseDatabase.getInstance().getReference()
                                                                                .child(Constants.LIKE)
                                                                                .child(mPostData.key)
                                                                                .removeValue();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                    // Deleting from PROGRAMS branch.
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(Constants.PROGRAM)
                                                            .child(mPostData.key)
                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            finish();
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if (Build.VERSION.SDK_INT >= 21) {
                                                Snackbar.make(findViewById(R.id.read_coordinator),
                                                        "Sorry post was not deleted.",
                                                        Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ReadPostActivity.this, "Sorry post was not deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else                          // Not authorized to delete the post.

                                {
                                    if (Build.VERSION.SDK_INT >= 21) {
                                        Snackbar.make(findViewById(R.id.read_coordinator),
                                                "Sorry you are not authorized to delete this.",
                                                Snackbar.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(ReadPostActivity.this, "Sorry you are not authorized to delete this.", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {                // If no.
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                builder.show();             // Showing the dialog.

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                //Whole post block is created here.
                if (data.getStringExtra(Constants.KEY) != null) {
                    mPostData.key = data.getStringExtra(Constants.KEY);
                    mPostData.data.title = data.getStringExtra(Constants.TITLE);
                    mPostData.data.userName = data.getStringExtra(Constants.USERNAME);
                    mPostData.data.userId = data.getStringExtra(Constants.USERID);
                    mPostData.data.date = data.getStringExtra(Constants.DATE);
                    mPostData.data.likes = data.getStringExtra(Constants.LIKES);
                    mPostData.data.fileUid = data.getStringExtra(Constants.FILEUID);
                    mPostData.data.fileUri = data.getStringExtra(Constants.FILEURI);
                    mPostData.data.description = data.getStringExtra(Constants.DESCRIPTION);
                    mPostData.data.language = data.getStringExtra(Constants.LANGUAGE);
                } else {
                    mPostData = new PostData();
                }
            }
            // UI elements are filled with data according to the post data.
            mTitle.setText(mPostData.data.title);
            mDate.setText(mPostData.data.date);
            mPosterName.setText(mPostData.data.userName);
            mDescription.setText(mPostData.data.description);

            //Setting the language image view.
            if (mPostData.data.language.equals(Constants.C)) {
                mLangView.setImageResource(R.drawable.c);
            } else if (mPostData.data.language.equals(Constants.CPP)) {
                mLangView.setImageResource(R.drawable.cpp);
            } else if (mPostData.data.language.equals(Constants.JAVA)) {
                mLangView.setImageResource(R.drawable.java);
            } else if (mPostData.data.language.equals(Constants.PYTHON)) {
                mLangView.setImageResource(R.drawable.python);
            } else {
                mLangView.setVisibility(View.INVISIBLE);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mPostData.data.title);
            }
        }
    }
}
