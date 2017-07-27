package com.subhajitdas.c.post;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;
import com.subhajitdas.c.about.AboutActivity;
import com.subhajitdas.c.login.LoginActivity;
import com.subhajitdas.c.profile.ProfileActivity;


public class PostActivity extends AppCompatActivity {

    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;
    private ImageView mCoverImg, mProfileImg;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUserRef, mUpdateRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Layout containers for fragments.
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setCheckedItem(R.id.nav_posts);
        // Nav Header fields postData.
        View layout = mNavView.getHeaderView(0);
        TextView emailField = (TextView) layout.findViewById(R.id.nav_email);
        TextView nameField = (TextView) layout.findViewById(R.id.nav_name);
        mProfileImg = (ImageView) layout.findViewById(R.id.nav_profile_image);
        mCoverImg = (ImageView) layout.findViewById(R.id.nav_cover_image);
        emailField.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        nameField.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        //App Toolbar work.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_48px);
        mToolbar.setTitle("Posts");
        setSupportActionBar(mToolbar);

        mProgressDialog = new ProgressDialog(this);

        /*Navigation drawer work
            Needed for the Toolbar icon to respond on touch and
            automatically change icon with animations.
                Note:- DON'T remove the deprecated method since it helps
                       in changing the icon.
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();


        //Attaching the fragments.
        //      Main list of posts fragment attached.
        if (findViewById(R.id.main_container) != null) {
            PostFragment postFragment = new PostFragment();
            getFragmentManager().beginTransaction().add(R.id.main_container, postFragment).commit();
        }


        mUserRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mUpdateRef = FirebaseDatabase.getInstance().getReference().child(Constants.UPDATE);

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Constants.DP_THUMB_URL)) {
                    String dpUrl = dataSnapshot.child(Constants.DP_THUMB_URL).getValue().toString();
                    RequestOptions profileOptions = new RequestOptions();
                    profileOptions.circleCrop();
                    profileOptions.placeholder(R.drawable.ic_avatar_black);
                    Glide.with(getApplicationContext())
                            .load(dpUrl)
                            .apply(profileOptions)
                            .into(mProfileImg);
                }

                if (dataSnapshot.hasChild(Constants.COVER_THUMB_URL)) {
                    String coverUrl = dataSnapshot.child(Constants.COVER_THUMB_URL).getValue().toString();
                    RequestOptions coverOptions = new RequestOptions();
                    coverOptions.fitCenter();
                    Glide.with(getApplicationContext())
                            .load(coverUrl)
                            .into(mCoverImg);
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

        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {

                    Intent profileIntent = new Intent(PostActivity.this, ProfileActivity.class);
                    profileIntent.putExtra(Constants.ACTIVITY, Constants.POST_ACTIVITY);
                    profileIntent.putExtra(Constants.USERID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    startActivity(profileIntent);
                    mDrawerLayout.closeDrawers();

                } else if (id == R.id.nav_posts) {

                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    PostFragment postFragment = new PostFragment();
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, postFragment)
                            .commit();
                    mDrawerLayout.closeDrawers();

                } else if (id == R.id.nav_bookmarks) {
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    BookmarkFragment bookmarksFragment = new BookmarkFragment();
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, bookmarksFragment)
                            .commit();
                    mDrawerLayout.closeDrawers();

                } else if (id == R.id.nav_notifications) {
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    NotificationFragment notificationFragment = new NotificationFragment();
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, notificationFragment)
                            .commit();
                    mDrawerLayout.closeDrawers();
                } else if (id == R.id.nav_updates) {
                    PackageManager manager = getApplicationContext().getPackageManager();
                    PackageInfo info = null;
                    try {
                        info = manager.getPackageInfo(
                                getApplicationContext().getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    final int versionCode = info.versionCode;

                    mProgressDialog.setTitle("Checking for updates");
                    mProgressDialog.setMessage("Please wait while we check for updates");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    mUpdateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int gotVersionCode = Integer.parseInt(dataSnapshot.child(Constants.VERSION_CODE).getValue().toString());
                            String url19 = dataSnapshot.child(Constants.API19).getValue().toString();
                            String url21 = dataSnapshot.child(Constants.API21).getValue().toString();
                            if (gotVersionCode > versionCode) {
                                mProgressDialog.setMessage("Downloading your update");
                                if (Build.VERSION.SDK_INT >= 21) {
                                    mProgressDialog.dismiss();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(url21));
                                    startActivity(browserIntent);

                                } else if (Build.VERSION.SDK_INT <= 19) {
                                    mProgressDialog.dismiss();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(url19));
                                    startActivity(browserIntent);
                                }
                            } else {
                                Toast.makeText(PostActivity.this, "No updates available", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    mDrawerLayout.closeDrawers();

                } else if (id == R.id.nav_share) {
                    String textToShare = "Download CodeHub from our website.Join our app today!!\nLink:- https://code-hub.tk";
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share app");
                    intent.putExtra(Intent.EXTRA_TEXT, textToShare);
                    startActivity(Intent.createChooser(intent, "Share"));
                    mDrawerLayout.closeDrawers();
                } else if (id == R.id.nav_contact) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    String[] TO = {"info.codehub@gmail.com"};
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CodeHub Feedback");

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                        //finish();

                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(PostActivity.this, "There is no email client installed.", Toast.LENGTH_LONG).show();

                    }
                    mDrawerLayout.closeDrawers();
                } else if (id == R.id.nav_about) {
                    Intent aboutIntent = new Intent(PostActivity.this, AboutActivity.class);
                    startActivity(aboutIntent);
                    mDrawerLayout.closeDrawers();
                } else if (id == R.id.nav_sign_out) {

                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.LOGIN_STATE, Context.MODE_PRIVATE).edit();
                    editor.putInt(Constants.LOGIN_STATE, 0);
                    editor.apply();
                    Intent intent = new Intent(PostActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    mDrawerLayout.closeDrawers();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}

