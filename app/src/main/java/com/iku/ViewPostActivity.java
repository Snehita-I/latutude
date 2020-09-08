package com.iku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.iku.databinding.ActivityViewPostBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ViewPostActivity extends AppCompatActivity {

    private String imageUrl;

    private ActivityViewPostBinding viewPostBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPostBinding = ActivityViewPostBinding.inflate(getLayoutInflater());
        setContentView(viewPostBinding.getRoot());

        setImage();

    }

    private void setImage() {

        Bundle extras = this.getIntent().getExtras();

        String userName = extras.getString("EXTRA_PERSON_NAME");
        imageUrl = extras.getString("EXTRA_IMAGE_URL");

        if (userName != null && imageUrl != null) {
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