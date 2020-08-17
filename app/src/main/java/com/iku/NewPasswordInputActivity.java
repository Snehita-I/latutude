package com.iku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iku.databinding.ActivityNewPasswordInputBinding;

public class NewPasswordInputActivity extends AppCompatActivity {

    private ActivityNewPasswordInputBinding binding;

    private String email, password;

    private FirebaseAuth fAuth;

    private String TAG = NewPasswordInputActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPasswordInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fAuth = FirebaseAuth.getInstance();

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent myIntent = getIntent();
        email = myIntent.getStringExtra("email");
        Log.i(TAG, "email: " + email);

        binding.newPasswordNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = binding.enterNewPassword.getText().toString().trim();

                //validations
                if (password.isEmpty()) {
                    binding.enterNewPassword.setError("password cannot be empty");
                    return;
                }
                if (password.length() < 8) {
                    binding.enterNewPassword.setError("password must be at least 8 characters");
                    return;
                }

                //Firebase registrations
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.i("Register", "User registered ");

                                    FirebaseUser user = fAuth.getCurrentUser();

                                    if (user != null) {
                                        Log.i(TAG, String.valueOf(user));
                                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(NewPasswordInputActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("Register", "email not sent " + e.getMessage());
                                            }
                                        });

                                        Toast.makeText(NewPasswordInputActivity.this, "User Registered", Toast.LENGTH_SHORT).show();
                                        //sending to Profile Activity
                                        Intent goToNameActivity = new Intent(NewPasswordInputActivity.this, NameInputActivity.class);
                                        startActivity(goToNameActivity);

                                    }
                                } else {
                                    Toast.makeText(NewPasswordInputActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        });

    }
}