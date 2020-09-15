package com.iku;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.databinding.ActivityViewPostBinding;
import com.iku.models.LeaderboardModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ViewPostActivity extends AppCompatActivity {

    private static final String TAG = "ViewPost";

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy");

    private Bundle extras;

    private ActivityViewPostBinding viewPostBinding;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private MaterialTextView memberCount;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPostBinding = ActivityViewPostBinding.inflate(getLayoutInflater());
        setContentView(viewPostBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        extras = this.getIntent().getExtras();

        setImage();
        setDetails();

        String messageId = extras.getString("EXTRA_MESSAGE_ID");
        initalEmojis(messageId);
        reactions(messageId);

        viewPostBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void setDetails() {

        String userName = extras.getString("EXTRA_PERSON_NAME");
        long timestamp = extras.getLong("EXTRA_POST_TIMESTAMP");

        viewPostBinding.userName.setText(userName);
        viewPostBinding.postTime.setText(sfdMainDate.format(timestamp));
    }

    private void setImage() {


        String imageUrl = extras.getString("EXTRA_IMAGE_URL");

        if (imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(viewPostBinding.viewedImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .noFade()
                                    .into(viewPostBinding.viewedImage);
                        }
                    });
        } else {
            finish();
        }
    }

    private void initalEmojis(String messageId) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long upvotesCount, downvotesCount;
                        ArrayList<String> HeartUpArray = (ArrayList) document.get("upvoters");
                        ArrayList<String> emoji1Array = (ArrayList) document.get("emoji1");
                        ArrayList<String> emoji2Array = (ArrayList) document.get("emoji2");
                        ArrayList<String> emoji3Array = (ArrayList) document.get("emoji3");
                        ArrayList<String> emoji4Array = (ArrayList) document.get("emoji4");
                        ArrayList<String> HeartDownArray = (ArrayList) document.get("downvoters");
                        upvotesCount = (long) document.get("upvoteCount");
                        downvotesCount = (long) document.get("downvoteCount");

                        if (upvotesCount >= 0) {
                            for (String element : HeartUpArray) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji1Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji2Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji3Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                            for (String element : emoji4Array) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                        }
                        if (downvotesCount >= 0) {
                            for (String element : HeartDownArray) {
                                if (element.contains(user.getUid())) {
                                    viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                }
                            }
                        }

                    } else {
                    }
                } else {
                }
            }
        });
    }

    private void reactions(String messageId) {

        viewPostBinding.choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "upvoters");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji1");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji2");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji3");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "emoji4");
                disableEmojiButtons(false);
            }
        });
        viewPostBinding.choose6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVote(messageId, "downvoters");
                disableEmojiButtons(false);
            }
        });
    }

    public void userVote(String messageDocumentID, String emoji) {
        DocumentReference docRef = db.collection("iku_earth_messages").document(messageDocumentID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String authorOfMessage;
                        Long upvotesCount, downvotesCount;
                        authorOfMessage = (String) document.get("uid");
                        ArrayList<String> HeartUpArray = (ArrayList) document.get("upvoters");
                        ArrayList<String> emoji1Array = (ArrayList) document.get("emoji1");
                        ArrayList<String> emoji2Array = (ArrayList) document.get("emoji2");
                        ArrayList<String> emoji3Array = (ArrayList) document.get("emoji3");
                        ArrayList<String> emoji4Array = (ArrayList) document.get("emoji4");
                        ArrayList<String> HeartDownArray = (ArrayList) document.get("downvoters");
                        upvotesCount = (long) document.get("upvoteCount");
                        downvotesCount = (long) document.get("downvoteCount");
                        Boolean HeartupLiked = false;
                        Boolean emoji1Liked = false;
                        Boolean emoji2Liked = false;
                        Boolean emoji3Liked = false;
                        Boolean emoji4Liked = false;
                        Boolean disliked = false;

                        if (upvotesCount >= 0) {
                            for (String element : HeartUpArray) {
                                if (element.contains(user.getUid())) {
                                    HeartupLiked = true;
                                    break;
                                }
                            }
                            for (String element : emoji1Array) {
                                if (element.contains(user.getUid())) {
                                    emoji1Liked = true;
                                    break;
                                }
                            }
                            for (String element : emoji2Array) {
                                if (element.contains(user.getUid())) {
                                    emoji2Liked = true;
                                    break;
                                }
                            }
                            for (String element : emoji3Array) {
                                if (element.contains(user.getUid())) {
                                    emoji3Liked = true;
                                    break;
                                }
                            }
                            for (String element : emoji4Array) {
                                if (element.contains(user.getUid())) {
                                    emoji4Liked = true;
                                    break;
                                }
                            }
                        }
                        if (downvotesCount >= 0) {
                            for (String element : HeartDownArray) {
                                if (element.contains(user.getUid())) {
                                    disliked = true;
                                    break;
                                }
                            }
                        }
                        if (!HeartupLiked && !emoji1Liked && !emoji2Liked && !emoji3Liked && !emoji4Liked && !disliked) {
                            newLikeorDislike(messageDocumentID, emoji, upvotesCount, downvotesCount, authorOfMessage);
                        } else {
                            if (HeartupLiked) {
                                changeLikesArray(messageDocumentID, emoji, "upvoters", upvotesCount, downvotesCount, authorOfMessage);
                            } else if (emoji1Liked) {
                                changeLikesArray(messageDocumentID, emoji, "emoji1", upvotesCount, downvotesCount, authorOfMessage);
                            } else if (emoji2Liked) {
                                changeLikesArray(messageDocumentID, emoji, "emoji2", upvotesCount, downvotesCount, authorOfMessage);
                            } else if (emoji3Liked) {
                                changeLikesArray(messageDocumentID, emoji, "emoji3", upvotesCount, downvotesCount, authorOfMessage);
                            } else if (emoji4Liked) {
                                changeLikesArray(messageDocumentID, emoji, "emoji4", upvotesCount, downvotesCount, authorOfMessage);
                            } else if (disliked) {
                                changeLikesArray(messageDocumentID, emoji, "downvoters", upvotesCount, downvotesCount, authorOfMessage);
                            }
                        }
                    } else {
                    }
                } else {
                }
            }
        });
    }

    private void changeLikesArray(String messageDocumentID, String currentEmoji, String previousEmoji, long upvotesCount, long downvotesCount, String authorOfMessage) {
        if (currentEmoji == previousEmoji) {
            if (currentEmoji == "upvoters" || currentEmoji == "emoji1" || currentEmoji == "emoji2" || currentEmoji == "emoji3" || currentEmoji == "emoji4") {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                    db.collection("users").document(authorOfMessage)
                                            .update("points", usersData.getPoints() - 1)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                }
                            });
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("upvoteCount", upvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                switch (currentEmoji) {
                                    case "upvoters":
                                        viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji1":
                                        viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji2":
                                        viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji3":
                                        viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                    case "emoji4":
                                        viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                        break;
                                }
                                disableEmojiButtons(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        disableEmojiButtons(true);
                    }
                });
            } else if (currentEmoji == "downvoters") {
                if (!authorOfMessage.equals(user.getUid())) {
                    db.collection("users").document(authorOfMessage).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                    db.collection("users").document(authorOfMessage)
                                            .update("points", usersData.getPoints() + 1)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                }
                            });
                }
                db.collection("iku_earth_messages").document(messageDocumentID)
                        .update("downvoteCount", downvotesCount - 1,
                                currentEmoji, FieldValue.arrayRemove(user.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                                disableEmojiButtons(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        disableEmojiButtons(true);
                    }
                });
            }

        } else if ((currentEmoji != previousEmoji) && (currentEmoji == "downvoters")) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() - 2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount - 1,
                            "downvoteCount", downvotesCount + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeEmojiBackground(currentEmoji, previousEmoji);
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });

        } else if ((previousEmoji == "downvoters") && (currentEmoji != previousEmoji)) {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() + 2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", upvotesCount + 1,
                            "downvoteCount", downvotesCount - 1
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeEmojiBackground(currentEmoji, previousEmoji);
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        } else {
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(previousEmoji, FieldValue.arrayRemove(user.getUid()),
                            currentEmoji, FieldValue.arrayUnion(user.getUid()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            changeEmojiBackground(currentEmoji, previousEmoji);
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        }
    }

    private void newLikeorDislike(String messageDocumentID, String emoji, long UpvotesCount, long DownvotesCount, String authorOfMessage) {
        if (emoji == "downvoters") {
            viewPostBinding.heartDown.setBackgroundTintList(ContextCompat.getColorStateList(ViewPostActivity.this, R.color.white));
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() - 1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "downvoteCount", DownvotesCount + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        } else {
            if (!authorOfMessage.equals(user.getUid())) {
                db.collection("users").document(authorOfMessage).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                db.collection("users").document(authorOfMessage)
                                        .update("points", usersData.getPoints() + 1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
                                        });
                            }
                        });
            }
            db.collection("iku_earth_messages").document(messageDocumentID)
                    .update(emoji, FieldValue.arrayUnion(user.getUid()),
                            "upvoteCount", UpvotesCount + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            switch (emoji) {
                                case "emoji1":
                                    viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji2":
                                    viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji3":
                                    viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                case "emoji4":
                                    viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                                    break;
                                default:
                                    // code block
                            }
                            disableEmojiButtons(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    disableEmojiButtons(true);
                }
            });
        }
    }

    private void changeEmojiBackground(String currentEmoji, String previousEmoji) {
        switch (currentEmoji) {
            case "upvoters":
                viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji1":
                viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji2":
                viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji3":
                viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "emoji4":
                viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
            case "downvoters":
                viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_selected_viewpost_activity));
                break;
        }
        switch (previousEmoji) {
            case "upvoters":
                viewPostBinding.heartUp.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji1":
                viewPostBinding.emoji1.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji2":
                viewPostBinding.emoji2.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji3":
                viewPostBinding.emoji3.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "emoji4":
                viewPostBinding.emoji4.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
            case "downvoters":
                viewPostBinding.heartDown.setBackground(getDrawable(R.drawable.hearts_button_background_viewpost_activity));
                break;
        }
    }

    private void disableEmojiButtons(Boolean status) {

        viewPostBinding.choose.setEnabled(status);
        viewPostBinding.choose1.setEnabled(status);
        viewPostBinding.choose2.setEnabled(status);
        viewPostBinding.choose3.setEnabled(status);
        viewPostBinding.choose4.setEnabled(status);
        viewPostBinding.choose6.setEnabled(status);
    }
}