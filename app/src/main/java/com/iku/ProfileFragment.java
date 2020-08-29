package com.iku;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amulyakhare.textdrawable.TextDrawable;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private FirebaseUser user;

    private String photoUrl;

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        initButtons();
        getUserHearts();
        getProfileDetails();

        return view;
    }

    private void initButtons() {
        profileBinding.logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            startActivity(intent);
        });

        profileBinding.settingsButton.setOnClickListener(view -> {
            Intent goToSettingsPage = new Intent(getActivity(), SettingsActivity.class);
            startActivity(goToSettingsPage);
        });
    }

    private void getProfileDetails(){
        if (user != null) {
            String originalPieceOfUrl = "s96-c";
            String newPieceOfUrlToAdd = "s800-c";

            String personName = user.getDisplayName();
            profileBinding.userName.setText(personName);
            Log.i(TAG, "Person Name: " + personName + "\n");

            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {

                photoUrl = photoUri.toString();

                photoUrl = photoUrl.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

                photoUrl = photoUrl + "?height=500";

                Log.e(TAG, "\nUser: " + user + "\nPhoto: " + photoUrl + "\nGiveName: " + "\nName: " + personName);

                Picasso.get()
                        .load(photoUrl)
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(profileBinding.profileImage, new Callback() {

                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "PICASSO Success ");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.i(TAG, "PICASSO Error ");
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
                    Log.i(TAG, "No picture:" + firstLetter + " " + secondLetter);
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
                                    profileBinding.userHearts.setText("Hearts Won: " + change.getDocument().get("points"));
                                }

                                String source = querySnapshot.getMetadata().isFromCache() ?
                                        "local cache" : "server";
                                Log.i(TAG, "Cache Hearts Won: " + source);
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
