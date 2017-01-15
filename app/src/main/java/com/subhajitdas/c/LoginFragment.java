package com.subhajitdas.c;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    private EditText mEmail, mPassword;
    private FloatingActionButton mGo;
    private ProgressDialog mProgress;
    TextView mRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String TAG = "Jeetu";
    private String EMAIL="email";
    private String PASSWORD="password";

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //----NETWORK STATE CHECKING----
        if(savedInstanceState==null)
        {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isConnected)
                Toast.makeText(getActivity(), "Sorry no network connection", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getActivity(), "Network Established", Toast.LENGTH_LONG).show();
        }

        //----USER LOGIN LISTENER-----
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    mProgress.hide();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmail = (EditText) getActivity().findViewById(R.id.login_email);
        mPassword = (EditText) getActivity().findViewById(R.id.login_password);
        mGo = (FloatingActionButton) getActivity().findViewById(R.id.login_fab);
        mRegister=(TextView)getActivity().findViewById(R.id.register);
        mProgress=new ProgressDialog(getActivity());


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        // TODO Remove this code later
        // TODO Use Shared preferences to store the last used user data
        mEmail.setText("jeetudas95@gmail.com");
        mPassword.setText("123456");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            mEmail.setText(savedInstanceState.getString(EMAIL));
            mPassword.setText(savedInstanceState.getString(PASSWORD));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterFragment register = new RegisterFragment();
                FragmentTransaction tempTransaction = getActivity().getFragmentManager().beginTransaction();
                tempTransaction.replace(R.id.frag_container,register);
                tempTransaction.addToBackStack(null);
                tempTransaction.commit();
            }
        });

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setMessage("Logging in");
                mProgress.show();
                mProgress.setCancelable(false);

                mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                if (!task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Cant sign in.", Toast.LENGTH_SHORT).show();
                                    mProgress.hide();
                                }


                            }
                        });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        String CHECK_INTERNET = "InternetStatusCheck";
        outState.putBoolean(CHECK_INTERNET,false);
        outState.putString(EMAIL,mEmail.getText().toString());
        outState.putString(PASSWORD,mPassword.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
