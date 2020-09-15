package com.iku.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.iku.LeaderboardActivity;
import com.iku.R;
import com.iku.models.ChatModel;
import com.iku.models.LeaderboardModel;

import java.text.SimpleDateFormat;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class LeaderBoardAdapter extends FirestoreRecyclerAdapter<LeaderboardModel, RecyclerView.ViewHolder> {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LeaderBoardAdapter.OnItemClickListener listener;

    public LeaderBoardAdapter(@NonNull FirestoreRecyclerOptions<LeaderboardModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull LeaderboardModel leaderboardModel) {
        LeaderBoardAdapter.LeaderboardViewHolder leaderboardViewHolder = (LeaderboardViewHolder) viewHolder;
        leaderboardViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
        leaderboardViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));

        if(leaderboardModel.getUid().equals(user.getUid())){
            leaderboardViewHolder.firstNameTextView.setTextColor(Color.parseColor("#11b7b7"));
            leaderboardViewHolder.pointsTextView.setTextColor(Color.parseColor("#11b7b7"));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_data, parent, false);
        return new LeaderBoardAdapter.LeaderboardViewHolder(view);
    }

    public class LeaderboardViewHolder extends RecyclerView.ViewHolder{
        public MaterialTextView firstNameTextView,pointsTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
    public void setOnItemClickListener(LeaderBoardAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
}


