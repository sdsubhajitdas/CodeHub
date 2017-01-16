package com.subhajitdas.c;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private FloatingActionButton mGo;
    private ProgressDialog mProgress;
    private TextView mRegister;
    private Button mGoogleLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mUserRef;
    GoogleSignInAccount account;

    private String TAG = "Jeetu";
    private String EMAIL = "email";
    private String PASSWORD = "password";
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

        mUserRef = FirebaseDatabase.getInstance().getReference().child("user");
        //----NETWORK STATE CHECKING----
        if (savedInstanceState == null) {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isConnected)
                Toast.makeText(getActivity(), "Sorry no network connection\nPlease turn on network connection", Toast.LENGTH_LONG).show();
        }

        //----USER LOGIN LISTENER-----
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
                            } else {//CHECKING IF NEW USER VIA GOOGLE LOGIN
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(account.getDisplayName())
                                        .build();

                                user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            mUserRef.child(user.getUid()).child("username").setValue(account.getDisplayName());
                                            mUserRef.child(user.getUid()).child("email").setValue(account.getEmail());
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
                .enableAutoManage((AppCompatActivity) getActivity() /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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
        mRegister = (TextView) getActivity().findViewById(R.id.register);
        mProgress = new ProgressDialog(getActivity());
        mGoogleLogin = (Button) getActivity().findViewById(R.id.login_google_button);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mEmail.setText(savedInstanceState.getString(EMAIL));
            mPassword.setText(savedInstanceState.getString(PASSWORD));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences loginState =getActivity().getSharedPreferences("LOGIN_STATE",Context.MODE_PRIVATE);

        if(loginState.getInt("LOGIN_STATE",0)==1)
        {
            mProgress.setMessage("Logging in your last session");
            mProgress.setCancelable(false);
            mProgress.show();
        }

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterFragment register = new RegisterFragment();
                FragmentTransaction tempTransaction = getActivity().getFragmentManager().beginTransaction();
                tempTransaction.replace(R.id.frag_container, register);
                tempTransaction.addToBackStack(null);
                tempTransaction.commit();
            }
        });

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(mEmail.getText().toString()) && !TextUtils.isEmpty(mPassword.getText().toString())) {
                    mProgress.setMessage("Logging in");
                    mProgress.show();
                    mProgress.setCancelable(false);

                    mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Cant Log in.", Toast.LENGTH_SHORT).show();
                                        mProgress.hide();
                                    }


                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Please fill email and password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
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
        outState.putBoolean(CHECK_INTERNET, false);
        outState.putString(EMAIL, mEmail.getText().toString());
        outState.putString(PASSWORD, mPassword.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            mProgress.setMessage("Logging in");
            mProgress.show();
            mProgress.setCancelable(false);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        } else {
            Toast.makeText(getActivity(), "Sorry we couldn't select your account", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), "Cant Log in.", Toast.LENGTH_SHORT).show();
                            mProgress.hide();
                        }
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void chageIntent()
    {
        SharedPreferences loginState = getActivity().getSharedPreferences("LOGIN_STATE",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =loginState.edit();
        editor.putInt("LOGIN_STATE",1);
        editor.apply();
        mProgress.hide();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
