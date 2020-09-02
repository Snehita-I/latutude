package com.iku;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.iku.databinding.ActivityNameInputBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class NameInputActivity extends AppCompatActivity {

    private ActivityNameInputBinding binding;

    private String email;

    private FirebaseFirestore db;

    private FirebaseAuth fAuth;

    private FirebaseUser user;

    public static final String TAG = NameInputActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNameInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        fAuth = FirebaseAuth.getInstance();

        user = fAuth.getCurrentUser();

        email = user.getEmail();


        db = FirebaseFirestore.getInstance();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!user.isEmailVerified()) {
                    handler.postDelayed(this, 1000);
                    user.reload();
                } else {
                    // do actions
                    binding.resendEmailButton.setVisibility(View.GONE);
                    binding.verificationMessage.setVisibility(View.GONE);
                    binding.enterFirstName.setVisibility(View.VISIBLE);
                    binding.enterLastName.setVisibility(View.VISIBLE);
                    binding.namesNextButton.setVisibility(View.VISIBLE);
                    Toast.makeText(NameInputActivity.this, "Email verification successful!", Toast.LENGTH_SHORT).show();

                }
            }
        }, 1000);

        binding.resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NameInputActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        //log event
                        Bundle password_bundle = new Bundle();
                        password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                        password_bundle.putString("verification_email_status", "sent");
                        mFirebaseAnalytics.logEvent("email_verified", password_bundle);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Register", "email not sent " + e.getMessage());
                        //log event
                        Bundle password_bundle = new Bundle();
                        password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                        password_bundle.putString("verification_email_status", "failed");
                        mFirebaseAnalytics.logEvent("verification_email_failed", password_bundle);
                    }
                });
                new CountDownTimer(1*60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        binding.resendEmailButton.setEnabled(false);
                        binding.resendEmailButton.setText("Resend in " +new SimpleDateFormat("ss").format(new Date( millisUntilFinished)) + "s");
                        binding.resendEmailButton.setTextColor(getResources().getColor(R.color.colorTextSecondary));
                    }

                    public void onFinish() {
                        binding.resendEmailButton.setText("Resend Verification Email");
                        binding.resendEmailButton.setEnabled(true);
                        binding.resendEmailButton.setTextColor(getResources().getColor(R.color.colorAccent));

                    }
                }.start();
            }
        });

        binding.namesNextButton.setOnClickListener(view -> {
            user.reload();
            if (binding.enterFirstName.getText().toString().length() > 0 &&
                    binding.enterLastName.getText().toString().length() > 0) {
                if (!user.isEmailVerified()) {
                    Toast.makeText(NameInputActivity.this, "Verify your email via the email sent to you before proceeding.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "VERIFIED USER.");
                    String firstName = binding.enterFirstName.getText().toString().trim();
                    firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();

                    String lastName = binding.enterLastName.getText().toString().trim();
                    lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

                    if (!firstName.isEmpty() && !lastName.isEmpty())
                        newUserSignUp(firstName, lastName, email);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(NameInputActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(NameInputActivity.this, "Sign in to continue.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void newUserSignUp(final String firstName, final String lastName, final String email) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    Toast.makeText(NameInputActivity.this, token, Toast.LENGTH_SHORT).show();
                    // Create the arguments to the callable function.
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("firstName", firstName);
                    userInfo.put("lastName", lastName);
                    userInfo.put("email", email);
                    userInfo.put("uid", fAuth.getUid());
                    userInfo.put("points", 0);

                    Map<String, Object> userRegistrationTokenInfo = new HashMap<>();
                    userRegistrationTokenInfo.put("registrationToken", token);
                    userRegistrationTokenInfo.put("uid", fAuth.getUid());


                    final String userID = fAuth.getUid();

                    if (userID != null) {

                        db.collection("users").document(fAuth.getUid())
                                .set(userInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        DocumentReference groupRef = db.collection("groups").document("iku_earth");
                                        groupRef.update("members", FieldValue.arrayUnion(userID));
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(firstName + " " + lastName).build();

                                        user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> updateUI(user));

                                        Log.d(TAG, "DocumentSnapshot successfully written!" + user.getDisplayName());

                                        /*Log event*/
                                        Bundle signup_bundle = new Bundle();
                                        signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                                    }
                                })
                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

                        db.collection("registrationTokens").document(fAuth.getUid())
                                .set(userRegistrationTokenInfo)
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> {

                                });
                    }
                });
    }
}