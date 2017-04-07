package com.subhajitdas.c;


import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NavDrawerFragment extends Fragment {

    private ImageView mUserImage;
    private TextView mUserName, mUserEmail;
    private ListView mlistView;
    private DrawerLayout mDrawerLayout;
    private ProgressDialog mProgress;
    private DownloadManager downloadManager;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mUpdateRef;


    public NavDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUpdateRef = FirebaseDatabase.getInstance().getReference().child("update");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nav_drawer, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mlistView = (ListView) getActivity().findViewById(R.id.nav_list);
        mUserImage = (ImageView) getActivity().findViewById(R.id.acct_image);
        mUserName = (TextView) getActivity().findViewById(R.id.user_display_name);
        mUserEmail = (TextView) getActivity().findViewById(R.id.user_display_email);
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mProgress = new ProgressDialog(getActivity());
        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

    }

    @Override
    public void onStart() {
        super.onStart();

        mUserImage.setImageResource(R.drawable.ic_avatar);
        mUserName.setText(mCurrentUser.getDisplayName());
        mUserEmail.setText(mCurrentUser.getEmail());

        ArrayList<Options> option = new ArrayList<Options>();
        CustomArrayAdapter adapter = new CustomArrayAdapter(getActivity(), option);
        mlistView.setAdapter(adapter);

        int navIcon[] = {R.drawable.ic_home_black_24px,
                R.drawable.ic_bookmark_gray_24px,
                R.drawable.ic_feedback_black_24px,
                R.drawable.ic_update_black_24px,
                R.drawable.ic_sign_out_black_24px};
        String navText[] = {"Posts", "Bookmarks", "Feedback", "Update", "Log Out"};

        for (int i = 0; i<=4; i++) {
            Options item = new Options(navIcon[i], navText[i]);
            adapter.add(item);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mDrawerLayout.closeDrawers();
                        getActivity().getFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        ListFragment list = new ListFragment();
                        getActivity().getFragmentManager().beginTransaction().replace(R.id.main_activity_frag_container, list).commit();
                        break;
                    case 1:
                        mDrawerLayout.closeDrawers();
                        BookmarksFragment bookmarksFragment = new BookmarksFragment();
                        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
                        transaction.replace(R.id.main_activity_frag_container, bookmarksFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 2:
                        mDrawerLayout.closeDrawers();
                        FeedbackFragment feedbackFragment = new FeedbackFragment();
                        FragmentTransaction transaction2 = getActivity().getFragmentManager().beginTransaction();
                        transaction2.replace(R.id.main_activity_frag_container, feedbackFragment);
                        transaction2.addToBackStack(null);
                        transaction2.commit();
                        break;
                    case 3:
                        mProgress.setMessage("Checking for update");
                        mDrawerLayout.closeDrawers();
                        mProgress.setCancelable(false);
                        mProgress.show();
                        mUpdateRef.child("versionCode").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                int newVersion = Integer.parseInt(dataSnapshot.getValue().toString());
                                if (newVersion > BuildConfig.VERSION_CODE) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Do you want to download the new update." +
                                            "\nNew update contains bug fixes." +
                                            "\nDownload recommended").
                                            setTitle("Download Update");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            mUpdateRef.child("apkUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (Build.VERSION.SDK_INT >= 23) {
                                                        if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                                == PackageManager.PERMISSION_GRANTED) {
                                                            Uri fileUri = Uri.parse(dataSnapshot.getValue().toString());
                                                            DownloadManager.Request request = new DownloadManager.Request(fileUri);
                                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CodeHub_Update.apk");
                                                            request.setTitle("Downloading update");
                                                            downloadManager.enqueue(request);

                                                        } else {
                                                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                                        }
                                                    }
                                                    else {
                                                        Uri fileUri = Uri.parse(dataSnapshot.getValue().toString());
                                                        DownloadManager.Request request = new DownloadManager.Request(fileUri);
                                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CodeHub_Update.apk");
                                                        request.setTitle("Downloading update");
                                                        downloadManager.enqueue(request);
                                                    }


                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(getActivity(), "Cannot be downloaded", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        }
                                    });

                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });


                                    AlertDialog dialog = builder.create();
                                    mProgress.dismiss();
                                    dialog.show();

                                } else {
                                    mProgress.dismiss();
                                    Toast.makeText(getActivity(), "No Update available", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                mProgress.dismiss();
                                Toast.makeText(getActivity(), "Sorry can't check for update", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 4:
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences loginState = getActivity().getSharedPreferences(Constants.LOGIN_STATE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = loginState.edit();
                        editor.putInt(Constants.LOGIN_STATE, 0);
                        editor.apply();
                        mDrawerLayout.closeDrawers();
                        Intent intent = new Intent(getActivity(), com.subhajitdas.c.LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    default:
                        Toast.makeText(getActivity(), "Feature not added " + position, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //resume tasks needing this permission
        }
    }
}
