package com.iku.adapter;

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
import com.iku.R;
import com.iku.models.ChatModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, RecyclerView.ViewHolder> {


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;

    private ChatAdapter.OnItemClickListener listener;

    private ChatAdapter.onItemLongClickListener longClickListener;

    private Context mContext;

    private SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private String TAG = ChatAdapter.class.getSimpleName();

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull final ChatModel chatModel) {
        switch (viewHolder.getItemViewType()) {

            case MSG_TYPE_LEFT:
                ChatLeftViewHolder chatLeftViewHolder = (ChatLeftViewHolder) viewHolder;
                long timeStampLeft = chatModel.getTimestamp();

                chatLeftViewHolder.messageText.setText(chatModel.getMessage());
                chatLeftViewHolder.messageTime.setText(sfd.format(new Date(timeStampLeft)));
                chatLeftViewHolder.senderName.setText(chatModel.getUserName());
                if (chatModel.getUpvoteCount() == 0) {
                    chatLeftViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);
                } else
                    chatLeftViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                chatLeftViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                break;

            case MSG_TYPE_RIGHT:
                ChatRightViewHolder chatRightViewHolder = (ChatRightViewHolder) viewHolder;
                long timeStampRight = chatModel.getTimestamp();

                chatRightViewHolder.messageText.setText(chatModel.getMessage());
                chatRightViewHolder.messageTime.setText(sfd.format(new Date(timeStampRight)));
                if (chatModel.getUpvoteCount() == 0) {
                    chatRightViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);
                } else
                    chatRightViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                chatRightViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                break;

            case MSG_TYPE_IMAGE_LEFT:
                final ChatLeftImageViewHolder chatLeftImageViewHolder = (ChatLeftImageViewHolder) viewHolder;
                long timeStampImageLeft = chatModel.getTimestamp();

                chatLeftImageViewHolder.messageText.setText(chatModel.getMessage());
                chatLeftImageViewHolder.messageTime.setText(sfd.format(new Date(timeStampImageLeft)));
                chatLeftImageViewHolder.senderName.setText(chatModel.getUserName());
                if (chatModel.getUpvoteCount() == 0) {
                    chatLeftImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);
                } else
                    chatLeftImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                chatLeftImageViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                Picasso.get()
                        .load(chatModel.getimageUrl())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(chatLeftImageViewHolder.receiverImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatModel.getimageUrl())
                                        .noFade()
                                        .into(chatLeftImageViewHolder.receiverImage);
                            }
                        });
                break;

            case MSG_TYPE_IMAGE_RIGHT:
                final ChatRightImageViewHolder chatRightImageViewHolder = (ChatRightImageViewHolder) viewHolder;
                long timeStampImageRight = chatModel.getTimestamp();

                chatRightImageViewHolder.messageText.setText(chatModel.getMessage());
                chatRightImageViewHolder.messageTime.setText(sfd.format(new Date(timeStampImageRight)));
                if (chatModel.getUpvoteCount() == 0) {
                    chatRightImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.GONE);
                } else
                    chatRightImageViewHolder.itemView.findViewById(R.id.upvotesLayout).setVisibility(View.VISIBLE);
                chatRightImageViewHolder.upvoteCount.setText(String.valueOf(chatModel.getUpvoteCount()));
                Picasso.get()
                        .load(chatModel.getimageUrl())
                        .noFade()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(chatRightImageViewHolder.sentImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(chatModel.getimageUrl())
                                        .noFade()
                                        .into(chatRightImageViewHolder.sentImage);
                            }
                        });
                break;

        }
    }

    public class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, senderName, upvoteCount;

        public ChatLeftViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            senderName = itemView.findViewById(R.id.sender_name);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                        longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return false;
                }
            });


        }
    }

    public class ChatLeftImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, senderName, upvoteCount;
        private ImageView receiverImage;

        public ChatLeftImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            senderName = itemView.findViewById(R.id.sender_name);
            receiverImage = itemView.findViewById(R.id.receivedImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                        longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return false;
                }
            });


        }
    }

    public class ChatRightImageViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, upvoteCount;
        private ImageView sentImage;

        public ChatRightImageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            sentImage = itemView.findViewById(R.id.sentImage);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);

        }
    }

    public class ChatRightViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime, upvoteCount;

        public ChatRightViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);

        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(ChatAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemLongClickListener {
        void onItemLongClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemLongClickListener(ChatAdapter.onItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public ChatAdapter(Context context, @NonNull FirestoreRecyclerOptions<ChatModel> options) {
        super(options);
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
            return new ChatRightViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);
            return new ChatLeftViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_image, parent, false);
            return new ChatLeftImageViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_image, parent, false);
            return new ChatRightImageViewHolder(view);
        } else
            return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text")) {
            return MSG_TYPE_RIGHT;
        } else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("text")) {
            return MSG_TYPE_LEFT;
        } else if (!getItem(position).getUID().equals(user.getUid()) && getItem(position).getType().equals("image")) {
            return MSG_TYPE_IMAGE_LEFT;
        } else if (getItem(position).getType().equals("image") && getItem(position).getimageUrl() != null && getItem(position).getUID().equals(user.getUid()))
            return MSG_TYPE_IMAGE_RIGHT;
        else
            return 0;
    }


}
