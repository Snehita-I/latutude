package com.iku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.iku.databinding.ActivityViewPostBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

public class ViewPostActivity extends AppCompatActivity {

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("hh:mm a, MMMM dd, yyyy");

    private Bundle extras;

    private ActivityViewPostBinding viewPostBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPostBinding = ActivityViewPostBinding.inflate(getLayoutInflater());
        setContentView(viewPostBinding.getRoot());

        extras = this.getIntent().getExtras();

        setImage();
        setDetails();

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
}