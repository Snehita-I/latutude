package com.iku;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.adapter.ChatAdapter;
import com.iku.adapter.LeaderBoardAdapter;
import com.iku.models.LeaderboardModel;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView mLeaderboardList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private LeaderBoardAdapter leaderboardadapter;

    private TextView heartscount;
    private TextView playerscount;

    private ImageView backButton;

    private int totalHearts, totalPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        backButton = findViewById(R.id.back_button);
        heartscount = findViewById(R.id.heartscount);
        playerscount = findViewById(R.id.playerscount);
        mLeaderboardList = findViewById(R.id.leaderboard_recyclerview);

        Query query = firebaseFirestore.collection("users").orderBy("points", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<LeaderboardModel> options = new FirestoreRecyclerOptions.Builder<LeaderboardModel>()
                .setQuery(query, LeaderboardModel.class)
                .build();
        mLeaderboardList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mLeaderboardList.setLayoutManager(linearLayoutManager);
        leaderboardadapter = new LeaderBoardAdapter(options);
        leaderboardadapter.startListening();
        mLeaderboardList.setAdapter(leaderboardadapter);

        firebaseFirestore.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                totalPlayers++;
                                Long userPoints = (Long) document.get("points");
                                if (userPoints != null) {
                                    int i;
                                    i = userPoints.intValue();
                                    totalHearts += i;
                                }
                            }
                            heartscount.setText(Integer.toString(totalHearts));
                            playerscount.setText(Integer.toString(totalPlayers));
                        } else {
                        }
                    }
                });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

}