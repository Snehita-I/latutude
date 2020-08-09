package com.iku;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.iku.models.ChatModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.joery.animatedbottombar.AnimatedBottomBar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private FirebaseFunctions mFunctions;

    private FirestoreRecyclerAdapter adapter;

    private RecyclerView mChatList;

    private FirebaseFirestore firebaseFirestore;

    private AnimatedBottomBar animatedBottomBar;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;

    EditText messageBox;
    MaterialButton sendButton;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mFunctions = FirebaseFunctions.getInstance();

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        messageBox = view.findViewById(R.id.messageTextField);
        sendButton = view.findViewById(R.id.sendMessageButton);
        mChatList = view.findViewById(R.id.chatRecyclerView);

        animatedBottomBar = getActivity().findViewById(R.id.animatedBottomBar);

        firebaseFirestore = FirebaseFirestore.getInstance();

        Query query = firebaseFirestore.collection("iku_earth_messages").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<ChatModel, ChatViewHolder>(options) {
            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_bubble, parent, false);
                return new ChatViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i, @NonNull ChatModel chatModel) {
                SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
                long timeStamp = chatModel.getTimestamp();

                chatViewHolder.messageText.setText(chatModel.getMessage());
                chatViewHolder.messageTime.setText(sfd.format(new Date(timeStamp)));
            }
        };

        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mChatList.smoothScrollToPosition(0);
            }
        });

        mChatList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        mChatList.setLayoutManager(linearLayoutManager);
        mChatList.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTheMessage();
            }
        });

        return view;
    }

    private void sendTheMessage() {
        final String message = String.valueOf(messageBox.getText());
        Date d = new Date();
        long timestamp = d.getTime();
        Log.i(TAG, "onClick: " + message + "\n TIME:" + timestamp);
        Map<String, Object> docData = new HashMap<>();
        docData.put("message", message);
        docData.put("timestamp", timestamp);
        docData.put("uid", user.getUid());
        docData.put("userName", user.getDisplayName());

        db.collection("iku_earth_messages")
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        messageBox.setText("");
                        messageBox.requestFocus();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private class ChatViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText, messageTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message);
            messageTime = itemView.findViewById(R.id.message_time);

        }
    }

}
