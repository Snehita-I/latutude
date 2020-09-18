package com.iku

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amulyakhare.textdrawable.TextDrawable
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.util.*

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
        setPicture()
        checkNameEdit(user)
        userBio.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                characterCount.text = (140 - s.toString().length).toString()
            }
        })
    }

    private fun setPicture() {
        val profileUrl = intent.extras?.get("EXTRA_PROFILE_URL")
        if (profileUrl != null) {
            Picasso.get().load(profileUrl.toString())
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(profileImage, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception) {
                            Picasso.get()
                                    .load(profileUrl.toString())
                                    .noFade()
                                    .into(profileImage)
                        }
                    })

        } else {
            val profileName: String = intent.extras?.get("EXTRA_PROFILE_NAME") as String
            val firstLetter = profileName[0].toString()
            val secondLetter: String = profileName.substring(profileName.indexOf(' ') + 1, profileName.indexOf(' ') + 2).trim { it <= ' ' }
            val drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(200)
                    .height(200)
                    .endConfig()
                    .buildRect(firstLetter + secondLetter, Color.DKGRAY)

            profileImage.setImageDrawable(drawable)
        }
    }

    private fun initButtons(user: FirebaseUser?) {
        back_button.setOnClickListener { onBackPressed() }
        save_button.setOnClickListener {
            val name = editUserNameField.text.toString().trim().capitalize(Locale.ROOT)
            val bio = userBio.text.toString().trim()
            val optionalLink = linkInBio.text.toString().trim()
            if (name.isNotEmpty() && user != null) {
                saveUserDetails(bio, optionalLink, user, name)
            }
        }
    }

    private fun saveUserDetails(bio: String, link: String, user: FirebaseUser, name: String) {

        val parts = name.split(" ").toMutableList()
        val firstName = parts.firstOrNull()?.capitalize(Locale.ROOT)
        parts.removeAt(0)
        val lastName = parts.joinToString(" ").capitalize(Locale.ROOT)
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

        db.collection("users").document(user.uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val nameCheck = document.get("nameEdited") as Boolean?
                    if (nameCheck != true) {
                        val nameData = mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "nameEdited" to true,
                                "nameUpdatedTime" to FieldValue.serverTimestamp()
                        )
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            db.collection("users").document(user.uid)
                                    .update(nameData).addOnSuccessListener { }
                        }
                    }
                }
            }
        }


        val data = mapOf(
                "userBio" to bio,
                "userBioLink" to link
        )

        db.collection("users").document(user.uid)
                .update(data).addOnSuccessListener { onBackPressed() }.addOnFailureListener {
                    firebaseAnalytics.logEvent("profile_update") {
                        param(FirebaseAnalytics.Param.METHOD, "User tried updating Info")
                        param("state", "Failure updating")
                    }
                }
    }

    private fun checkNameEdit(user: FirebaseUser?) {
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val nameCheck = document.get("nameEdited") as Boolean?
                        if (nameCheck == true) {
                            editUserName.visibility = View.GONE
                            userName.visibility = View.VISIBLE
                            userName.text =user.displayName
                        } else
                            editUserName.visibility =View.VISIBLE
                    }
                }
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