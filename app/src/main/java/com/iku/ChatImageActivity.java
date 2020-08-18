package com.iku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.appevents.codeless.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.iku.models.ChatImageModel;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

public class ChatImageActivity extends AppCompatActivity {

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    Button choose, upload;
    Uri mImageUri;
    Uri mainImageUri;
    ImageView image;
    EditText imageName;
    private FirebaseFirestore firebaseFirestore;
    int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image);

        imageName = (EditText) findViewById(R.id.imageName);
        mStorageRef = FirebaseStorage.getInstance().getReference("images");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("images");

        choose = (Button) findViewById(R.id.choose);
        upload = (Button) findViewById(R.id.upload);
        image = (ImageView) findViewById(R.id.imageView);
        firebaseFirestore = FirebaseFirestore.getInstance();
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
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
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                        + "." + getFileExtension(mainImageUri));

                StorageTask mUploadTask = fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Toast.makeText(ChatImageActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                                ChatImageModel chatImageModel = new ChatImageModel(
                                        taskSnapshot.getMetadata().getReference().getDownloadUrl().toString(), imageName.getText().toString().trim());
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(uploadId).setValue(chatImageModel);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Picasso.with(this).load(mImageUri).into(image);
            image.setImageURI(mImageUri);
        }
    }
}