package com.iku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private ImageView profilePicture;
    private MaterialTextView userNameText;

    private MaterialButton logoutButton;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        profilePicture = view.findViewById(R.id.profileImage);
        userNameText = view.findViewById(R.id.userName);
        logoutButton = view.findViewById(R.id.logout_button);

        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
        if (account != null) {
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personID = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            String originalPieceOfUrl = "s96-c";
            String newPieceOfUrlToAdd = "s400-c";

            String photoPath = personPhoto.toString();
            String highRes = photoPath.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

            Log.e(TAG, "onCreateView: " + highRes);

            userNameText.setText(personName);
            Picasso.with(getActivity())
                    .load(highRes)
                    .noFade()
                    .into(profilePicture);

            Log.e(TAG, "\nAccount: " + account + "\nEmail: " + personEmail + "\nFamilyName: " + personFamilyName + "\nPhoto: " + personPhoto + "\nGiveName: " + personGivenName + "\nID: " + personID + "\nName: " + personName);

        }*/

        if (user != null) {
            String originalPieceOfUrl = "s96-c";
            String newPieceOfUrlToAdd = "s800-c";

            String personName = user.getDisplayName();
            String photoUrl = user.getPhotoUrl().toString();

            photoUrl = photoUrl.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

            photoUrl = photoUrl + "?height=500";


            Log.e(TAG, "\nUser: " + user + "\nPhoto: " + photoUrl + "\nGiveName: " + "\nName: " + personName);
            userNameText.setText(personName);
            Picasso.with(getActivity())
                    .load(photoUrl)
                    .noFade()
                    .into(profilePicture);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
