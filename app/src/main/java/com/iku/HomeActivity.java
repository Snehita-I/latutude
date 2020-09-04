package com.iku;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

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

    private ChatFragment chatFragment;

    private static final String TAG = HomeActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        boolean hasMenuKey = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        findViewById(android.R.id.content).getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                findViewById(android.R.id.content).getRootView().getWindowVisibleDisplayFrame(r);
                int heightDiff = findViewById(android.R.id.content).getRootView().getRootView().getHeight() - (r.bottom - r.top);

                boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
                boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

                if (hasBackKey && hasHomeKey) {
                    if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                        //ok now we know the keyboard is up...
                        homeBinding.animatedBottomBar.post(new Runnable() {
                            @Override
                            public void run() {
                                //ok now we know the keyboard is down...
                                homeBinding.animatedBottomBar.setVisibility(View.GONE);
                            }
                        });

                    } else {
                        homeBinding.animatedBottomBar.post(new Runnable() {
                            @Override
                            public void run() {
                                //ok now we know the keyboard is down...
                                homeBinding.animatedBottomBar.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    if (findViewById(R.id.messageTextField) != null) {
                        if (findViewById(R.id.messageTextField).hasFocus())
                            homeBinding.animatedBottomBar.setVisibility(View.GONE);
                    }
                }
            }
        });

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
                        hideKeyboard(HomeActivity.this);
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.messageTextField).clearFocus();
        homeBinding.animatedBottomBar.setVisibility(View.VISIBLE);
    }
}