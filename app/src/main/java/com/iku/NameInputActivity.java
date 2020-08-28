package com.iku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.iku.databinding.ActivityNameInputBinding;

import java.util.HashMap;
import java.util.Map;

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

        binding.namesNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.reload();
                if (!user.isEmailVerified()) {
                    Toast.makeText(NameInputActivity.this, "Verify your email via the email sent to you before proceeding.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "VERIFIED USER.");
                    newUserSignUp(binding.enterFirstName.getText().toString(), binding.enterLastName.getText().toString(), email);
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
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
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

                                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    updateUI(user);
                                                }
                                            });

                                            Log.d(TAG, "DocumentSnapshot successfully written!" + user.getDisplayName());

                                            /*Log event*/
                                            Bundle signup_bundle = new Bundle();
                                            signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, signup_bundle);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                            db.collection("registrationTokens").document(fAuth.getUid())
                                    .set(userRegistrationTokenInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "onSuccess: of Reg tok");
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
}
