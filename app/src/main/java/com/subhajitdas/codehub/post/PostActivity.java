package com.subhajitdas.codehub.post;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.subhajitdas.codehub.R;


public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FrameLayout mMainContainer, mNavDrawerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Layout containers for fragments.
        mMainContainer = (FrameLayout) findViewById(R.id.main_container);
        mNavDrawerContainer = (FrameLayout) findViewById(R.id.nav_drawer_container);

        //App Toolbar work.
        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_48px);
        mToolbar.setTitle("Posts");
        setSupportActionBar(mToolbar);

        /*Navigation drawer work
            Needed for the Toolbar icon to respond on touch and
            automatically change icon with animations.
                Note:- DON'T remove the deprecated method since it helps
                       in changing the icon.
         */
        DrawerLayout drawer =(DrawerLayout)findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Attaching the fragments.
        //      Main list of posts fragment attached.
        if (findViewById(R.id.main_container) != null) {
            PostFragment2 postFragment = new PostFragment2();
            getFragmentManager().beginTransaction().add(R.id.main_container, postFragment).commit();
        }
        //      Navigation drawer fragment to be attached.

    }
}
