package com.iku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.textview.MaterialTextView;

public class UserPofileActivity extends AppCompatActivity {


    private ImageView profilePicture;
    private MaterialTextView nameTextView;
    private String TAG = UserPofileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pofile);

        Bundle extras = this.getIntent().getExtras();
        String userName = extras.getString("EXTRA_PERSON_NAME");
        nameTextView = findViewById(R.id.userName);
        nameTextView.setText(userName);
        Log.i(TAG, "onCreate: " + userName);

        profilePicture = findViewById(R.id.profileImage);

        String firstLetter, secondLetter;

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
}