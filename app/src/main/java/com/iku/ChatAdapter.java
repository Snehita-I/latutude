package com.iku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iku.models.ChatModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, ChatAdapter.ChatViewHolder> {


    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, senderName;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            senderName = itemView.findViewById(R.id.sendername);

            senderName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }
    }


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;

    private ChatAdapter.OnItemClickListener listener;

    private Context mContext;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(ChatAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public ChatAdapter(Context context, @NonNull FirestoreRecyclerOptions<ChatModel> options) {
        super(options);
        mContext = context;
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
        View view = null;

        if (viewType == MSG_TYPE_RIGHT)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
        else if (viewType == MSG_TYPE_LEFT)
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


}
