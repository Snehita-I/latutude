package com.iku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.models.LeaderboardModel;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class LeaderboardActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;

    private RecyclerView mLeaderboardList;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseUser user;

    private FirebaseAuth mAuth;

    private TextView heartscount;
    private TextView playerscount;

    private ImageView backButton;

    private int totalHearts, totalPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

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
                            Log.d("Query", "Error getting documents: ", task.getException());
                        }
                    }
                });

        adapter = new FirestoreRecyclerAdapter<LeaderboardModel, LeaderboardActivity.LeaderboardViewHolder>(options) {
            @NonNull
            @Override
            public LeaderboardActivity.LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_data, parent, false);
                return new LeaderboardActivity.LeaderboardViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull LeaderboardActivity.LeaderboardViewHolder leaderboardViewHolder, int i, @NonNull LeaderboardModel leaderboardModel) {

                if (leaderboardModel.getUid().equals(user.getUid())) {
                    leaderboardViewHolder.firstNameTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                    leaderboardViewHolder.pointsTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                    final KonfettiView konfettiView = findViewById(R.id.viewConfetti);
                    leaderboardViewHolder.firstNameTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            Toast.makeText(LeaderboardActivity.this, "- drum roll -", Toast.LENGTH_SHORT).show();
                            konfettiView.build()
                                    .addColors(Color.BLUE, Color.LTGRAY, getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent))
                                    .setDirection(0.0, 359.0)
                                    .setSpeed(1f, 10f)
                                    .setFadeOutEnabled(true)
                                    .setTimeToLive(2000L)
                                    .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                                    .addSizes(new Size(8, 10f))
                                    .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                                    .streamFor(300, 5000L);

                            new CountDownTimer(1*10000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    leaderboardViewHolder.firstNameTextView.setEnabled(false);
                                }

                                public void onFinish() {
                                    leaderboardViewHolder.firstNameTextView.setEnabled(true);

                                }
                            }.start();


                        }
                    });
                }
                leaderboardViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                leaderboardViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
            }

        };

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mLeaderboardList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mLeaderboardList.setLayoutManager(linearLayoutManager);
        mLeaderboardList.setAdapter(adapter);


    }

    private class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView firstNameTextView, pointsTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.startListening();
    }
}