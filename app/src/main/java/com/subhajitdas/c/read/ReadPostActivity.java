package com.subhajitdas.c.read;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;
import com.subhajitdas.c.edit.EditPostActivity;
import com.subhajitdas.c.post.PostData;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class ReadPostActivity extends AppCompatActivity {

    private TextView mTitle, mDescription, mDate, mPosterName, mPostContent, mEmptyView;
    private PostData mPostData = new PostData();
    private SwipeRefreshLayout mRefreshLayout;
    private File mLocalFile;
    private ImageView mLangView, mDp, mGetImageIcon, mShowImageChoosen;
    private Button mCmmtOk;
    private EditText mCmmtText;
    private RecyclerView mCmmtReycyclerView;
    private Uri mUploadImgUri;
    private ProgressDialog mProgress;

    private ArrayList<CmmtData> mDataSet;
    private ArrayList<String> mNotiSendId;
    private CmmtAdapter mCmmtAdapter = new CmmtAdapter(mDataSet);
    private final int REQUEST_CODE = 123;
    private Boolean uploadImg = false;

    private StorageReference mProgramFile, mCmmtImg;
    private DatabaseReference mUserRef, mCmmtRef, mPostRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_post);
        //Getting postData from Post Fragment.
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
                mPostData.data.comments = intent.getStringExtra(Constants.COMMENTS);
            } else {
                mPostData = new PostData();
            }
        }
        //Database is initialized.
        mProgramFile = FirebaseStorage.getInstance().getReference().child(Constants.PROGRAMS);
        mCmmtImg = FirebaseStorage.getInstance().getReference().child(Constants.CMMT_IMAGES).child(mPostData.key);
        mPostRef = FirebaseDatabase.getInstance().getReference().child(Constants.PROGRAM).child(mPostData.key).child(Constants.COMMENTS);

        //UI elements are initialized.
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mTitle = (TextView) findViewById(R.id.read_title);
        mDescription = (TextView) findViewById(R.id.read_description);
        mPosterName = (TextView) findViewById(R.id.read_poster_name);
        mPostContent = (TextView) findViewById(R.id.read_content);
        mDate = (TextView) findViewById(R.id.read_date);
        mLangView = (ImageView) findViewById(R.id.read_lang);
        mDp = (ImageView) findViewById(R.id.read_dp);
        mGetImageIcon = (ImageView) findViewById(R.id.read_cmmt_pic);
        mCmmtOk = (Button) findViewById(R.id.read_cmmt_ok);
        mCmmtText = (EditText) findViewById(R.id.read_cmmt_text);
        mCmmtReycyclerView = (RecyclerView) findViewById(R.id.read_recylerview);
        mEmptyView = (TextView) findViewById(R.id.no_cmmt_label);
        mShowImageChoosen = (ImageView) findViewById(R.id.cmmt_image_choose);
        mProgress = new ProgressDialog(this);
        mDataSet = new ArrayList<>();

        // Toolbar work done.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.read_toolbar);
        mToolbar.setTitle(mPostData.data.title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // UI elements are filled with postData according to the post postData.
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

        //Cmmt recycler view.
        mCmmtReycyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCmmtReycyclerView.setNestedScrollingEnabled(false);
        mCmmtReycyclerView.setHasFixedSize(false);
        mCmmtAdapter = new CmmtAdapter(mDataSet);
        mCmmtAdapter.setPostId(mPostData.key);

        mCmmtReycyclerView.setAdapter(mCmmtAdapter);
        mCmmtReycyclerView.setVisibility(View.INVISIBLE);
        //All the people who need notifications to be sent.
        mNotiSendId = new ArrayList<>();
        mNotiSendId.add(mPostData.data.userId);

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER);
        mCmmtRef = FirebaseDatabase.getInstance().getReference().child(Constants.COMMENT).child(mPostData.key);
        mCmmtRef.keepSynced(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        //Cmmts data.
        mCmmtRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CmmtData dataBlock = makeCmmtData(dataSnapshot);
                mDataSet.add(dataBlock);
                mCmmtAdapter.notifyItemInserted(mDataSet.size() - 1);
                //mCmmtReycyclerView.scrollToPosition(mDataSet.size() - 1);
                if (mDataSet.size() == 1) {
                    mCmmtReycyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.INVISIBLE);
                }
                //Getting ID's of people to whom noti will be sent.
                boolean foundId = false;
                for (int i = 0; i < mNotiSendId.size(); i++) {
                    if (dataBlock.retriveData.userId.equals(mNotiSendId.get(i))) {
                        foundId = true;
                    }
                }
                if (foundId == false) {
                    mNotiSendId.add(dataBlock.retriveData.userId);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int indexToChange = -1;
                for (int i = 0; i < mDataSet.size(); i++) {
                    if (mDataSet.get(i).key.equals(dataSnapshot.getKey())) {
                        indexToChange = i;
                        break;
                    }
                }

                if (indexToChange != -1) {
                    if (dataSnapshot.hasChild(Constants.CMMT_TEXT)) {
                        mDataSet.get(indexToChange).retriveData.cmmt_text = dataSnapshot.child(Constants.CMMT_TEXT).getValue().toString();
                    } else {
                        mDataSet.get(indexToChange).retriveData.cmmt_text = null;
                    }
                    mCmmtAdapter.notifyItemChanged(indexToChange);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int indexToRemove = -1;
                for (int i = 0; i < mDataSet.size(); i++) {
                    if (mDataSet.get(i).key.equals(dataSnapshot.getKey())) {
                        indexToRemove = i;
                        break;
                    }
                }

                if (indexToRemove != -1) {
                    mDataSet.remove(indexToRemove);
                    mCmmtAdapter.notifyItemRemoved(indexToRemove);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //No of cmmts .
            //Updating the value with each cmmt. or deletion.
        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    mPostData.data.comments = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //File is created in the app internal postData location.
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
        //Loading the profile image.
        mUserRef.child(mPostData.data.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Constants.DP_THUMB_URL)) {
                    String imageUrl = dataSnapshot.child(Constants.DP_THUMB_URL).getValue().toString();
                    RequestOptions posterOptions = new RequestOptions();
                    posterOptions.circleCrop();
                    posterOptions.placeholder(getResources().getDrawable(R.drawable.ic_avatar_black));
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .apply(posterOptions)
                            .into(mDp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        //Comment ok button handler.
        mCmmtOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!TextUtils.isEmpty(mCmmtText.getText().toString())) || uploadImg) {

                    final CmmtData.PostCmmt postCmmtBlock = new CmmtData.PostCmmt();
                    postCmmtBlock.name = mCurrentUser.getDisplayName();
                    postCmmtBlock.userId = mCurrentUser.getUid();
                    postCmmtBlock.time = ServerValue.TIMESTAMP;

                    final String pushKey = mCmmtRef.push().getKey();
                    // Checking for text part
                    if (!TextUtils.isEmpty(mCmmtText.getText().toString()))
                        postCmmtBlock.cmmt_text = mCmmtText.getText().toString();
                    else
                        postCmmtBlock.cmmt_text = null;
                    //If an image is chosen.
                    if (uploadImg) {
                        mProgress.setTitle("Please wait");
                        mProgress.setMessage("Uploading your image");
                        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgress.setIndeterminate(false);
                        mProgress.setMax(100);
                        mProgress.show();
                        mProgress.setCancelable(false);
                        mCmmtImg.child(pushKey + ".jpg")            //Uploading image.
                                .putFile(mUploadImgUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        postCmmtBlock.cmmt_img_url = taskSnapshot.getDownloadUrl().toString();
                                        //Uploading data.
                                        mCmmtRef.child(pushKey).setValue(postCmmtBlock)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        int newCmmt = Integer.parseInt(mPostData.data.comments) + 1;
                                                        mPostData.data.comments = Integer.toString(newCmmt);
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child(Constants.PROGRAM)
                                                                .child(mPostData.key)
                                                                .child(Constants.COMMENTS)
                                                                .setValue(Integer.toString(newCmmt));   //Increasing cmmt number.
                                                        uploadImg = false;
                                                        //sending noti.
                                                        sendNoti(mCmmtText.getText().toString());
                                                        mCmmtText.setText(null);
                                                        mShowImageChoosen.setImageDrawable(null);
                                                        mShowImageChoosen.setVisibility(View.GONE);
                                                        mProgress.dismiss();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        uploadImg = false;
                                                        mProgress.dismiss();
                                                        if (Build.VERSION.SDK_INT >= 21)
                                                            Snackbar.make(findViewById(R.id.read_coordinator), "Sorry can't post your comment.", Snackbar.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(ReadPostActivity.this, "Sorry can't post your comment.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        uploadImg = false;
                                        mProgress.dismiss();
                                        if (Build.VERSION.SDK_INT >= 21)
                                            Snackbar.make(findViewById(R.id.read_coordinator), "Sorry can't post your comment.", Snackbar.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(ReadPostActivity.this, "Sorry can't post your comment.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                        double percentage = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        mProgress.setProgress((int) percentage);

                                    }
                                });
                    } else {
                        postCmmtBlock.cmmt_img_url = null;
                        //Uploading data.
                        mCmmtRef.child(pushKey).setValue(postCmmtBlock)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        int newCmmt = Integer.parseInt(mPostData.data.comments) + 1;
                                        mPostData.data.comments = Integer.toString(newCmmt);
                                        FirebaseDatabase.getInstance().getReference()
                                                .child(Constants.PROGRAM)
                                                .child(mPostData.key)
                                                .child(Constants.COMMENTS)
                                                .setValue(Integer.toString(newCmmt));   //Increasing cmmt number.
                                        //sending noti.
                                        sendNoti(mCmmtText.getText().toString());
                                        mCmmtText.setText(null);
                                        mShowImageChoosen.setImageDrawable(null);
                                        mShowImageChoosen.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (Build.VERSION.SDK_INT >= 21)
                                            Snackbar.make(findViewById(R.id.read_coordinator), "Sorry can't post your comment.", Snackbar.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(ReadPostActivity.this, "Sorry can't post your comment.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                } else {
                    if (Build.VERSION.SDK_INT >= 21)
                        Snackbar.make(findViewById(R.id.read_coordinator), "Can't post empty comment.", Snackbar.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ReadPostActivity.this, "Can't post empty comment.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Getting image for cmmt.
        mGetImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Choosing image.
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAutoZoomEnabled(true)
                        .start(ReadPostActivity.this);
            }
        });


    }

    private void sendNoti(String msg) {
        //If there is no cmmt text.
        if(TextUtils.isEmpty(msg)){
            msg = " an image ";
        }else{
            msg="  \""+msg+"\"  ";
        }

        //Shortening the text.
        String tempTitleText = mPostData.data.title;
        if (tempTitleText.length() > 40) {
            tempTitleText = tempTitleText.substring(0, 40) + "...";
        }
        String notiText = null;

        for (int i = 0; i < mNotiSendId.size(); i++) {
            //For not sending notification to myself.
            if (!mNotiSendId.get(i).equals(mCurrentUser.getUid())) {
                //noti sent to owner.
                int index = mNotiSendId.indexOf(mCurrentUser.getUid());
                if (mNotiSendId.get(i).equals(mPostData.data.userId)) {
                    notiText = mCurrentUser.getDisplayName() + " commented " +  msg + "on your post \"" + tempTitleText + "\"";
                } else if (index < mNotiSendId.size() && index >= 0) {
                    notiText = mCurrentUser.getDisplayName() + " replied " + msg + "on post \"" + tempTitleText + "\"";
                } else {
                    notiText = mCurrentUser.getDisplayName() + " commented "+ msg +  "on post \"" + tempTitleText + "\"";
                }
                //Uploading the data.
                HashMap<String, String> uploadNoti = new HashMap<>();
                uploadNoti.put(Constants.NOTI_TEXT, notiText);
                uploadNoti.put(Constants.NOTI_READ, "false");
                uploadNoti.put(Constants.NOTI_TYPE, "comment");
                uploadNoti.put(Constants.NOTI_POST_KEY, mPostData.key);
                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.NOTI)
                        .child(mNotiSendId.get(i))
                        .push()
                        .setValue(uploadNoti);
            }
        }
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
            case android.R.id.home:
                finish();
                break;
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
                    editIntent.putExtra(Constants.COMMENTS, mPostData.data.comments);
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

                                                    //Deleting from COMMENT branch
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(Constants.COMMENT)
                                                            .child(mPostData.key)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getValue() != null) {
                                                                        FirebaseDatabase.getInstance().getReference()
                                                                                .child(Constants.COMMENT)
                                                                                .child(mPostData.key)
                                                                                .removeValue();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                    /*
                                                    FOLDER DEL NOT SUPPORTED SO THINK OF THIS PART IN FUTURE
                                                    // Deleting the images comment folder
                                                    FirebaseStorage.getInstance().getReference()
                                                            .child(Constants.CMMT_IMAGES)
                                                            .child(mPostData.key)
                                                            .delete()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        Log.e("del","del was done");
                                                                    }
                                                                    else{
                                                                        Log.e("del","del was not done");
                                                                    }
                                                                }
                                                            });
                                                    */

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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
                    mPostData.data.comments = data.getStringExtra(Constants.COMMENTS);
                } else {
                    mPostData = new PostData();
                }
            }
            // UI elements are filled with postData according to the post postData.
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

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mUploadImgUri = result.getUri();
                uploadImg = true;
                mShowImageChoosen.setVisibility(View.VISIBLE);
                mShowImageChoosen.setImageURI(mUploadImgUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                if (Build.VERSION.SDK_INT >= 21)
                    Snackbar.make(findViewById(R.id.read_coordinator), error.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Toast.makeText(ReadPostActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                if (Build.VERSION.SDK_INT >= 21)
                    Snackbar.make(findViewById(R.id.read_coordinator), "Sorry unable to choose image.", Snackbar.LENGTH_LONG).show();
                else
                    Toast.makeText(ReadPostActivity.this, "Sorry unable to choose image.", Toast.LENGTH_LONG).show();
            }
        }

    }

    private CmmtData makeCmmtData(DataSnapshot dataSnapshot) {

        CmmtData returnData = new CmmtData();
        returnData.retriveData = new CmmtData.RetriveCmmt();
        returnData.key = dataSnapshot.getKey();


        if (dataSnapshot.hasChild(Constants.NAME)) {
            returnData.retriveData.name = dataSnapshot.child(Constants.NAME).getValue().toString();
        } else {
            returnData.retriveData.name = null;
        }

        if (dataSnapshot.hasChild(Constants.TIME)) {
            String timestamp = dataSnapshot.child(Constants.TIME).getValue().toString();
            Long temp = Long.parseLong(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
            returnData.retriveData.time = sdf.format(new Date(temp));
        } else {
            returnData.retriveData.time = null;
        }

        if (dataSnapshot.hasChild(Constants.USERID)) {
            returnData.retriveData.userId = dataSnapshot.child(Constants.USERID).getValue().toString();
        } else {
            returnData.retriveData.userId = null;
        }

        if (dataSnapshot.hasChild(Constants.CMMT_TEXT)) {
            returnData.retriveData.cmmt_text = dataSnapshot.child(Constants.CMMT_TEXT).getValue().toString();
        } else {
            returnData.retriveData.cmmt_text = null;
        }

        if (dataSnapshot.hasChild(Constants.CMMT_IMG_URL)) {
            returnData.retriveData.cmmt_img_url = dataSnapshot.child(Constants.CMMT_IMG_URL).getValue().toString();
        } else {
            returnData.retriveData.cmmt_img_url = null;
        }
        return returnData;
    }

}
