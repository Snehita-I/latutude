package com.iku.adapter;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iku.R;
import com.iku.models.LeaderboardModel;

public class LeaderBoardAdapter extends FirestorePagingAdapter<LeaderboardModel, RecyclerView.ViewHolder> {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LeaderBoardAdapter.OnItemClickListener listener;

    public LeaderBoardAdapter(@NonNull FirestorePagingOptions<LeaderboardModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull LeaderboardModel leaderboardModel) {
        LeaderBoardAdapter.LeaderboardViewHolder leaderboardViewHolder = (LeaderboardViewHolder) viewHolder;
        leaderboardViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
        leaderboardViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
        if (leaderboardModel.getUid().equals(user.getUid())) {
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

    public class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView firstNameTextView, pointsTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                LeaderboardModel leaderboardModel = getItem(getAdapterPosition()).toObject(LeaderboardModel.class);
                if (leaderboardModel.getUid().equals(user.getUid())) {
                    Log.i("TEST", "onClick => Current user : " + leaderboardModel.getUid());
                    listener.onItemClick();
                    new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            itemView.setEnabled(false);
                        }
                        public void onFinish() {
                            itemView.setEnabled(true);
                        }
                    }.start();
                }
            });
        }
    }

    public void setOnItemClickListener(LeaderBoardAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick();
    }
}


