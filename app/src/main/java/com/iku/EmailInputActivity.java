package com.iku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class EmailInputActivity extends AppCompatActivity {

    private MaterialButton emailNextButton;
    private String email;
    private TextInputEditText emailEditText;
    private FirebaseAuth fAuth;
    private ImageView backButton;
    private String TAG = EmailInputActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_input);

        emailNextButton = findViewById(R.id.email_next_button);
        emailEditText = findViewById(R.id.enter_email);

        fAuth = FirebaseAuth.getInstance();

        backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        emailNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailEditText.getText().toString().trim();

                //validations
                if (email.isEmpty()) {
                    emailEditText.setError("Email cannot be empty");
                    return;
                }

                fAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(
                        new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                Log.d(TAG, "" + task.getResult().getSignInMethods().size());
                                if (task.getResult().getSignInMethods().size() == 0) {

                                    Log.i(TAG, "Email not exists");

                                    // email not existed
                                    //Go to Sigup page
                                    Intent goToNewPasswordActivity = new Intent(EmailInputActivity.this, NewPasswordInputActivity.class);
                                    goToNewPasswordActivity.putExtra("email", email);
                                    startActivity(goToNewPasswordActivity);

                                } else {

                                    Log.i(TAG, "Email exists");
                                    // email existed
                                    // Go to Login page
                                    Intent goToPasswordActivity = new Intent(EmailInputActivity.this, PasswordInputActivity.class);
                                    goToPasswordActivity.putExtra("email", email);
                                    startActivity(goToPasswordActivity);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }
}