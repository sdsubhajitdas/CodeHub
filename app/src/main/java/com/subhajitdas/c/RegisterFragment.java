package com.subhajitdas.c;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText mEmail, mPassword, mUsername;
    private Button mRegister;
    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

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

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constants.USER);

        // User login listener
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    mProgress.setMessage("Setting up your account ");
                    userRef.child(user.getUid()).child(Constants.USERNAME).setValue(mUsername.getText().toString());
                    userRef.child(user.getUid()).child(Constants.EMAIL).setValue(mEmail.getText().toString());
                    userRef.child(user.getUid()).child(Constants.PASSWORD).setValue(mPassword.getText().toString());

                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mUsername.getText().toString())
                            .build();

                    user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgress.dismiss();
                                // Changing activity.
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
        mRegister = (Button) getActivity().findViewById(R.id.register_button);
        mProgress = new ProgressDialog(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restored data is recovered.
        if (savedInstanceState != null) {
            mUsername.setText(savedInstanceState.getString(Constants.USERNAME));
            mEmail.setText(savedInstanceState.getString(Constants.EMAIL));
            mPassword.setText(savedInstanceState.getString(Constants.PASSWORD));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Data not empty
                if (!(TextUtils.isEmpty(mEmail.getText().toString())) &&
                        !(TextUtils.isEmpty(mPassword.getText().toString())) &&
                        !(TextUtils.isEmpty(mUsername.getText().toString()))) {

                    mProgress.setMessage("Creating Account");
                    mProgress.show();
                    mProgress.setCancelable(false);
                    mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    if (!task.isSuccessful()) {
                                        if (Build.VERSION.SDK_INT >= 21)
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Cant create account", Snackbar.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(getActivity(), "Cant create account", Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });
                } else {
                    if (Build.VERSION.SDK_INT >= 21)
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Please fill in details", Snackbar.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Please fill in details", Toast.LENGTH_SHORT).show();
                }
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
        // Storing the data before pausing.
        outState.putString(Constants.USERNAME, mUsername.getText().toString());
        outState.putString(Constants.EMAIL, mEmail.getText().toString());
        outState.putString(Constants.PASSWORD, mPassword.getText().toString());
        super.onSaveInstanceState(outState);
    }
}