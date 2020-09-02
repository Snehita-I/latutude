package com.iku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.iku.databinding.ActivityPasswordInputBinding;

public class PasswordInputActivity extends AppCompatActivity {

    private ActivityPasswordInputBinding binding;

    private FirebaseAuth firebaseAuth;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle extras = getIntent().getExtras();
        final String enteredEmail = extras.getString("email");

        firebaseAuth = FirebaseAuth.getInstance();

        initProgressDialog();

        binding.backButton.setOnClickListener(view -> onBackPressed());

        binding.signinButton.setOnClickListener(view -> {

            if (binding.enterPassword.getText().toString().isEmpty()) {
                Toast.makeText(PasswordInputActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            } else {
                mProgress.show();
                firebaseAuth.signInWithEmailAndPassword(enteredEmail, binding.enterPassword.getText().toString())
                        .addOnCompleteListener(PasswordInputActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PasswordInputActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                                //sending to Home Activity
                                mProgress.dismiss();
                                Intent goToHomeActivity = new Intent(PasswordInputActivity.this, HomeActivity.class);
                                goToHomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(goToHomeActivity);


                                //log event
                                Bundle password_bundle = new Bundle();
                                password_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, password_bundle);

                            } else {
                                mProgress.dismiss();
                                Toast.makeText(PasswordInputActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        binding.forgotPasswordTextView.setOnClickListener(view -> firebaseAuth.sendPasswordResetEmail(enteredEmail).addOnSuccessListener(aVoid -> Toast.makeText(PasswordInputActivity.this, "Verification Email Sent", Toast.LENGTH_LONG).show()).addOnFailureListener(e -> Toast.makeText(PasswordInputActivity.this, "Email Not Sent" + e.getMessage(), Toast.LENGTH_LONG).show()));

    }

    private void initProgressDialog() {
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
    }
}