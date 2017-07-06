package com.subhajitdas.c.post;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;
import com.subhajitdas.c.login.LoginActivity;
import com.subhajitdas.c.profile.ProfileActivity;


public class PostActivity extends AppCompatActivity {

    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Layout containers for fragments.
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setCheckedItem(R.id.nav_posts);
        // Nav Header fields data.
        View layout =mNavView.getHeaderView(0);
        TextView emailField = (TextView) layout.findViewById(R.id.nav_email);
        TextView nameField = (TextView) layout.findViewById(R.id.nav_name);
        emailField.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        nameField.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        //App Toolbar work.
        Toolbar mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_48px);
        mToolbar.setTitle("Posts");
        setSupportActionBar(mToolbar);

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
        /*
        //      Navigation drawer fragment to be attached.
        if(findViewById(R.id.nav_drawer_container)!=null){
            NavigationDrawerFragment navigationDrawerFragment = new NavigationDrawerFragment();
            getFragmentManager().beginTransaction().add(R.id.nav_drawer_container,navigationDrawerFragment).commit();
        }*/

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
                    profileIntent.putExtra(Constants.ACTIVITY,Constants.POST_ACTIVITY);
                    profileIntent.putExtra(Constants.USERID,FirebaseAuth.getInstance().getCurrentUser().getUid());
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

                } else if (id == R.id.nav_updates) {

                    String url = "http://www.google.com";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                    mDrawerLayout.closeDrawers();

                } else if (id == R.id.nav_share) {
                    String textToShare = "Testing for android share intent.\nLink:- www.google.com \nPlease be successfull";
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "A Sample share intent");
                    intent.putExtra(Intent.EXTRA_TEXT, textToShare);
                    startActivity(Intent.createChooser(intent, "Share"));
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

