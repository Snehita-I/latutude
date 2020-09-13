package com.iku

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_email_input.*

class EmailInputActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_input)

        mAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = Firebase.analytics
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        back_button.setOnClickListener { onBackPressed() }
        email_next_button.setOnClickListener {
            email_next_button.isEnabled = false
            val email = enter_email.text.toString().trim()

            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email-ID", Toast.LENGTH_SHORT).show()
                email_next_button.isEnabled = true
                return@setOnClickListener
            }
            checkUser(email)
        }
    }
    private fun checkUser(email: String) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.result.signInMethods!!.size == 0) {
                        firebaseAnalytics.logEvent("email_entered") {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("state", "new user")
                        }
                        val goToNewPasswordActivity = Intent(this@EmailInputActivity, NewPasswordInputActivity::class.java)
                        goToNewPasswordActivity.putExtra("email", email)
                        startActivity(goToNewPasswordActivity)
                    } else {
                        firebaseAnalytics.logEvent("email_entered") {
                            param(FirebaseAnalytics.Param.METHOD, "Email")
                            param("state", "existing user")
                        }
                        val goToPasswordActivity = Intent(this@EmailInputActivity, PasswordInputActivity::class.java)
                        goToPasswordActivity.putExtra("email", email)
                        startActivity(goToPasswordActivity)
                    }
                }
                .addOnFailureListener { e ->
                    email_next_button.isEnabled = true
                    e.printStackTrace()
                    firebaseAnalytics.logEvent("email_entered") {
                        param(FirebaseAnalytics.Param.METHOD, "Email")
                        param("state", "failed to fetch method")
                    }
                }
    }
    override fun onResume() {
        super.onResume()
        email_next_button.isEnabled = true
    }
}