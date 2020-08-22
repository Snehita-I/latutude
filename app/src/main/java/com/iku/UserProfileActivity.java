package com.iku;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.textview.MaterialTextView;

public class UserProfileActivity extends AppCompatActivity {


    private ImageView profilePicture;
    private MaterialTextView nameTextView;
    private String TAG = UserProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pofile);

        String firstLetter, secondLetter;

        Bundle extras = this.getIntent().getExtras();
        String userName = extras.getString("EXTRA_PERSON_NAME");

        nameTextView = findViewById(R.id.userName);
        profilePicture = findViewById(R.id.profileImage);
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
}