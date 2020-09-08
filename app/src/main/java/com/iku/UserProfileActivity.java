package com.iku;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.databinding.ActivityUserPofileBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {


    private ImageView profilePicture;
    private MaterialTextView nameTextView, userHeartsTextView;
    private FirebaseFirestore db;

    private String userName;

    private String userUID;

    private ActivityUserPofileBinding userPofileBinding;

    private String TAG = UserProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPofileBinding = ActivityUserPofileBinding.inflate(getLayoutInflater());
        setContentView(userPofileBinding.getRoot());

        db = FirebaseFirestore.getInstance();

        Bundle extras = this.getIntent().getExtras();
        userName = extras.getString("EXTRA_PERSON_NAME");
        userUID = extras.getString("EXTRA_PERSON_UID");

        initButtons();
        getUserHearts(userUID);
        getPicture(userUID);

        nameTextView = findViewById(R.id.userName);
        nameTextView.setText(userName);
        profilePicture = findViewById(R.id.profileImage);
        userHeartsTextView = findViewById(R.id.userHearts);

    }

    private void initButtons() {
        userPofileBinding.backButton.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void getPicture(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String firstLetter, secondLetter;
                                String url = (String) document.get("imageUrl");
                                Log.i(TAG, "onComplete: " + url);
                                if (url != null) {
                                    Picasso.get()
                                            .load(url)
                                            .noFade()
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(profilePicture, new Callback() {

                                                @Override
                                                public void onSuccess() {
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get()
                                                            .load(url)
                                                            .noFade()
                                                            .into(profilePicture);
                                                }
                                            });
                                } else {

                                    firstLetter = String.valueOf(userName.charAt(0));
                                    secondLetter = userName.substring(userName.indexOf(' ') + 1, userName.indexOf(' ') + 2).trim();

                                    TextDrawable drawable = TextDrawable.builder()
                                            .beginConfig()
                                            .width(200)
                                            .height(200)
                                            .endConfig()
                                            .buildRect(firstLetter + secondLetter, Color.DKGRAY);

                                    profilePicture.setImageDrawable(drawable);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void getUserHearts(String uid) {
        {
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
                            }

                        }
                    });
        }
    }

}