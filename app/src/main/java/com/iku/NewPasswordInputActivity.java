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
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class NewPasswordInputActivity extends AppCompatActivity {

    private MaterialButton newPasswordNextBtn;
    private String email, password;
    private TextInputEditText passwordEditText;
    private FirebaseAuth fAuth;
    private ImageView backButton;
    private String TAG = "REGISTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password_input);

        newPasswordNextBtn = findViewById(R.id.new_password_next_button);
        passwordEditText = findViewById(R.id.enter_new_password);

        fAuth = FirebaseAuth.getInstance();

        backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent myIntent = getIntent();
        email = myIntent.getStringExtra("email");
        Log.i(TAG, "email: " + email);

        newPasswordNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = passwordEditText.getText().toString().trim();

                //validations
                if (password.isEmpty()) {
                    passwordEditText.setError("password cannot be empty");
                    return;
                }
                if (password.length() < 8) {
                    passwordEditText.setError("password must be at least 8 characters");
                    return;
                }

                //Firebase registrations
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.i("Register", "User registered ");

                                    Toast.makeText(NewPasswordInputActivity.this, "User Registered", Toast.LENGTH_SHORT).show();
                                    //sending to Profile Activity
                                    Intent goToNameActivity = new Intent(NewPasswordInputActivity.this, NameInputActivity.class);
                                    startActivity(goToNameActivity);
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