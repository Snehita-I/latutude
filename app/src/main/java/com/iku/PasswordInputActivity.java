package com.iku;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.iku.databinding.ActivityPasswordInputBinding;

public class PasswordInputActivity extends AppCompatActivity {

    private ActivityPasswordInputBinding binding;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        final String enteredEmail = extras.getString("email");

        firebaseAuth = FirebaseAuth.getInstance();

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.signinButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (binding.enterPassword.getText().toString().isEmpty()) {
                    Toast.makeText(PasswordInputActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(enteredEmail, binding.enterPassword.getText().toString())
                        .addOnCompleteListener(PasswordInputActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PasswordInputActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                                    //sending to Home Activity
                                    Intent goToHomeActivity = new Intent(PasswordInputActivity.this, HomeActivity.class);
                                    goToHomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(goToHomeActivity);
                                } else {
                                    Toast.makeText(PasswordInputActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        binding.forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.sendPasswordResetEmail(enteredEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PasswordInputActivity.this, "Verification Email Sent", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(PasswordInputActivity.this, "Email Not Sent" + e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });


            }
        });

    }
}