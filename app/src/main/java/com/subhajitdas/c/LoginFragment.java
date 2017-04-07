package com.subhajitdas.c;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginFragment extends Fragment {

    private EditText mEmail, mPassword;
    private ProgressDialog mProgress;
    private Button mGoogleSignin, mSignin;
    private Snackbar snackbar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mUserRef;
    private GoogleSignInAccount account;

    private int RC_SIGN_IN = 2;

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

        mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER);

        // Network state is checked
        if (savedInstanceState == null) {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isConnected)
                if (Build.VERSION.SDK_INT >= 21) {
                    snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "Sorry no network connection\nPlease turn on network connection",
                            Snackbar.LENGTH_INDEFINITE).setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                } else
                    Toast.makeText(getActivity(), "Sorry no network connection\nPlease turn on network connection", Toast.LENGTH_LONG).show();
        }

        // User login listener
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    mUserRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user.getUid())) {
                                chageIntent();
                            } else {// Checking if new user via google login
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(account.getDisplayName())
                                        .build();

                                user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            mUserRef.child(user.getUid()).child(Constants.USERNAME).setValue(account.getDisplayName());
                                            mUserRef.child(user.getUid()).child(Constants.EMAIL).setValue(account.getEmail());
                                            chageIntent();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (Build.VERSION.SDK_INT >= 21)
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Login Failed", Snackbar.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login2, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmail = (EditText) getActivity().findViewById(R.id.login_email);
        mPassword = (EditText) getActivity().findViewById(R.id.login_password);
        mSignin = (Button) getActivity().findViewById(R.id.signin_button);
        mProgress = new ProgressDialog(getActivity());
        mGoogleSignin = (Button) getActivity().findViewById(R.id.google_signin_button);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Stored data in EditText fields are recovered.
        if (savedInstanceState != null) {
            mEmail.setText(savedInstanceState.getString(Constants.EMAIL));
            mPassword.setText(savedInstanceState.getString(Constants.PASSWORD));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Checking if the user previously in signed in state or not.
        SharedPreferences loginState = getActivity().getSharedPreferences(Constants.LOGIN_STATE, Context.MODE_PRIVATE);
        if (loginState.getInt(Constants.LOGIN_STATE, 0) == 1) {
            mProgress.setMessage("Logging in your last session");
            mProgress.setCancelable(false);
            mProgress.show();
        }

        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // No empty data.
                if (!TextUtils.isEmpty(mEmail.getText().toString()) && !TextUtils.isEmpty(mPassword.getText().toString())) {
                    mProgress.setMessage("Logging in");
                    mProgress.show();
                    mProgress.setCancelable(false);

                    mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        if (Build.VERSION.SDK_INT >= 21)
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Cant Log in.", Snackbar.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(getActivity(), "Cant Log in.", Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });
                } else {
                    if (Build.VERSION.SDK_INT >= 21)
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Please fill email and password", Snackbar.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Please fill email and password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        // Authentication listener is unregistered.
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Connectivity was checked for once.
        String CHECK_INTERNET = "InternetStatusCheck";
        outState.putBoolean(CHECK_INTERNET, false);

        // Data in EditText fields is stored.
        outState.putString(Constants.EMAIL, mEmail.getText().toString());
        outState.putString(Constants.PASSWORD, mPassword.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void signInGoogle() {
        // Google accounts intent pops up.
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google signin was successful, authenticate with Firebase
                mProgress.setMessage("Logging in");
                mProgress.show();
                mProgress.setCancelable(false);

                account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                if (Build.VERSION.SDK_INT >= 21)
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Sorry we couldn't select your account", Snackbar.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Sorry we couldn't select your account", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            if (Build.VERSION.SDK_INT >= 21)
                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Cant Log in.", Snackbar.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getActivity(), "Cant Log in.", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });
    }

    private void chageIntent() {
        SharedPreferences loginState = getActivity().getSharedPreferences(Constants.LOGIN_STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = loginState.edit();
        editor.putInt(Constants.LOGIN_STATE, 1);
        editor.apply();
        mProgress.dismiss();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
