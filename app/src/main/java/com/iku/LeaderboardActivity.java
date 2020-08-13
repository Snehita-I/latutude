package com.iku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iku.models.LeaderboardModel;

public class LeaderboardActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;

    private RecyclerView mLeaderboardList;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;

    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        
        firebaseFirestore = FirebaseFirestore.getInstance();

        mLeaderboardList=findViewById(R.id.leaderboard_recyclerview);

        Query query = firebaseFirestore.collection("dummy_groups").orderBy("points", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<LeaderboardModel> options = new FirestoreRecyclerOptions.Builder<LeaderboardModel>()
                .setQuery(query, LeaderboardModel.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<LeaderboardModel, LeaderboardActivity.LeaderboardViewHolder>(options) {
            @NonNull
            @Override
            public LeaderboardActivity.LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_data, parent, false);
                return new LeaderboardActivity.LeaderboardViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull LeaderboardActivity.LeaderboardViewHolder leaderboardViewHolder, int i, @NonNull LeaderboardModel leaderboardModel) {

                leaderboardViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName());
                leaderboardViewHolder.lastNameTextView.setText(leaderboardModel.getLastName());
                leaderboardViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
            }
        };



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
            lastNameTextView = itemView.findViewById(R.id.lastname);
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