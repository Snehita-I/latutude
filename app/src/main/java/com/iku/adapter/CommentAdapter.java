package com.iku.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.iku.R;
import com.iku.models.CommentModel;

public class CommentAdapter extends FirestorePagingAdapter<CommentModel, CommentAdapter.CommentViewHolder> {

    public CommentAdapter(@NonNull FirestorePagingOptions<CommentModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentViewHolder commentViewHolder, int position, @NonNull CommentModel commentModel) {
        commentViewHolder.commentTextView.setText(commentModel.getComment());
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_data, parent, false);
        return new CommentAdapter.CommentViewHolder(view);
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView commentTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.comment);
        }
    }
}