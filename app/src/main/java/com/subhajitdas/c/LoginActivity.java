package com.subhajitdas.c;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        if(findViewById(R.id.frag_container)!= null) {
            LoginFragment login = new LoginFragment();
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
}
