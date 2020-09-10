package com.iku;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.iku.databinding.ActivityHomeBinding;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding homeBinding;

    private FragmentManager fragmentManager;

    boolean doubleBackToExitPressedOnce = false;

    private static final String TAG = HomeActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        networkChecker();

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

    private void networkChecker() {
        if (!CheckInternet.isNetwork(HomeActivity.this)) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setView(R.layout.no_internet_dialog)
                    .setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    });
            Dialog dialog = builder.show();
            dialog.setCanceledOnTouchOutside(false);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
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

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        } else {
            if (findViewById(R.id.messageTextField) != null) {
                findViewById(R.id.messageTextField).clearFocus();
                homeBinding.animatedBottomBar.setVisibility(View.VISIBLE);
            }
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).

                show();

        new Handler()
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);

    }
}