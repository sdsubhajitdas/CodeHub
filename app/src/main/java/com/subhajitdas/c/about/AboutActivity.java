package com.subhajitdas.c.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

public class AboutActivity extends AppCompatActivity {

    ImageView mDp1, fb1, github1, web1, linkedin1, go1, go2, go3, go4, go5;
    ImageView mDp2, fb2, github2, web2, linkedin2;
    ImageView shareIcon,feedbackIcon,websiteIcon;
    TextView shareText1, shareText2,feedbackText1,feedbackText2,websiteText1,websiteText2;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //App Toolbar work.
        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        toolbar.setTitle("About the app");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        //1st card layout
        mDp1 = (ImageView) findViewById(R.id.dev_dp);
        fb1 = (ImageView) findViewById(R.id.fb);
        github1 = (ImageView) findViewById(R.id.github);
        web1 = (ImageView) findViewById(R.id.web);
        linkedin1 = (ImageView) findViewById(R.id.link);
        //2nd card layout
        mDp2 = (ImageView) findViewById(R.id.dev_dp2);
        fb2 = (ImageView) findViewById(R.id.fb2);
        github2 = (ImageView) findViewById(R.id.github2);
        web2 = (ImageView) findViewById(R.id.web2);
        linkedin2 = (ImageView) findViewById(R.id.link2);
        //Other elements initialization.
        go1 = (ImageView) findViewById(R.id.go_link1);
        go2 = (ImageView) findViewById(R.id.go_link2);
        go3 = (ImageView) findViewById(R.id.go_link3);
        go4 = (ImageView) findViewById(R.id.go_link4);
        go5 = (ImageView) findViewById(R.id.go_link5);
        shareIcon = (ImageView) findViewById(R.id.share_icon);
        feedbackIcon = (ImageView) findViewById(R.id.contact_icon);
        websiteIcon = (ImageView) findViewById(R.id.website_icon);

        shareText1 = (TextView) findViewById(R.id.share);
        shareText2 = (TextView) findViewById(R.id.share_text2);
        feedbackText1 = (TextView) findViewById(R.id.feedback);
        feedbackText2 = (TextView) findViewById(R.id.feedback_text2);
        websiteText1 = (TextView) findViewById(R.id.website);
        websiteText2 = (TextView) findViewById(R.id.website_text2);


        //Loading the DP.
        FirebaseDatabase.getInstance().getReference().child(Constants.DEV_DP)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        RequestOptions dpOptions = new RequestOptions();
                        dpOptions.placeholder(R.drawable.ic_avatar_black);
                        dpOptions.circleCrop();

                        Glide.with(AboutActivity.this)
                                .load(dataSnapshot.child("subhajit").getValue().toString())
                                .apply(dpOptions)
                                .into(mDp1);

                        Glide.with(AboutActivity.this)
                                .load(dataSnapshot.child("ivan").getValue().toString())
                                .apply(dpOptions)
                                .into(mDp2);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();

        //1st card click listeners.
        fb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://www.facebook.com/heatboy.loading");
            }
        });

        github1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/Jeetu95");
            }
        });

        web1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, "Link to be added in future.", Toast.LENGTH_SHORT).show();
            }
        });

        linkedin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://www.linkedin.com/in/subhajit-das-764742142/");
            }
        });

        //2nd card click listeners.
        fb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://www.facebook.com/karmakarivan");
            }
        });

        github2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/karmakarivan");
            }
        });

        web2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, "Link to be added in future.", Toast.LENGTH_SHORT).show();
            }
        });

        linkedin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://www.linkedin.com/in/ivan-karmakar-345803108/");
            }
        });

        //Other elements listeners.
        go1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/jd-alexander/LikeButton");
            }
        });

        go2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/bumptech/glide");
            }
        });

        go3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/ArthurHub/Android-Image-Cropper");
            }
        });

        go4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/zetbaitsu/Compressor");
            }
        });

        go5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://github.com/lgvalle/Material-Animations");
            }
        });

        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp();
            }
        });

        shareText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp();
            }
        });

        shareText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp();
            }
        });

        feedbackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        feedbackText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        feedbackText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        websiteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("http://code-hub.tk/");
            }
        });

        websiteText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("http://code-hub.tk/");
            }
        });

        websiteText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("http://code-hub.tk/");
            }
        });
    }

    //To open browser with a link
    public void openBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        startActivity(browserIntent);
    }

    //Send a text msg.
    public void shareApp() {
        String textToShare = "CodeHub is an app which tends to make a community of coders and help each other with understanding code. " +
                "Download CodeHub from our website.Join our app today!!\n" +
                "Link:- http://code-hub.tk";
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share app");
        intent.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    //Send mail to us.
    public void sendFeedback() {
        String[] TO = {"info.codehub@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "CodeHub Feedback");

        try {
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
            //finish();

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AboutActivity.this, "There is no email client installed.", Toast.LENGTH_LONG).show();

        }
    }

}
