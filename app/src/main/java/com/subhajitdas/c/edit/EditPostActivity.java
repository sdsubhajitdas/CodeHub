package com.subhajitdas.c.edit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;
import com.subhajitdas.c.post.Post;
import com.subhajitdas.c.post.PostData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class EditPostActivity extends AppCompatActivity {
    private TextView mTitle, mDescription, mLanguage, mEditor, mFile;
    private FloatingActionButton mUploadButton;
    private ProgressDialog mProgress;
    private String FILENAME;
    public static final int TEXT_REQUEST = 1234;
    public PostData mPostData;

    private StorageReference mStorageProgramRef;
    private DatabaseReference mDatabaseProgramRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        String editorData = "";
        Intent gotIntent = getIntent();
        mPostData = new PostData();
        if (gotIntent != null) {
            //Whole post block is created here.
            mPostData.key = gotIntent.getStringExtra(Constants.KEY);
            mPostData.data.title = gotIntent.getStringExtra(Constants.TITLE);
            mPostData.data.userName = gotIntent.getStringExtra(Constants.USERNAME);
            mPostData.data.userId = gotIntent.getStringExtra(Constants.USERID);
            mPostData.data.date = gotIntent.getStringExtra(Constants.DATE);
            mPostData.data.likes = gotIntent.getStringExtra(Constants.LIKES);
            mPostData.data.fileUid = gotIntent.getStringExtra(Constants.FILEUID);
            mPostData.data.fileUri = gotIntent.getStringExtra(Constants.FILEURI);
            mPostData.data.description = gotIntent.getStringExtra(Constants.DESCRIPTION);
            mPostData.data.language = gotIntent.getStringExtra(Constants.LANGUAGE);
            editorData = gotIntent.getStringExtra(Constants.EDITOR);
        }
        //Toolbar work.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.edit_toolbar);
        mToolbar.setTitle("Edit Post");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        //UI components initialized.
        mTitle = (TextView) findViewById(R.id.edit_title);
        mDescription = (TextView) findViewById(R.id.edit_description);
        mLanguage = (TextView) findViewById(R.id.edit_lang);
        mEditor = (TextView) findViewById(R.id.edit_editor);
        mFile = (TextView) findViewById(R.id.edit_file);
        mUploadButton = (FloatingActionButton) findViewById(R.id.fab);
        mProgress = new ProgressDialog(this);

        //Filling in UI components.
        mTitle.setText(mPostData.data.title);
        mDescription.setText(mPostData.data.description);
        mLanguage.setText(mPostData.data.language);
        mEditor.setText(editorData);

        //Setting Language and File name for non editable.
        mLanguage.setFocusable(false);
        mLanguage.setFocusableInTouchMode(false);
        mFile.setFocusable(false);
        mFile.setFocusableInTouchMode(false);

        //Getting a random ID for the file.
        FILENAME = randomID();

        //Setting the upload paths.
        mStorageProgramRef = FirebaseStorage.getInstance().getReference().child(Constants.PROGRAMS);  //TODO change branch for uploading text file (During Update work)
        mDatabaseProgramRef = FirebaseDatabase.getInstance().getReference().child(Constants.PROGRAM); // TODO change branch for uploading data (During Update work)
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Handling upload button events.
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(mTitle.getText())) {         //Title is not empty.
                    if (!TextUtils.isEmpty(mEditor.getText())) {    //Editor is not empty.
                        //Setting up Progress Dialog .
                        mProgress.setTitle("Please wait");
                        mProgress.setMessage("Changing your content");
                        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgress.setIndeterminate(false);
                        mProgress.setMax(100);
                        mProgress.show();
                        mProgress.setCancelable(false);
                        // Deleting the old file.
                        mStorageProgramRef.child(mPostData.data.fileUid + ".txt").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mProgress.setProgress(20);
                                //Starting the upload work.
                                mStorageProgramRef.child(FILENAME + ".txt")
                                        .putFile(makeTextFile())                                                           // makeTextFile - Makes a txt file from editor text.
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                Post uploadData = makePostData(taskSnapshot.getDownloadUrl().toString());  // Making data block for the post.
                                                mPostData.data=uploadData;
                                                mProgress.setProgress(95);
                                                mDatabaseProgramRef.child(mPostData.key).setValue(uploadData).addOnCompleteListener(new OnCompleteListener<Void>() {  //Pusing post data.
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mProgress.setProgress(100);
                                                        mProgress.dismiss();
                                                        setBackData();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (Build.VERSION.SDK_INT >= 21) {
                                            Snackbar.make(findViewById(R.id.edit_coordinator),
                                                    "Upload failed.",
                                                    Snackbar.LENGTH_LONG).show();
                                        } else
                                            Toast.makeText(EditPostActivity.this, "Upload failed.", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (Build.VERSION.SDK_INT >= 21) {
                                    Snackbar.make(findViewById(R.id.edit_coordinator),
                                            e.getMessage(),
                                            Snackbar.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(EditPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        if (Build.VERSION.SDK_INT >= 21) {
                            Snackbar.make(findViewById(R.id.edit_coordinator),
                                    "Nothing to upload.",
                                    Snackbar.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(EditPostActivity.this, "Nothing to upload.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (Build.VERSION.SDK_INT >= 21) {
                        Snackbar.make(findViewById(R.id.edit_coordinator),
                                "Please give some Title.",
                                Snackbar.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(EditPostActivity.this, "Please give some Title.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // To handle file choosing event.
        mFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileIntent("text/plain");             // Method which works on Samsung devices and many more devices.
            }
        });

        // To handle the language selector.
        mLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String langList[] = {Constants.C, Constants.CPP, Constants.JAVA, Constants.PYTHON};
                AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
                builder.setTitle("Pick a language")
                        .setItems(langList, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mLanguage.setText(langList[which]);
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);
                dialog.show();
            }
        });

    }

    //To create the data block which will be uploaded.
    private Post makePostData(String url) {

        //Making the data block.
        Post returnData = new Post();
        returnData.title = mTitle.getText().toString();
        returnData.likes = mPostData.data.likes;
        returnData.userId = mPostData.data.userId;
        returnData.userName = mPostData.data.userName;
        returnData.fileUid = FILENAME;
        returnData.description = mDescription.getText().toString();
        returnData.date = mPostData.data.date;
        returnData.language = mLanguage.getText().toString();
        returnData.fileUri = url;
        return returnData;
    }

    //To make a txt file with the text from editor.
    private Uri makeTextFile() {

        FileOutputStream fos = null;
        try {
            fos = openFileOutput("tempFile", MODE_PRIVATE);      //Making file.
            fos.write(mEditor.getText().toString().getBytes());  //Writing data.
        } catch (IOException e) {
            if (Build.VERSION.SDK_INT >= 21) {
                Snackbar.make(findViewById(R.id.edit_coordinator),
                        "File Creation Error.",
                        Snackbar.LENGTH_LONG).show();
            } else
                Toast.makeText(EditPostActivity.this, "File Creation Error.", Toast.LENGTH_LONG).show();

        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                if (Build.VERSION.SDK_INT >= 21) {
                    Snackbar.make(findViewById(R.id.edit_coordinator),
                            "File Creation Error.",
                            Snackbar.LENGTH_LONG).show();
                } else
                    Toast.makeText(EditPostActivity.this, "File Creation Error.", Toast.LENGTH_LONG).show();
            }
        }

        // Getting the file Uri and sending it back.
        File uploadFile = new File(getFilesDir(), "tempFile");
        Uri uri = Uri.fromFile(uploadFile);
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //After a file is chosen.
        if (requestCode == TEXT_REQUEST && resultCode == RESULT_OK && data != null) {
            File uploadFile = new File(data.getData().getPath());           // Getting the file.
            mFile.setText(uploadFile.getName());
            displayFile(uploadFile);                                        // Displaying the contents of the file in editor.
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                Snackbar.make(findViewById(R.id.edit_coordinator),
                        "Sorry file not chosen.",
                        Snackbar.LENGTH_LONG).show();
            } else
                Toast.makeText(EditPostActivity.this, "Sorry file not chosen.", Toast.LENGTH_LONG).show();
        }
    }

    private void displayFile(File localFile) {
        // A file with the name of localFile is read.
        FileInputStream fis = null;
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(mEditor.getText());
        try {
            fis = new FileInputStream(localFile);
            int read = -1;
            //Reading the whole file till end.
            while ((read = fis.read()) != -1) {
                stringBuffer.append((char) read);
            }
            //Finally setting the content of the file in textView
            mEditor.setText(stringBuffer.toString());
        } catch (IOException e) {
            //Error msg shown.
            if (Build.VERSION.SDK_INT >= 21) {
                Snackbar.make(findViewById(R.id.edit_coordinator),
                        "File reading error",
                        Snackbar.LENGTH_LONG).show();
            } else
                Toast.makeText(this, "File reading error", Toast.LENGTH_LONG).show();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    //Error msg shown.
                    if (Build.VERSION.SDK_INT >= 21) {
                        Snackbar.make(findViewById(R.id.edit_coordinator),
                                "File reading error",
                                Snackbar.LENGTH_LONG).show();
                    } else
                        Toast.makeText(this, "File reading error", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // To choose a file to upload.
    public void openFileIntent(String minmeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, TEXT_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    // Function which generates a random string.
    protected static String randomID() {
        // creating UUID
        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        // checking the value of random UUID
        return uid.randomUUID().toString();
    }

    //Recording data on pause.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.TITLE, mTitle.getText().toString());
        outState.putString(Constants.DESCRIPTION, mDescription.getText().toString());
        outState.putString(Constants.LANGUAGE, mLanguage.getText().toString());
        outState.putString(Constants.FILEUID, mFile.getText().toString());
        outState.putString(Constants.EDITOR, mEditor.getText().toString());
    }

    // Restoring data after resume .
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mTitle.setText(savedInstanceState.getString(Constants.TITLE));
        mDescription.setText(savedInstanceState.getString(Constants.DESCRIPTION));
        mLanguage.setText(savedInstanceState.getString(Constants.LANGUAGE));
        mFile.setText(savedInstanceState.getString(Constants.FILEUID));
        mEditor.setText(savedInstanceState.getString(Constants.EDITOR));
    }

    //If the hardware back button is pressed.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setBackData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            // If the back toolbar back is pressed.
            case android.R.id.home:
                setBackData();
                return true;
        }
        return false;
    }

    // To set the returning back intent.
    public void setBackData() {
        Intent backData = new Intent();
        backData.putExtra(Constants.KEY, mPostData.key);
        backData.putExtra(Constants.TITLE, mPostData.data.title);
        backData.putExtra(Constants.DESCRIPTION, mPostData.data.description);
        backData.putExtra(Constants.LANGUAGE, mPostData.data.language);
        backData.putExtra(Constants.USERNAME, mPostData.data.userName);
        backData.putExtra(Constants.USERID, mPostData.data.userId);
        backData.putExtra(Constants.DATE, mPostData.data.date);
        backData.putExtra(Constants.LIKES, mPostData.data.likes);
        backData.putExtra(Constants.FILEUID, mPostData.data.fileUid);
        backData.putExtra(Constants.FILEURI, mPostData.data.fileUri);
        setResult(RESULT_OK, backData);
        finish();                   //Ends the activity and returns to the parent.
    }
}
