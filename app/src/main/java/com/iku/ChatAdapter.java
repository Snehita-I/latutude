package com.iku;

import android.content.Context;
import android.util.Log;
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
import com.iku.models.ChatModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, RecyclerView.ViewHolder> {

    private String TAG = ChatAdapter.class.getSimpleName();

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull ChatModel chatModel) {
        switch (viewHolder.getItemViewType()) {
            case MSG_TYPE_LEFT:
                ChatLeftViewHolder chatLeftViewHolder = (ChatLeftViewHolder) viewHolder;
                SimpleDateFormat sfdLeft = new SimpleDateFormat("hh:mm a");
                long timeStampLeft = chatModel.getTimestamp();

                chatLeftViewHolder.messageText.setText(chatModel.getMessage());
                chatLeftViewHolder.messageTime.setText(sfdLeft.format(new Date(timeStampLeft)));
                chatLeftViewHolder.senderName.setText(chatModel.getUserName());
                break;
            case MSG_TYPE_RIGHT:
                ChatRightViewHolder chatRightViewHolder = (ChatRightViewHolder) viewHolder;
                SimpleDateFormat sfdRight = new SimpleDateFormat("hh:mm a");
                long timeStampRight = chatModel.getTimestamp();

                chatRightViewHolder.messageText.setText(chatModel.getMessage());
                chatRightViewHolder.messageTime.setText(sfdRight.format(new Date(timeStampRight)));
                break;
            case MSG_TYPE_IMAGE_LEFT:
                ChatLeftImageViewHolder chatLeftImageViewHolder = (ChatLeftImageViewHolder) viewHolder;
                SimpleDateFormat sfdImageLeft = new SimpleDateFormat("hh:mm a");
                long timeStampImageLeft = chatModel.getTimestamp();

                chatLeftImageViewHolder.messageText.setText(chatModel.getMessage());
                chatLeftImageViewHolder.messageTime.setText(sfdImageLeft.format(new Date(timeStampImageLeft)));
                chatLeftImageViewHolder.senderName.setText(chatModel.getUserName());
                Picasso.with(mContext).load(chatModel.getimageUrl()).placeholder(R.drawable.iku).resize(1080,1080).into(chatLeftImageViewHolder.receiverImage);
                break;
        }
    }

    public class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, senderName;

        public ChatLeftViewHolder(@NonNull View itemView) {
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

    public class ChatLeftImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, senderName;
        private ImageView receiverImage;

        public ChatLeftImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            senderName = itemView.findViewById(R.id.sendername);
            receiverImage = itemView.findViewById(R.id.receivedImage);

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

    public class ChatRightViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime;

        public ChatRightViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);

        }
    }


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;

    private ChatAdapter.OnItemClickListener listener;

    private Context mContext;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_image, parent, false);
            return new ChatLeftImageViewHolder(view);
        } else
            return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text")) {
            Log.i(TAG, "getItemViewType: " + getItem(position).getUID() + "\n" + getItem(position).getType());
            return MSG_TYPE_RIGHT;
        } else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text")) {
            Log.i(TAG, "getItemViewType: " + getItem(position).getUID() + "\n" + getItem(position).getType());
            return MSG_TYPE_LEFT;
        } else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image")) {
            Log.i(TAG, "getItemViewType: " + getItem(position).getUID() + "\n" + getItem(position).getType() + "\n" + getItem(position).getimageUrl());
            return MSG_TYPE_IMAGE_LEFT;
        }   // else if (getItem(position).getType().equals("image") && getItem(position).getimageUrl() != null && getItem(position).getUID().equals(user.getUid()))
        //    return MSG_TYPE_IMAGE_RIGHT;
        else
            return 0;
    }


}
