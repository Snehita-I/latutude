package com.iku;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

public class UserProfileActivity extends AppCompatActivity {


    private ImageView profilePicture;
    private MaterialTextView nameTextView,userHeartsTextView;
    private FirebaseFirestore db;
    private String TAG = UserProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pofile);

        String firstLetter, secondLetter;

        db = FirebaseFirestore.getInstance();

        Bundle extras = this.getIntent().getExtras();
        String userName = extras.getString("EXTRA_PERSON_NAME");
        String userUID = extras.getString("EXTRA_PERSON_UID");

        getUserHearts(userUID);

        nameTextView = findViewById(R.id.userName);
        profilePicture = findViewById(R.id.profileImage);
        userHeartsTextView = findViewById(R.id.userHearts);

        if (userName!=null) {
            nameTextView.setText(userName);
            Log.i(TAG, "Set Username: " + userName);

            firstLetter = String.valueOf(userName.charAt(0));
            secondLetter = userName.substring(userName.indexOf(' ') + 1, userName.indexOf(' ') + 2).trim();
            Log.i(TAG, "No picture:" + firstLetter + " " + secondLetter);

            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(200)
                    .height(200)
                    .endConfig()
                    .buildRect(firstLetter + secondLetter, Color.DKGRAY);

            profilePicture.setImageDrawable(drawable);
        }
        else {
            nameTextView.setText("An ikulogist!");
            profilePicture.setImageResource(R.drawable.ic_circle_account);
        }
    }

    private void getUserHearts(String uid) { {
            db.collection("users").whereEqualTo("uid", uid)
                    .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    userHeartsTextView.setText("Hearts Won: " + change.getDocument().get("points"));
                                }

                                String source = querySnapshot.getMetadata().isFromCache() ?
                                        "local cache" : "server";
                                Log.i(TAG, "Cache Hearts Won: " + source);
                            }

                        }
                    });
        }
    }

}