package com.subhajitdas.c;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



public class LoginActivity extends AppCompatActivity {

    LoginFragment login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        if(findViewById(R.id.frag_container)!= null) {
            login = new LoginFragment();
            getFragmentManager().beginTransaction().add(R.id.frag_container,login).commit();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        login.onActivityResult(requestCode,resultCode,data);
    }
}
