package com.iku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.iku.databinding.ActivityWelcomeBinding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;

    ActivityWelcomeBinding binding;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;

    private FirebaseFunctions mFunctions;

    private FirebaseFirestore db;

    public static final String TAG = WelcomeActivity.class.getSimpleName();

    private int RC_SIGN_IN = 121;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        db = FirebaseFirestore.getInstance();
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initProgressDialog();

        binding.googleSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        binding.emailSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent enterEmailIntent = new Intent(WelcomeActivity.this, EmailInputActivity.class);
                startActivity(enterEmailIntent);
            }
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        binding.facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.show();
                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this,
                        Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                    }
                });
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String displayName = user.getDisplayName();
                                if (displayName != null) {
                                    String firstName = displayName.substring(0, displayName.indexOf(' ')).trim();
                                    String lastName = displayName.substring(displayName.indexOf(' ')).trim();
                                    String email = user.getEmail();
                                    newUserSignUp(firstName, lastName, email);

                                    /*Log event*/
                                    Bundle signup_bundle = new Bundle();
                                    signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Facebook");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                                }
                            }
                        } else {

                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                            switch (errorCode) {

                                case "ERROR_INVALID_CUSTOM_TOKEN":
                                    Toast.makeText(WelcomeActivity.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                    Toast.makeText(WelcomeActivity.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_INVALID_CREDENTIAL":
                                    Toast.makeText(WelcomeActivity.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_INVALID_EMAIL":
                                    Toast.makeText(WelcomeActivity.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();

                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                    Toast.makeText(WelcomeActivity.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();

                                    break;

                                case "ERROR_USER_MISMATCH":
                                    Toast.makeText(WelcomeActivity.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_REQUIRES_RECENT_LOGIN":
                                    Toast.makeText(WelcomeActivity.this, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    Toast.makeText(WelcomeActivity.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    Toast.makeText(WelcomeActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                    Toast.makeText(WelcomeActivity.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_DISABLED":
                                    Toast.makeText(WelcomeActivity.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_TOKEN_EXPIRED":

                                case "ERROR_INVALID_USER_TOKEN":
                                    Toast.makeText(WelcomeActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_USER_NOT_FOUND":
                                    Toast.makeText(WelcomeActivity.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_OPERATION_NOT_ALLOWED":
                                    Toast.makeText(WelcomeActivity.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_WEAK_PASSWORD":
                                    Toast.makeText(WelcomeActivity.this, "The given password is invalid.", Toast.LENGTH_LONG).show();
                                    break;

                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Log.e(TAG, "signInResult: Sign In Fail" + e);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount inAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(inAccount.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(WelcomeActivity.this);
                    String firstName = null, lastName = null, email = null;
                    if (acct != null) {
                        firstName = acct.getGivenName();
                        lastName = acct.getFamilyName();
                        email = acct.getEmail();
                        mProgress.show();
                        newUserSignUp(firstName, lastName, email);

                        /*Log event*/
                        Bundle signup_bundle = new Bundle();
                        signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Google");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                    }
                } else {
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mProgress.dismiss();
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(WelcomeActivity.this, "Sign in to continue.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void newUserSignUp(final String firstName, final String lastName, final String email) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Create the arguments to the callable function.
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("firstName", firstName);
                        userInfo.put("lastName", lastName);
                        userInfo.put("email", email);
                        userInfo.put("uid", mAuth.getUid());
                        userInfo.put("points", 0);
                        userInfo.put("firstMessage", false);

                        Map<String, Object> userRegistrationTokenInfo = new HashMap<>();
                        userRegistrationTokenInfo.put("registrationToken", token);
                        userRegistrationTokenInfo.put("uid", mAuth.getUid());


                        final String userID = mAuth.getUid();

                        if (userID != null) {

                            db.collection("users").document(mAuth.getUid())
                                    .set(userInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            DocumentReference groupRef = db.collection("groups").document("iku_earth");
                                            groupRef.update("members", FieldValue.arrayUnion(userID));
                                            updateUI(user);
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                            db.collection("registrationTokens").document(mAuth.getUid())
                                    .set(userRegistrationTokenInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                        }
                    }
                });

    }

    private void initProgressDialog() {
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Logging in..");
        mProgress.setMessage("");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }
}