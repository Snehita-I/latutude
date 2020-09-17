package com.iku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        firebaseAnalytics = Firebase.analytics
        val user = mAuth.currentUser
        initButtons(user)
        getDetails(user)
    }

    private fun initButtons(user: FirebaseUser?) {
        back_button.setOnClickListener { onBackPressed() }
        save_button.setOnClickListener {
            val bio = userBio.text.toString().trim()
            val optionalLink = linkInBio.text.toString().trim()
            if (user != null) {
                saveUserDetails(bio, optionalLink, user.uid)
            }
        }
    }

    private fun saveUserDetails(bio: String, link: String, uid: String) {
        val data = mapOf(
                "userBio" to bio,
                "userBioLink" to link
        )
        db.collection("users").document(uid)
                .update(data).addOnSuccessListener { onBackPressed() }.addOnFailureListener {
                    firebaseAnalytics.logEvent("profile_update") {
                        param(FirebaseAnalytics.Param.METHOD, "User tried updating Info")
                        param("state", "Failure updating")
                    }
                }
    }

    private fun getDetails(user: FirebaseUser?) {
        if (user != null) {
            editUserNameField.setText(user.displayName)
            db.collection("users").document(user.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        userBio.setText(document.get("userBio") as String?)
                        linkInBio.setText(document.get("userBioLink") as String?)
                    }
                }
            }
        }
    }
}