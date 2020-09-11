package com.iku;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iku.databinding.ActivityChatImageBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatImageActivity extends AppCompatActivity {

    private StorageReference mStorageRef;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;

    private FirebaseAnalytics mFirebaseAnalytics;

    private Uri mImageUri;

    private int PICK_IMAGE = 1;

    private int STORAGE_PERMISSION_CODE = 10;

    private String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private ActivityChatImageBinding chatImageBinding;

    private String TAG = ChatImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatImageBinding = ActivityChatImageBinding.inflate(getLayoutInflater());
        setContentView(chatImageBinding.getRoot());

        initItems();
        initButtons();

        openFileChooser();
    }

    private void initItems() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference(user.getUid());
        db = FirebaseFirestore.getInstance();
    }

    private void initButtons() {

        chatImageBinding.backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        chatImageBinding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chatImageBinding.messageTextField.getText().toString().isEmpty()) {

                    uploadFile(chatImageBinding.messageTextField.getText().toString());

                    chatImageBinding.sendMessageButton.setClickable(false);

                } else
                    Toast.makeText(ChatImageActivity.this, "Caption such empty..much wow!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadFile(String message) {
        if (mImageUri != null) {
            StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + ".");
            UploadTask uploadTask = imageRef.putFile(mImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                downloadUrl.addOnSuccessListener(uri -> {
                    Date d = new Date();
                    long timestamp = d.getTime();
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("message", message.trim());
                    docData.put("timestamp", timestamp);
                    docData.put("uid", user.getUid());
                    docData.put("type", "image");
                    docData.put("imageUrl", uri.toString());
                    docData.put("userName", user.getDisplayName());
                    docData.put("upvoteCount", 0);
                    ArrayList<Object> upvotersArray = new ArrayList<>();
                    docData.put("upvoters", upvotersArray);
                    ArrayList<Object> thumbsUpArray = new ArrayList<>();
                    docData.put("emoji1", thumbsUpArray);
                    ArrayList<Object> clapsArray = new ArrayList<>();
                    docData.put("emoji2", clapsArray);
                    ArrayList<Object> thinkArray = new ArrayList<>();
                    docData.put("emoji3", thinkArray);
                    ArrayList<Object> ideaArray = new ArrayList<>();
                    docData.put("emoji4", ideaArray);
                    ArrayList<Object> dounvotersArray = new ArrayList<>();
                    docData.put("downvoters", dounvotersArray);
                    docData.put("downvoteCount", 0);

                    Map<String, Object> normalMessage = new HashMap<>();
                    normalMessage.put("firstImage", true);

                    db.collection("iku_earth_messages")
                            .add(docData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    db.collection("users").document(user.getUid()).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            Boolean isFirstImage = (Boolean) document.get("firstImage");
                                                            if (!isFirstImage) {
                                                                db.collection("users").document(user.getUid())
                                                                        .update(normalMessage)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                //Log event
                                                                                Bundle params = new Bundle();
                                                                                params.putString("type", "image");
                                                                                params.putString("uid", user.getUid());
                                                                                mFirebaseAnalytics.logEvent("first_message", params);
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {

                                                                            }
                                                                        });

                                                            }
                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    } else {
                                                        Log.d(TAG, "get failed with ", task.getException());
                                                    }
                                                }
                                            });


                                    chatImageBinding.messageTextField.setText("");
                                    Toast.makeText(ChatImageActivity.this, "Aren't you the best", Toast.LENGTH_LONG).show();
                                    chatImageBinding.messageTextField.requestFocus();
                                    ChatImageActivity.super.onBackPressed();

                                    //Log event
                                    Bundle params = new Bundle();
                                    params.putString("type", "image");
                                    params.putString("uid", user.getUid());
                                    mFirebaseAnalytics.logEvent("messaging", params);
                                }
                            })
                            .addOnFailureListener(e -> {
                                chatImageBinding.sendMessageButton.setClickable(true);
                                Toast.makeText(ChatImageActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            });
                });
            });
        } else
            onBackPressed();
    }

    private void openFileChooser() {
        if (checkAndRequestPermissions()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();
            Log.i(TAG, "onActivityResult: " + mImageUri);
            chatImageBinding.chosenImage.setImageURI(mImageUri);
        } else
            onBackPressed();
    }

    public boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    STORAGE_PERMISSION_CODE);
            return false;
        }
        return true;
    }
}