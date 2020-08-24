package com.iku;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatImageActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private ImageView sendImageChatbtn;
    private Uri mImageUri, mainImageUri;
    private ImageView image;
    private EditText messageEntered;
    private ImageButton backButton;
    private int PICK_IMAGE = 1;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        messageEntered = findViewById(R.id.messageTextField);
        mStorageRef = FirebaseStorage.getInstance().getReference("images");

        backButton = findViewById(R.id.backbutton);
        sendImageChatbtn = findViewById(R.id.sendMessageButton);
        image = findViewById(R.id.chosenImage);
        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        openFileChooser();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendImageChatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    uploadFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getFileExtension(Uri uri) {
        if (uri != null) {
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } else return null;
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "images", null);

        if (path != null) return Uri.parse(path);
        else return null;
    }


    private void uploadFile() throws FileNotFoundException {
        if (mImageUri != null) {
            Bitmap imageSelected = decodeUri(this, mImageUri, 300);
            if (imageSelected != null)
                mainImageUri = getImageUri(this, imageSelected);
            if (mainImageUri != null) {
                final StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mainImageUri));
                UploadTask uploadTask = imageRef.putFile(mainImageUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Date d = new Date();
                                long timestamp = d.getTime();
                                Map<String, Object> docData = new HashMap<>();
                                docData.put("message", messageEntered.getText().toString());
                                docData.put("timestamp", timestamp);
                                docData.put("uid", user.getUid());
                                docData.put("type", "image");
                                docData.put("imageUrl", uri.toString());
                                docData.put("userName", user.getDisplayName());
                                docData.put("upvoteCount", 0);
                                ArrayList<Object> upvotersArray = new ArrayList<>();
                                docData.put("upvoters", upvotersArray);

                                db.collection("iku_earth_messages")
                                        .add(docData)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                messageEntered.setText("");
                                                Toast.makeText(ChatImageActivity.this, "Image info uploaded", Toast.LENGTH_LONG).show();
                                                messageEntered.requestFocus();
                                                ChatImageActivity.super.onBackPressed();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(ChatImageActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        });
                    }
                });
            }

        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show();
        }
    }

    private void openFileChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Log.i("ap", "onActivityResult: " + mImageUri);
            Picasso.get().load(mImageUri).into(image);
            image.setImageURI(mImageUri);
        } else
            onBackPressed();
    }
}