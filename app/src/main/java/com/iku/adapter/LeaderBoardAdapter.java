package com.iku.adapter;

import android.os.CountDownTimer;
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

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LeaderBoardAdapter.OnItemClickOfSameUidListener listener;
    private LeaderBoardAdapter.OnItemClickOfDiffUidListener listenerDiff;
    public static final int CURRENT_USER = 0;
    public static final int OTHER_USER = 1;
    public static final int FIRST_PLACE = 2;
    public static final int SECOND_PLACE = 3;
    public static final int THIRD_PLACE = 4;

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
            case FIRST_PLACE:
                LeaderBoardAdapter.FirstPlaceViewHolder firstPlaceViewHolder = (FirstPlaceViewHolder) viewHolder;
                firstPlaceViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                firstPlaceViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
                break;
            case SECOND_PLACE:
                LeaderBoardAdapter.SecondPlaceViewHolder secondPlaceViewHolder = (SecondPlaceViewHolder) viewHolder;
                secondPlaceViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                secondPlaceViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
                break;
            case THIRD_PLACE:
                LeaderBoardAdapter.ThirdPlaceViewHolder thirdPlaceViewHolder = (ThirdPlaceViewHolder) viewHolder;
                thirdPlaceViewHolder.firstNameTextView.setText(leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
                thirdPlaceViewHolder.pointsTextView.setText(String.valueOf(leaderboardModel.getPoints()));
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
        } else if (viewType == FIRST_PLACE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.first_place_leaderboard, parent, false);
            return new LeaderBoardAdapter.FirstPlaceViewHolder(view);
        }else if (viewType == SECOND_PLACE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.second_place_leaderboard, parent, false);
            return new LeaderBoardAdapter.SecondPlaceViewHolder(view);
        }else if (viewType == THIRD_PLACE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.third_place_leaderboard, parent, false);
            return new LeaderBoardAdapter.ThirdPlaceViewHolder(view);
        }
        else
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

    public class OtherUserViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView firstNameTextView, pointsTextView;

        public OtherUserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                LeaderboardModel leaderboardModel = getItem(getAdapterPosition()).toObject(LeaderboardModel.class);
                listenerDiff.onItemOfDiffUidClick(leaderboardModel.getUid(), leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
            });
        }
    }

    public class FirstPlaceViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView firstNameTextView, pointsTextView;

        public FirstPlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                LeaderboardModel leaderboardModel = getItem(getAdapterPosition()).toObject(LeaderboardModel.class);
                listenerDiff.onItemOfDiffUidClick(leaderboardModel.getUid(), leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
            });
        }
    }

    public class SecondPlaceViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView firstNameTextView, pointsTextView;

        public SecondPlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                LeaderboardModel leaderboardModel = getItem(getAdapterPosition()).toObject(LeaderboardModel.class);
                listenerDiff.onItemOfDiffUidClick(leaderboardModel.getUid(), leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
            });
        }
    }

    public class ThirdPlaceViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView firstNameTextView, pointsTextView;

        public ThirdPlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstname);
            pointsTextView = itemView.findViewById(R.id.pointsText);
            itemView.setOnClickListener(view -> {
                LeaderboardModel leaderboardModel = getItem(getAdapterPosition()).toObject(LeaderboardModel.class);
                listenerDiff.onItemOfDiffUidClick(leaderboardModel.getUid(), leaderboardModel.getFirstName() + " " + leaderboardModel.getLastName());
            });
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
                }
            });
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).get("uid").equals(user.getUid()))
            return CURRENT_USER;
        if (position == 0)
            return FIRST_PLACE;
        if (position == 1)
            return SECOND_PLACE;
        if (position == 2)
            return THIRD_PLACE;
        else
            return OTHER_USER;
    }
}

