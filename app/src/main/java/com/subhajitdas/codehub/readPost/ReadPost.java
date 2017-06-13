package com.subhajitdas.codehub.readPost;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.subhajitdas.codehub.R;

public class ReadPost extends AppCompatActivity {
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_post);

        mToolbar = (Toolbar) findViewById(R.id.read_toolbar);
        mToolbar.setTitle("Some random title which ");
        mToolbar.setSubtitle("Some random description which is very big in size so i ");
        setSupportActionBar(mToolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }
}
