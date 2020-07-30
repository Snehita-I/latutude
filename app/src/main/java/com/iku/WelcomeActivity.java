package com.iku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class WelcomeActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private LoginButton loginButton;

    private SignInButton googleSignInButton;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    private MaterialButton emailButton;

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    private int RC_SIGN_IN = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        googleSignInButton = findViewById(R.id.google_signin_button);

        loginButton = findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email","public_profile");
        mCallbackManager = CallbackManager.Factory.create();


        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        emailButton = findViewById(R.id.email_signin_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("FB AUTH", "onSuccess: " + loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "onCancel: ");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "onError: " + error);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String personName = user.getDisplayName();
                    String personEmail = user.getEmail();
                    String personPhoto = user.getPhotoUrl().toString();
                    String personID = user.getUid();
                    personPhoto = personPhoto + "?type=large";
                    Log.e(TAG, "\nEmail: " + personEmail + "\nPhoto: " + personPhoto + "\nID: " + personID + "\nName: " + personName);

                } else
                    Log.i(TAG, "onAuthStateChanged: Not logged in");
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    mAuth.signOut();
                }
            }
        };

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent enterEmailIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
                startActivity(enterEmailIntent);
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            signInResult(task);
        }
    }

    private void signInResult(Task<GoogleSignInAccount> signInAccountTask) {
        try {
            GoogleSignInAccount acc = signInAccountTask.getResult(ApiException.class);
            Log.e(TAG, "signInResult: Signed IN");
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Log.e(TAG, "signInResult: Sign In Fail");
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount inAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(inAccount.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.e(TAG, "onComplete: Successful");
                    FirebaseUser user = mAuth.getCurrentUser();
                    googleAccountDetails(user);
                } else {
                    Log.e(TAG, "onComplete: failed");
                    googleAccountDetails(null);
                }
            }
        });
    }

    private void googleAccountDetails(FirebaseUser fUser) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personID = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            Log.e(TAG, "\nEmail: " + personEmail + "\nFamilyName: " + personFamilyName + "\nPhoto: " + personPhoto + "\nGiveName: " + personGivenName + "\nID: " + personID + "\nName: " + personName);

        }
    }

    private void handleFacebookToken(AccessToken token) {
        Log.i(TAG, "handleFacebookToken: " + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "SIGN IN WITH CREDENTIAL SUCCESS");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String personName = user.getDisplayName();
                        String personEmail = user.getEmail();
                        String personPhoto = user.getPhotoUrl().toString();
                        String personID = user.getUid();
                        personPhoto = personPhoto + "?type=large";
                        Log.e(TAG, "\nEmail: " + personEmail + "\nPhoto: " + personPhoto + "\nID: " + personID + "\nName: " + personName);
                    }
                } else {
                    Log.i(TAG, "SIGN IN WITH CREDENTIAL FAIL");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}