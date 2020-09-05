package com.iku;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.databinding.FragmentProfileBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private FirebaseUser user;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String photoUrl;

    private MaterialTextView userHeartsTextView;

    private FragmentProfileBinding profileBinding;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = profileBinding.getRoot();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        userHeartsTextView = view.findViewById(R.id.userHearts);

        initButtons();
        getUserHearts();
        getProfileDetails();

        return view;
    }

    private void initButtons() {
        profileBinding.logoutButton.setOnClickListener(view -> {
            //log event
            //Remove UID if this event is erroring out in Analytics
            Bundle logout_bundle = new Bundle();
            logout_bundle.putString("uid", user.getUid());
            mFirebaseAnalytics.logEvent("logout", logout_bundle);
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            startActivity(intent);
        });

        profileBinding.settingsButton.setOnClickListener(view -> {
            Intent goToSettingsPage = new Intent(getActivity(), SettingsActivity.class);
            startActivity(goToSettingsPage);
        });
    }

    private void getProfileDetails() {
        if (user != null) {
            String originalPieceOfUrl = "s96-c";
            String newPieceOfUrlToAdd = "s800-c";

            String personName = user.getDisplayName();
            profileBinding.userName.setText(personName);

            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {

                photoUrl = photoUri.toString();

                photoUrl = photoUrl.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

                photoUrl = photoUrl + "?height=500";

                storePictureToDB(photoUrl);

                Picasso.get()
                        .load(photoUrl)
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(profileBinding.profileImage, new Callback() {

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(photoUrl)
                                        .noFade()
                                        .into(profileBinding.profileImage);
                            }
                        });
            } else {
                String displayName = user.getDisplayName();
                String firstLetter, secondLetter;
                if (displayName != null) {
                    firstLetter = String.valueOf(displayName.charAt(0));
                    secondLetter = displayName.substring(displayName.indexOf(' ') + 1, displayName.indexOf(' ') + 2).trim();
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .width(200)
                            .height(200)
                            .endConfig()
                            .buildRect(firstLetter + secondLetter, Color.DKGRAY);

                    profileBinding.profileImage.setImageDrawable(drawable);
                }
            }
        }
    }

    private void storePictureToDB(String photoUrl) {
        if (user != null) {

            Map<String, Object> docData = new HashMap<>();
            docData.put("iamgeUrl", photoUrl);

            db.collection("users").document(user.getUid())
                    .update(docData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Log.i(TAG, "onSuccess: ");
                            //Log event
                            Bundle down_params = new Bundle();
                            down_params.putString("received_picture", "User has google or FB picture");
                            mFirebaseAnalytics.logEvent("profile_picture", down_params);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    private void getUserHearts() {
        if (user != null) {
            db.collection("users").whereEqualTo("uid", user.getUid())
                    .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    userHeartsTextView.setText("Hearts Won: " + change.getDocument().get("points"));
                                }

                                String source = querySnapshot.getMetadata().isFromCache() ?
                                        "local cache" : "server";
                            }

                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        profileBinding = null;
    }
}
