package com.iku;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
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
import com.squareup.picasso.Picasso;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatImageActivity extends AppCompatActivity {

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SimpleDateFormat dateFormatter;
    private Uri mImageUri;
    private File file;
    private File sourceFile;
    private File destFile;
    private int PICK_IMAGE = 1;
    private String docId, message, imageUrl;

    private int STORAGE_PERMISSION_CODE = 10;

    private String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //private ActivityChatImageBinding chatImageBinding;

    private String TAG = ChatImageActivity.class.getSimpleName();

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }

    private static String getTempFilename(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }

    private ImageButton sendImageChatbtn;

    private ImageView backButton;

    private PhotoView chosenImage;

    private EditText messageEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //chatImageBinding = ActivityChatImageBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_chat_image);
//        setContentView(chatImageBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference(user.getUid());
        db = FirebaseFirestore.getInstance();
        messageEntered = findViewById(R.id.messageTextField);
        backButton = findViewById(R.id.backbutton);
        sendImageChatbtn = findViewById(R.id.sendMessageButton);
        chosenImage = findViewById(R.id.chosenImage);
        dateFormatter = new SimpleDateFormat(
                DATE_FORMAT, Locale.US);

        file = new File(String.valueOf(getCacheDir()));
        if (!file.exists()) {
            file.mkdirs();
        }
        Bundle extras = getIntent().getExtras();
        docId = extras.getString("documentId");
        Toast.makeText(getApplicationContext(), docId, Toast.LENGTH_LONG).show();
        if (docId.equals("default")) {
            Toast.makeText(getApplicationContext(), "enterd1", Toast.LENGTH_LONG).show();
            openFileChooser();
        } else if (!docId.equals("default")) {
            message = extras.getString("message");
            messageEntered.setText(message);
            imageUrl = extras.getString("imageUrl");
            Picasso.get().load(imageUrl).into(chosenImage);
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendImageChatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!messageEntered.getText().toString().isEmpty()) {

                    if (docId.equals("default")) {

                        uploadFile(messageEntered.getText().toString());

                        sendImageChatbtn.setClickable(false);

                    } else if (!docId.equals("default")) {
                        updateMessage(docId, messageEntered.getText().toString());
                    }

                } else
                    Toast.makeText(ChatImageActivity.this, "Caption such empty..much wow!", Toast.LENGTH_SHORT).show();
            }
        });

//        initItems();
//        initButtons();
//
//        openFileChooser();
    }

    private void initItems() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference(user.getUid());
        db = FirebaseFirestore.getInstance();

        dateFormatter = new SimpleDateFormat(
                DATE_FORMAT, Locale.US);

        file = new File(String.valueOf(getCacheDir()));
        if (!file.exists()) {
            file.mkdirs();
        }

    }

//    private void initButtons() {
//
//        chatImageBinding.backbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
//
//        chatImageBinding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!chatImageBinding.messageTextField.getText().toString().isEmpty()) {
//
//                    uploadFile(chatImageBinding.messageTextField.getText().toString());
//
//                    chatImageBinding.sendMessageButton.setClickable(false);
//
//                } else
//                    Toast.makeText(ChatImageActivity.this, "Caption such empty..much wow!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

    private void uploadFile(String message) {
        Uri finalUri = Uri.fromFile(destFile);
        if (finalUri != null) {
            StorageReference imageRef = mStorageRef.child("IKU-img_"
                    + dateFormatter.format(new Date()) + ".png");
            UploadTask uploadTask = imageRef.putFile(finalUri);
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
                    docData.put("edited", false);

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
                                                        }
                                                    } else {
                                                    }
                                                }
                                            });


                                    messageEntered.setText("");
                                    Toast.makeText(ChatImageActivity.this, "Aren't you the best", Toast.LENGTH_LONG).show();
                                    messageEntered.requestFocus();
                                    ChatImageActivity.super.onBackPressed();

                                    //Log event
                                    Bundle params = new Bundle();
                                    params.putString("type", "image");
                                    params.putString("uid", user.getUid());
                                    mFirebaseAnalytics.logEvent("messaging", params);
                                }
                            })
                            .addOnFailureListener(e -> {
                                sendImageChatbtn.setClickable(true);
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

            sourceFile = new File(getPathFromGooglePhotosUri(mImageUri));

            destFile = new File(file, "IKU-img_"
                    + dateFormatter.format(new Date()) + ".png");


            try {
                copyFile(sourceFile, destFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = decodeFile(destFile);
            chosenImage.setImageBitmap(bitmap);
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

    private Bitmap decodeFile(File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        destFile = new File(file, "IKU-img_"
                + dateFormatter.format(new Date()) + ".png");

        try {
            FileOutputStream out = new FileOutputStream(destFile);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    public String getPathFromGooglePhotosUri(Uri uriPhoto) {
        if (uriPhoto == null)
            return null;

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uriPhoto, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);

            String tempFilename = getTempFilename(this);
            output = new FileOutputStream(tempFilename);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return tempFilename;
        } catch (IOException ignored) {
            // Nothing we can do
        } finally {
            closeSilently(input);
            closeSilently(output);
        }
        return null;
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    private void updateMessage(String messageDocumentID, String message) {

        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("edited", true);
        db.collection("iku_earth_messages").document(messageDocumentID)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messageEntered.setText("");
                        Toast.makeText(ChatImageActivity.this, "Message edited", Toast.LENGTH_LONG).show();
                        messageEntered.requestFocus();
                        ChatImageActivity.super.onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

}
