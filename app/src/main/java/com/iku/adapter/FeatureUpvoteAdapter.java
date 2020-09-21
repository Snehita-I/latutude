package com.iku.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.R;
import com.iku.models.FeatureUpvoteModel;
import com.squareup.picasso.Picasso;

public class FeatureUpvoteAdapter extends FirestoreRecyclerAdapter<FeatureUpvoteModel, FeatureUpvoteAdapter.LeaderboardViewHolder> {

    private String TAG = FeatureUpvoteAdapter.class.getSimpleName();

    public FeatureUpvoteAdapter(@NonNull FirestoreRecyclerOptions<FeatureUpvoteModel> options) {
        super(options);
    }

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, DocumentSnapshot snapshot);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull LeaderboardViewHolder leaderboardViewHolder, int position, @NonNull FeatureUpvoteModel FeatureUpvoteModel) {


        leaderboardViewHolder.firstNameTextView.setText(FeatureUpvoteModel.getTitle());
        leaderboardViewHolder.descTextView.setText(FeatureUpvoteModel.getDescription());
        leaderboardViewHolder.pointsTextView.setText("Upvoted: " + FeatureUpvoteModel.getUpvote_count());
        String[] p = FeatureUpvoteModel.getImage().split("/");
        String imageLink = "https://drive.google.com/uc?export=download&id=" + p[5];
        Picasso.get().load(imageLink).into(leaderboardViewHolder.image);
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upvote_data, parent, false);
        return new FeatureUpvoteAdapter.LeaderboardViewHolder(view, mListener);
    }

    public class LeaderboardViewHolder extends RecyclerView.ViewHolder {

        private TextView firstNameTextView, pointsTextView, descTextView;
        private Button upvoteFeatureButton;
        private ImageView image;

        public LeaderboardViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            upvoteFeatureButton = itemView.findViewById(R.id.button);
            firstNameTextView = itemView.findViewById(R.id.title);
            descTextView = itemView.findViewById(R.id.description);
            pointsTextView = itemView.findViewById(R.id.requestCount);
            image = itemView.findViewById(R.id.image);

            upvoteFeatureButton.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position, getSnapshots().getSnapshot(position));
                    }
                }

            });


        }
    }


}