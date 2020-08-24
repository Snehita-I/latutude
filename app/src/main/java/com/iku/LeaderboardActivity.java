package com.iku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class LeaderboardActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;

    private RecyclerView mLeaderboardList;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private TextView heartscount;
    private TextView playerscount;

    private ImageView backButton;

    private int totalHearts,totalPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        firebaseFirestore = FirebaseFirestore.getInstance();

        backButton=findViewById(R.id.back_button);
        heartscount = findViewById(R.id.heartscount);
        playerscount = findViewById(R.id.playerscount);
        mLeaderboardList = findViewById(R.id.leaderboard_recyclerview);

        Query query = firebaseFirestore.collection("dummy_groups").orderBy("points", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<LeaderboardModel> options = new FirestoreRecyclerOptions.Builder<LeaderboardModel>()
                .setQuery(query, LeaderboardModel.class)
                .build();

        firebaseFirestore.collection("dummy_groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.i("Query", document.getId() + " => " + document.getString("firstName") +
                                        document.getString("lastName"));
                                totalPlayers++;
                                Long l=(Long)document.get("points");
                                Log.i("l",Long.toString(l));
                                int i;
                                i = l.intValue();
                                totalHearts+=i;
                            }
                            Log.i("Total players",Integer.toString(totalPlayers));
                            Log.i("Total hearts",Integer.toString(totalHearts));
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

        private MaterialTextView firstNameTextView, lastNameTextView, pointsTextView;

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