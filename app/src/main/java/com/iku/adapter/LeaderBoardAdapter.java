package com.iku.adapter;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iku.LeaderboardActivity;
import com.iku.R;
import com.iku.models.LeaderboardModel;

public class LeaderBoardAdapter extends FirestorePagingAdapter<LeaderboardModel, RecyclerView.ViewHolder> {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LeaderBoardAdapter.OnItemClickOfSameUidListener listener;
    private LeaderBoardAdapter.OnItemClickOfDiffUidListener listenerDiff;
    public static final int CURRENT_USER = 0;
    public static final int OTHER_USER = 1;
    public LeaderBoardAdapter(@NonNull FirestorePagingOptions<LeaderboardModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull LeaderboardModel leaderboardModel) {
        switch (viewHolder.getItemViewType()) {
            case CURRENT_USER:
                LeaderBoardAdapter.CurrentUserViewHolder currentUserViewHolder = (CurrentUserViewHolder) viewHolder;
                currentUserViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                currentUserViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
                break;
            case OTHER_USER:
                LeaderBoardAdapter.OtherUserViewHolder otherUserViewHolder = (OtherUserViewHolder) viewHolder;
                otherUserViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                otherUserViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
                break;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view;
        if (viewType == OTHER_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.otheruser_leaderboard_data, parent, false);
            return new LeaderBoardAdapter.OtherUserViewHolder(view);
        } else if (viewType == CURRENT_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_data, parent, false);
            return new LeaderBoardAdapter.CurrentUserViewHolder(view);
        }else
            return null;
        
    }


    public void setOnItemClickOfSameUidListener(LeaderBoardAdapter.OnItemClickOfSameUidListener listener) {
        this.listener = listener;
    }

    public void setOnItemClickOfDiffUidListener(LeaderBoardAdapter.OnItemClickOfDiffUidListener listenerDiff) {
        this.listenerDiff = listenerDiff;
    }

    public interface OnItemClickOfSameUidListener {
        void onItemOfSameUidClick();
    }

    public interface OnItemClickOfDiffUidListener {
        void onItemOfDiffUidClick(String Uid, String name);
    }

    public class OtherUserViewHolder extends RecyclerView.ViewHolder{
        public MaterialTextView firstNameTextView, pointsTextView;

        public OtherUserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
        }
    }
    public class CurrentUserViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView firstNameTextView, pointsTextView;

        public CurrentUserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                LeaderboardModel leaderboardModel = getItem(getAdapterPosition()).toObject(LeaderboardModel.class);
                if (leaderboardModel.getUid().equals(user.getUid())) {
                    listener.onItemOfSameUidClick();
                    new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            itemView.setEnabled(false);
                        }

                        public void onFinish() {
                            itemView.setEnabled(true);
                        }
                    }.start();
                } else
                    listenerDiff.onItemOfDiffUidClick(leaderboardModel.getUid(), leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
            });
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).get("uid").equals(user.getUid())) {
            return CURRENT_USER;
        }else
            return OTHER_USER;

    }
}


