package com.iku;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iku.databinding.ActivityReportBugBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportBugActivity extends AppCompatActivity {


    private SimpleDateFormat formatter;
    private EditText feedbackText;
    private FirebaseAuth fAuth;
    private FirebaseFirestore firebaseFirestore;
    private String html;
    private String feedbackText_val;
    private String type;
    private String to;
    private Button button;
    private String TAG;
    private String subject;
    private FirebaseUser user;
    private StorageReference mStorageRef;

    ImageView d1, d2, d3;
    EditText messageEntered;
    Button upload;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private ImageView img1, img2, img3, img4;
    String myArray[] = new String[3];
    Uri UriArray[] = new Uri[3];

    private int counter = 0;
    List<Uri> myList = new ArrayList<>();
    List<String> finalUrl = new ArrayList<>();

    Uri mainUri;

    private Date d;

    private ActivityReportBugBinding reportBugBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reportBugBinding = ActivityReportBugBinding.inflate(getLayoutInflater());
        setContentView(reportBugBinding.getRoot());

        d = new Date();

        TAG = "ReportBug";
        firebaseFirestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        messageEntered = (EditText) findViewById(R.id.feedbackText);
        upload = (Button) findViewById(R.id.submitButton);
        img1 = (ImageView) findViewById(R.id.firstImage);
        img2 = (ImageView) findViewById(R.id.secondImage);
        img3 = (ImageView) findViewById(R.id.thirdImage);
        img4 = (ImageView) findViewById(R.id.hiddenImageView);
        d1 = (ImageView) findViewById(R.id.delete1);
        d2 = (ImageView) findViewById(R.id.delete2);
        d3 = (ImageView) findViewById(R.id.delete3);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mStorageRef = FirebaseStorage.getInstance().getReference("feedback");

        img1.setImageResource(R.drawable.addimages);
        img2.setEnabled(false);
        img3.setEnabled(false);
        d1.setVisibility(View.INVISIBLE);
        d2.setVisibility(View.INVISIBLE);
        d3.setVisibility(View.INVISIBLE);

        final ImageView[] imageBoxes = {img1, img2, img3, img4};
        final ImageView[] deleteButtons = {d1, d2, d3};

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ReportBugActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(0);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(i, 0);
                }
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ReportBugActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(1);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(i, 1);
                }
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ReportBugActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(2);
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(i, 2);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToStorage();
            }
        });


        d1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int k = 0, lastImage = 0;
                myList.remove(k);
                for (int i = k; i < imageBoxes.length - 1; i++) {
                    if (imageBoxes[i + 1].getDrawable() != null) {

                        imageBoxes[i].setImageDrawable(imageBoxes[i + 1].getDrawable());
                        imageBoxes[i].setEnabled(false);
                        deleteButtons[i].setVisibility(View.VISIBLE);
                        deleteButtons[i].setEnabled(true);
                        imageBoxes[i + 1].setImageDrawable(null);
                        if (i <= 1) {
                            deleteButtons[i + 1].setVisibility(View.INVISIBLE);
                            deleteButtons[i + 1].setEnabled(false);
                        }
                        lastImage = i;

                    } else break;

                }
                imageBoxes[lastImage].setEnabled(true);
                if (lastImage <= 2) {
                    deleteButtons[lastImage].setEnabled(false);
                    deleteButtons[lastImage].setVisibility(View.INVISIBLE);
                }

            }
        });
        d2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int k = 1, lastImage = 0;
                myList.remove(k);
                for (int i = k; i < imageBoxes.length - 1; i++) {
                    if (imageBoxes[i + 1].getDrawable() != null) {

                        imageBoxes[i].setImageDrawable(imageBoxes[i + 1].getDrawable());
                        imageBoxes[i].setEnabled(false);
                        deleteButtons[i].setVisibility(View.VISIBLE);
                        deleteButtons[i].setEnabled(true);
                        imageBoxes[i + 1].setImageDrawable(null);
                        if (i <= 1) {
                            deleteButtons[i + 1].setVisibility(View.INVISIBLE);
                            deleteButtons[i + 1].setEnabled(false);
                        }
                        lastImage = i;

                    } else break;

                }
                imageBoxes[lastImage].setEnabled(true);
                if (lastImage <= 2) {
                    deleteButtons[lastImage].setEnabled(false);
                    deleteButtons[lastImage].setVisibility(View.INVISIBLE);
                }

            }
        });
        d3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int k = 2, lastImage = 0;
                myList.remove(k);
                for (int i = k; i < imageBoxes.length - 1; i++) {
                    if (imageBoxes[i + 1].getDrawable() != null) {

                        imageBoxes[i].setImageDrawable(imageBoxes[i + 1].getDrawable());
                        imageBoxes[i].setEnabled(false);
                        deleteButtons[i].setVisibility(View.VISIBLE);
                        deleteButtons[i].setEnabled(true);
                        imageBoxes[i + 1].setImageDrawable(null);
                        if (i <= 1) {
                            deleteButtons[i + 1].setVisibility(View.INVISIBLE);
                            deleteButtons[i + 1].setEnabled(false);
                        }
                        lastImage = i;

                    } else break;

                }
                imageBoxes[lastImage].setEnabled(true);
                if (lastImage <= 2) {
                    deleteButtons[lastImage].setEnabled(false);
                    deleteButtons[lastImage].setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView[] imageBoxes = {img1, img2, img3, img4};
        ImageView[] deleteButtons = {d1, d2, d3};

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            myList.add(uri);
            imageBoxes[requestCode].setImageDrawable(null);
            imageBoxes[requestCode].setImageURI(uri);
            deleteButtons[requestCode].setEnabled(true);
            imageBoxes[requestCode].setEnabled(false);
            deleteButtons[requestCode].setVisibility(View.VISIBLE);
            if (requestCode <= 2) {
                imageBoxes[requestCode + 1].setImageResource(R.drawable.addimages);
                imageBoxes[requestCode + 1].setEnabled(true);
                if (requestCode <= 1) {
                    deleteButtons[requestCode + 1].setEnabled(false);
                    deleteButtons[requestCode + 1].setVisibility(View.INVISIBLE);
                }
            }


        } else
            onBackPressed();
    }


    private String getFileExtension(Uri uri) {
        if (uri != null) {
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } else return null;
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws
            FileNotFoundException {
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


    private void uploadToStorage() {

        for (Uri uri : myList) {
            try {
                final Bitmap imageSelected = decodeUri(this, uri, 1080);
                mainUri = getImageUri(ReportBugActivity.this, imageSelected);

                final StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mainUri));
                UploadTask uploadTask = imageRef.putFile(uri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                counter++;

                                finalUrl.add(uri.toString());

                                Toast.makeText(ReportBugActivity.this, "uri added", Toast.LENGTH_LONG).show();
                                if (counter == myList.size()) {
                                    uploadToDB();
                                }
                            }
                        });
                    }


                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadToDB() {

        Map<String, Object> docData = new HashMap<>();

        long timestamp = d.getTime();
        formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

        feedbackText_val = messageEntered.getText().toString();
        to = "tech@printola.in";
        subject = "bug reported by " + user.getDisplayName();
        html = "<h3>Bug details</h3>" +
                "<table> " +
                "<tr><th>Name</th> <th>" + user.getDisplayName() + "</th> </tr>" +
                "<tr> <th>Version</th> " + "<th>" + BuildConfig.VERSION_CODE + "</th> </tr> " +
                "<tr> <th>Message</th> <th>" + feedbackText_val + "</th> </tr> " +
                "<tr> <th>UID</th> <th>" + user.getUid() + "</th> </tr> " +
                "<tr> <th>Email ID</th> <th>" + user.getEmail() + "</th> </tr> " +
                "<tr> <th>Time</th> <th>" + d.getTime() + "</th> </tr>" +
                " </table>";
        type = "bug";

        docData.put("to: ", to);
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("attachments", finalUrl);
        nestedData.put("html", html);
        nestedData.put("subject", subject);
        docData.put("message", nestedData);
        docData.put("type", "bug");
        docData.put("uid", user.getUid());
        docData.put("timeStamp", new Timestamp(new Date()));

        db.collection("mail").document()
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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

    private void requestStoragePermission(int code) {

        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, code);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 0);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 1)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 1);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 2)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i, 2);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }


}