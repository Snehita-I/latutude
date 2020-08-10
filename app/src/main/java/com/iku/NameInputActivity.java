package com.iku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NameInputActivity extends AppCompatActivity {

    private MaterialButton submitNameBtn;
    private FirebaseAuth fAuth;
    private FirebaseUser user;

    private String TAG = NameInputActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_input);

        fAuth = FirebaseAuth.getInstance();
        submitNameBtn = findViewById(R.id.names_next_button);

        user = fAuth.getCurrentUser();

        submitNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.reload();
                if (!user.isEmailVerified()) {
                    Toast.makeText(NameInputActivity.this, "Verify your email via the email sent to you before proceeding.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.i(TAG, "VERIFIED USER. ");
                    Intent goToHome = new Intent(NameInputActivity.this, HomeActivity.class);
                    startActivity(goToHome);
                }
            }
        });


    }
}