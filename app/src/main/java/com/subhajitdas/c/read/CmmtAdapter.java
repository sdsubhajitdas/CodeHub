package com.subhajitdas.c.read;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Subhajit Das on 09-07-2017.
 */

public class CmmtAdapter extends RecyclerView.Adapter<CmmtAdapter.ViewHolder> {
    private ArrayList<CmmtData> mDataSet;
    private ArrayList<UserImageLinks> mUserImgLink;
    private DatabaseReference mCmmtRef;
    private StorageReference mCmmtImgRef, mDownloadImgRef;

    private String mCurrentUserId, mCurrentPostId;

    public void setPostId(String postId) {
        mCurrentPostId = postId;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, text, time, edit, del;
        private ImageView dp, cmmtImg;
        private Context context;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.cmmt_name);
            text = (TextView) v.findViewById(R.id.cmmt_text);
            time = (TextView) v.findViewById(R.id.cmmt_time);
            del = (TextView) v.findViewById(R.id.cmmt_del);
            edit = (TextView) v.findViewById(R.id.cmmt_edit);
            dp = (ImageView) v.findViewById(R.id.cmmt_dp);
            cmmtImg = (ImageView) v.findViewById(R.id.cmmt_image);
            context = v.getContext();
        }
    }

    private class UserImageLinks {
        private String userId;
        private String imageUrl;
    }



    // Provide a suitable constructor (depends on the kind of dataset)
    public CmmtAdapter(ArrayList<CmmtData> myDataset) {
        mDataSet = new ArrayList<>();
        mDataSet = myDataset;
        mUserImgLink = new ArrayList<>();
        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER);
        mUserRef.keepSynced(true);

        mCmmtImgRef = FirebaseStorage.getInstance().getReference().child(Constants.CMMT_IMAGES);
        mCmmtRef = FirebaseDatabase.getInstance().getReference().child(Constants.COMMENT);
        mCmmtRef.keepSynced(true);
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.hasChild(Constants.DP_THUMB_URL)) {
                    UserImageLinks data = new UserImageLinks();
                    data.userId = dataSnapshot.getKey();
                    data.imageUrl = dataSnapshot.child(Constants.DP_THUMB_URL).getValue().toString();
                    mUserImgLink.add(data);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CmmtAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cmmt_layout, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int place) {
        final int position = place;
        //Setting up the name.
        if (mDataSet.get(position).retriveData.name != null) {
            holder.name.setText(mDataSet.get(position).retriveData.name);
        } else {
            holder.name.setText("No Name Found");
        }

        //Setting up time.
        if (mDataSet.get(position).retriveData.time != null) {
            holder.time.setText(mDataSet.get(position).retriveData.time);
        } else {
            holder.time.setText("No Time Found");
        }

        // Setting up the profile picture.
        holder.dp.setImageDrawable(holder.context.getResources().getDrawable(R.drawable.ic_avatar_black));
        if (mDataSet.get(position).retriveData.userId != null) {
            int index = -1;
            for (int i = 0; i < mUserImgLink.size(); i++) {
                if (mDataSet.get(position).retriveData.userId.equals(mUserImgLink.get(i).userId)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                RequestOptions dpOptions = new RequestOptions();
                dpOptions.placeholder(R.drawable.ic_avatar_black);
                dpOptions.circleCrop();
                Glide.with(holder.context)
                        .load(mUserImgLink.get(index).imageUrl)
                        .apply(dpOptions)
                        .into(holder.dp);
            }
        }

        //Setting up cmmt text.
        if (mDataSet.get(position).retriveData.cmmt_text != null) {
            holder.text.setText(mDataSet.get(position).retriveData.cmmt_text);
        } else {
            holder.text.setVisibility(View.GONE);
        }

        //Setting up cmmt image.
        if (mDataSet.get(position).retriveData.cmmt_img_url != null) {
            holder.cmmtImg.setVisibility(View.VISIBLE);
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, holder.context.getResources().getDisplayMetrics());
            holder.cmmtImg.getLayoutParams().height = dimensionInDp;
            holder.cmmtImg.requestLayout();

            RequestOptions cmmtOptions = new RequestOptions();
            cmmtOptions.placeholder(R.drawable.ic_photo);

            Glide.with(holder.context)
                    .load(mDataSet.get(position).retriveData.cmmt_img_url)
                    .apply(cmmtOptions)
                    .into(holder.cmmtImg);
        } else {
            holder.cmmtImg.setVisibility(View.GONE);
        }

        //Setting up Edit and Del buttons.
        if (mDataSet.get(position).retriveData.userId != null) {
            if (!(mDataSet.get(position).retriveData.userId.equals(mCurrentUserId))) {
                holder.edit.setVisibility(View.GONE);
                holder.edit.setEnabled(false);
                holder.del.setVisibility(View.GONE);
                holder.del.setEnabled(false);
            }
        } else {
            holder.edit.setVisibility(View.GONE);
            holder.edit.setEnabled(false);
            holder.del.setVisibility(View.GONE);
            holder.del.setEnabled(false);
        }

        //Handling the edit cmmt.
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText input = new EditText(holder.context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                AlertDialog.Builder builder = new AlertDialog.Builder(holder.context);
                builder.setTitle("Edit your comment")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String editText = input.getText().toString();
                                if (!TextUtils.isEmpty(editText)) {
                                    mCmmtRef.child(mCurrentPostId)
                                            .child(mDataSet.get(position).key)
                                            .child(Constants.CMMT_TEXT)
                                            .setValue(editText);
                                } else {
                                    mCmmtRef.child(mCurrentPostId)
                                            .child(mDataSet.get(position).key)
                                            .child(Constants.CMMT_TEXT)
                                            .removeValue();
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.setView(input);
                if (mDataSet.get(position).retriveData.cmmt_text != null) {
                    input.setText(mDataSet.get(position).retriveData.cmmt_text);
                }
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //Handling the del cmmt.
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position >= 0 && position < getItemCount()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.context);
                    builder.setTitle("Delete alert !")
                            .setMessage("Are you sure you want to delete your comment ?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    if (mDataSet.get(position).retriveData.cmmt_img_url == null) {
                                        mCmmtRef.child(mCurrentPostId)
                                                .child(mDataSet.get(position).key)
                                                .removeValue();
                                    } else {
                                        Toast.makeText(holder.context, "Please wait deleting the picture", Toast.LENGTH_LONG).show();
                                        mCmmtImgRef.child(mCurrentPostId)
                                                .child(mDataSet.get(position).key + ".jpg")
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mCmmtRef.child(mCurrentPostId)
                                                                .child(mDataSet.get(position).key)
                                                                .removeValue();
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(holder.context, "Sorry your comment was not deleted", Toast.LENGTH_LONG).show();
                                                        dialog.dismiss();
                                                    }
                                                });
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(holder.context, "Please try later", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //To see the image bigger and download it
        holder.cmmtImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.context);
                builder.setTitle("Download image")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(holder.context, "Downloading your image.\nWe will notify you once its done.", Toast.LENGTH_LONG).show();
                                // File downloadImage = new File(Environment.DIRECTORY_DOWNLOADS);
                                // File downloadImage = holder.context.getExternalFilesDir()
                                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                final File downloadImage = new File(path, "/" + mDataSet.get(position).key + ".jpg");
                                mDownloadImgRef = FirebaseStorage.getInstance().getReferenceFromUrl(mDataSet.get(position).retriveData.cmmt_img_url);
                              /*  mCmmtImgRef.child(mCurrentPostId)
                                        .child(mDataSet.get(position).key+".jpg")
                                        */
                                mDownloadImgRef
                                        .getFile(downloadImage)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Toast.makeText(holder.context, "Image was saved in your gallery.", Toast.LENGTH_SHORT).show();
                                                MediaScannerConnection.scanFile(holder.context,
                                                        new String[]{downloadImage.getPath()}, null,
                                                        new MediaScannerConnection.OnScanCompletedListener() {

                                                            public void onScanCompleted(String path, Uri uri) {
                                                                Log.i("TAG", "Finished scanning " + path);
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(holder.context, "Sorry cant save your image.", Toast.LENGTH_SHORT).show();
                                                Log.e("Message", e.getMessage());
                                            }
                                        });

                                Log.e("path", mDownloadImgRef.getPath());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


                View view = LayoutInflater.from(holder.context)
                        .inflate(R.layout.see_cmmt_img, null);

                builder.setView(view);
                AlertDialog dialog = builder.create();
                ImageView showImage = (ImageView) view.findViewById(R.id.show_img);

                if (mDataSet.get(position).retriveData.cmmt_img_url != null) {
                    dialog.show();
                    Glide.with(holder.context)
                            .load(mDataSet.get(position).retriveData.cmmt_img_url)
                            .into(showImage);
                }
            }
        });

        //If comment name needs to change
        if (mCurrentUserId.equals(mDataSet.get(position).retriveData.userId)) {
            changeCommentName(position);

        }
    }

    private void changeCommentName(int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (!mDataSet.get(position).retriveData.name.equals(user.getDisplayName())) {
            mCmmtRef.child(mCurrentPostId)
                    .child(mDataSet.get(position).key)
                    .child(Constants.NAME)
                    .setValue(user.getDisplayName());
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
