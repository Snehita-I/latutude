package com.iku;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.models.ChatModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, ChatAdapter.ChatViewHolder> {


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<ChatModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int position, @NonNull ChatModel chatModel) {
        SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
        long timeStamp = chatModel.getTimestamp();

        chatViewHolder.messageText.setText(chatModel.getMessage());
        chatViewHolder.messageTime.setText(sfd.format(new Date(timeStamp)));
        chatViewHolder.senderName.setText(chatModel.getUserName());

    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MSG_TYPE_RIGHT)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getUID().equals(user.getUid()))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, senderName;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            senderName = itemView.findViewById(R.id.sendername);

        }
    }

}
