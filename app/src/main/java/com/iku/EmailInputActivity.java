package com.iku;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.iku.databinding.ActivityEmailInputBinding;

public class EmailInputActivity extends AppCompatActivity {

    private ActivityEmailInputBinding binding;

    private String email;
    private FirebaseAuth fAuth;
    private String TAG = EmailInputActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        fAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        binding.backButton.setOnClickListener(view -> onBackPressed());

        binding.emailNextButton.setOnClickListener(view -> {

            binding.emailNextButton.setEnabled(false);

            email = binding.enterEmail.getText().toString().trim();

            if (isValidEmail(binding.enterEmail.getText().toString().trim()) && !email.isEmpty()) {
                fAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(
                        task -> {


                            Log.d(TAG, "" + task.getResult().getSignInMethods().size());
                            if (task.getResult().getSignInMethods().size() == 0) {

                                //log event
                                Bundle signup_bundle = new Bundle();
                                signup_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                signup_bundle.putString("state", "new user");
                                mFirebaseAnalytics.logEvent("email_entered", signup_bundle);

                                // email not existed
                                //Go to Signup page
                                Intent goToNewPasswordActivity = new Intent(EmailInputActivity.this, NewPasswordInputActivity.class);
                                goToNewPasswordActivity.putExtra("email", email);
                                startActivity(goToNewPasswordActivity);

                            } else {
                                //log event
                                Bundle signin_bundle = new Bundle();
                                signin_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                                signin_bundle.putString("state", "existing user");
                                mFirebaseAnalytics.logEvent("email_entered", signin_bundle);

                                // email existed
                                // Go to Login page
                                Intent goToPasswordActivity = new Intent(EmailInputActivity.this, PasswordInputActivity.class);
                                goToPasswordActivity.putExtra("email", email);
                                startActivity(goToPasswordActivity);
                            }
                        }).addOnFailureListener(e -> {
                    binding.emailNextButton.setEnabled(true);
                    e.printStackTrace();
                    Bundle signin_bundle = new Bundle();
                    signin_bundle.putString(FirebaseAnalytics.Param.METHOD, "Email");
                    signin_bundle.putString("state", "failed to fetch method");
                    mFirebaseAnalytics.logEvent("email_entered", signin_bundle);

                });
            } else {
                Toast.makeText(EmailInputActivity.this, "Enter valid email-ID", Toast.LENGTH_SHORT).show();
                binding.emailNextButton.setEnabled(true);
            }
        });

    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.emailNextButton.setEnabled(true);
    }
}