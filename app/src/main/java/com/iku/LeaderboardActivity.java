package com.iku;

import android.content.Intent;
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
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.adapter.ChatAdapter;
import com.iku.adapter.LeaderBoardAdapter;
import com.iku.models.ChatModel;
import com.iku.models.LeaderboardModel;
import com.iku.utils.ItemClickSupport;

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
    private MaterialTextView firstNameTextView;
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
        firstNameTextView = findViewById(R.id.firstname);

        Query query = firebaseFirestore.collection("users").orderBy("points", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(5)
                .build();
        FirestorePagingOptions<LeaderboardModel> options = new FirestorePagingOptions.Builder<LeaderboardModel>()
                .setQuery(query, config,  new SnapshotParser<LeaderboardModel>() {
                    @NonNull
                    @Override
                    public LeaderboardModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        LeaderboardModel leaderboardModel = snapshot.toObject(LeaderboardModel.class);
                        String uid = snapshot.getId();
                        leaderboardModel.setUid(uid);
                        return leaderboardModel;
                    }
                })
                .setLifecycleOwner(this)
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

        leaderboardadapter.setOnItemClickListener((documentSnapshot, position) -> {
            LeaderboardModel leaderboardModel = documentSnapshot.toObject(LeaderboardModel.class);
            if(leaderboardModel.getUid().equals(user.getUid())){
                final KonfettiView viewConfetti = findViewById(R.id.viewConfetti);
                //Log event
                Bundle easter_bundle = new Bundle();
                easter_bundle.putString("easter_egg", "Leaderboard");
                easter_bundle.putString("UID", leaderboardModel.getUid());
                easter_bundle.putString("Name", leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                mFirebaseAnalytics.logEvent("easter_egg_found", easter_bundle);

                Toast.makeText(LeaderboardActivity.this, "- drum roll -", Toast.LENGTH_SHORT).show();
                viewConfetti.build()
                        .addColors(Color.BLUE, Color.LTGRAY, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent))
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 8f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                        .addSizes(new Size(10, 10f))
                        .setPosition(-50f, viewConfetti.getWidth() + 50f, -50f, -50f)
                        .streamFor(300, 5000L);

                new CountDownTimer(1 * 10000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        firstNameTextView.setEnabled(false);
                    }

                    public void onFinish() {
                        firstNameTextView.setEnabled(true);
                    }
                }.start();

            }
        });
    }

}