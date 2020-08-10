package com.iku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NameInputActivity extends AppCompatActivity {
    EditText firstName,lastName;
    String email;
    Button namesNextButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static final String TAG = NameInputActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_input);
        firstName=(EditText)findViewById(R.id.enter_first_name);
        lastName=(EditText)findViewById(R.id.enter_last_name);
        namesNextButton=(Button)findViewById(R.id.names_next_button);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        namesNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newUserSignUp(firstName.getText().toString(),lastName.getText().toString(),email);
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
    private void newUserSignUp(String firstName, String lastName, String email) {

        // Create the arguments to the callable function.
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("email", email);

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
        }

    }

}
