package com.iku;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.iku.databinding.ActivityHomeBinding;

import org.jetbrains.annotations.NotNull;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding homeBinding;

    private FragmentManager fragmentManager;

    private static final String TAG = HomeActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (savedInstanceState == null) {
            homeBinding.animatedBottomBar.selectTabById(R.id.chat, true);
            fragmentManager = getSupportFragmentManager();
            ChatFragment chatFragment = new ChatFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
                    .commit();
        }

        homeBinding.animatedBottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int lastIndex, @Nullable AnimatedBottomBar.Tab lastTab, int newIndex, @NotNull AnimatedBottomBar.Tab newTab) {
                Fragment fragment = null;
                switch (newTab.getId()) {
                    case R.id.chat:
                        fragment = new ChatFragment();

                        /*Log event*/
                        Bundle chat_bundle = new Bundle();
                        chat_bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Main Chat");
                        chat_bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "View");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, chat_bundle);
                        break;

                    /*case R.id.social:
                        fragment = new SocialFragment();
                        break;*/

                    case R.id.profile:
                        fragment = new ProfileFragment();

                        /*Log event*/
                        Bundle profile_bundle = new Bundle();
                        profile_bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "My Profile");
                        profile_bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "View");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, profile_bundle);
                        break;
                }

                if (fragment != null) {
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                            .commit();
                } else {
                    Log.e(TAG, "Error in creating Fragment");
                }
            }
        });
    }
}