package com.subhajitdas.c;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterFragment extends Fragment {

    EditText mEmail, mPassword, mUsername;
    FloatingActionButton mGo;
    ImageView mBack;
    ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    private String EMAIL="email";
    private String PASSWORD="password";
    private String USERNAME = "username";
    private String TAG = "Jeetu";
    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");

        //----USER LOGIN LISTENER-----
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    mProgress.setMessage("Setting up your account ");
                    userRef.child(user.getUid()).child("username").setValue(mUsername.getText().toString());
                    userRef.child(user.getUid()).child("email").setValue(mEmail.getText().toString());
                    userRef.child(user.getUid()).child("password").setValue(mPassword.getText().toString());

                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mUsername.getText().toString())
                            .build();

                    user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mProgress.hide();

                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    });


                }

            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mUsername = (EditText) getActivity().findViewById(R.id.register_username);
        mEmail = (EditText) getActivity().findViewById(R.id.register_email);
        mPassword = (EditText) getActivity().findViewById(R.id.register_password);
        mGo = (FloatingActionButton) getActivity().findViewById(R.id.register_fab);
        mBack=(ImageView)getActivity().findViewById(R.id.back_image);
        mProgress=new ProgressDialog(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState!=null) {
            mUsername.setText(savedInstanceState.getString(USERNAME));
            mEmail.setText(savedInstanceState.getString(EMAIL));
            mPassword.setText(savedInstanceState.getString(PASSWORD));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress.setMessage("Creating Account");
                mProgress.show();
                mProgress.setCancelable(false);
                mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                if (!task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Cant create account.",Toast.LENGTH_SHORT).show();
                                    mProgress.hide();
                                }

                            }
                        });
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager tempManager = getActivity().getFragmentManager();
                tempManager.popBackStack();
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
        outState.putString(USERNAME,mUsername.getText().toString());
        outState.putString(EMAIL,mEmail.getText().toString());
        outState.putString(PASSWORD,mPassword.getText().toString());

        super.onSaveInstanceState(outState);
    }
}