package com.iku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.iku.adapter.OnboardingAdapter;
import com.iku.models.OnboardingScreenModel;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String TAG = OnboardingActivity.class.getSimpleName();

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private MaterialButton buttonOnboardingAction;
    private ImageButton dismissOnboardingAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        //dismiss button skips onboarding screens
        dismissOnboardingAction = findViewById(R.id.dismissOnboardingAction);

        layoutOnboardingIndicators = findViewById(R.id.layoutOnboardingIndicators);
        buttonOnboardingAction = findViewById(R.id.buttonOnboardingAction);
        setupOnboardingItems();

        final ViewPager2 onboardingViewPager = findViewById(R.id.onboardingViewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });

        buttonOnboardingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                    onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
                } else {
                    startActivity(new Intent(OnboardingActivity.this, WelcomeActivity.class));
                    finish();
                }
            }
        });

        dismissOnboardingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OnboardingActivity.this,WelcomeActivity.class));
                finish();
            }
        });
    }

    private void setupOnboardingItems() {

        List<OnboardingScreenModel> onboardingItems = new ArrayList<>();

        OnboardingScreenModel itemIkuScreen1 = new OnboardingScreenModel();
        //itemIkuScreen1.setTitle("Heading 1");
        itemIkuScreen1.setDescription("Here's your community that celebrates living more sustainably.");
        itemIkuScreen1.setImage(R.drawable.ic_undraw_celebration_0jvk);
        onboardingItems.add(itemIkuScreen1);

        OnboardingScreenModel itemIkuScreen2 = new OnboardingScreenModel();
        //itemIkuScreen2.setTitle("Heading 2");
        itemIkuScreen2.setDescription("So, share your efforts of living sustainably - no matter how small an effort.");
        itemIkuScreen2.setImage(R.drawable.ic_undraw_publish_post_vowb);
        onboardingItems.add(itemIkuScreen2);

        OnboardingScreenModel itemIkuScreen3 = new OnboardingScreenModel();
        //itemIkuScreen3.setTitle("Heading 3");
        itemIkuScreen3.setDescription("Be inspired by others how to be more sustainable.\n" + "\n" + "Got a question? Ask Away.");
        itemIkuScreen3.setImage(R.drawable.ic_undraw_faq_rjoy);
        onboardingItems.add(itemIkuScreen3);

        OnboardingScreenModel itemIkuScreen4 = new OnboardingScreenModel();
        //itemIkuScreen4.setTitle("Heading 4");
        itemIkuScreen4.setDescription("Oh, we make it fun! \n Double tap a message / post to ❤ it.\n" + "\n" + "Compete to earn highest hearts to top the community leaderboard every month.");
        itemIkuScreen4.setImage(R.drawable.ic_undraw_super_thank_you_obwk);
        onboardingItems.add(itemIkuScreen4);

        OnboardingScreenModel itemIkuScreen5 = new OnboardingScreenModel();
        //itemIkuScreen5.setTitle("Heading 5");
        itemIkuScreen5.setDescription("You win, We win, Earth wins.\n" + "\n" + "Iku - world's first social app for sustainability.");
        itemIkuScreen5.setImage(R.drawable.ic_undraw_messenger_e7iu);
        onboardingItems.add(itemIkuScreen5);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);

    }

    private void setupOnboardingIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        int childcount = layoutOnboardingIndicators.getChildCount();
        for (int i = 0; i < childcount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_active)
                );
            } else {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_inactive)
                );
            }
        }

        if (index == onboardingAdapter.getItemCount() - 1) {
            buttonOnboardingAction.setText("Join");
        } else {
            buttonOnboardingAction.setText("Next");
        }
    }
}