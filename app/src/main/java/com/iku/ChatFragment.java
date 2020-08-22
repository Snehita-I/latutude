package com.iku;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iku.models.ChatModel;
import com.iku.utils.ItemClickSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.joery.animatedbottombar.AnimatedBottomBar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private RecyclerView mChatList;

    private FirebaseFirestore firebaseFirestore;

    private AnimatedBottomBar animatedBottomBar;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;

    private EditText messageBox;
    private ImageView sendButton, addImageButton;


    private ChatAdapter chatadapter;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        messageBox = view.findViewById(R.id.messageTextField);
        sendButton = view.findViewById(R.id.sendMessageButton);
        mChatList = view.findViewById(R.id.chatRecyclerView);
        addImageButton = view.findViewById(R.id.choose);

        animatedBottomBar = getActivity().findViewById(R.id.animatedBottomBar);

        firebaseFirestore = FirebaseFirestore.getInstance();

        Query query = firebaseFirestore.collection("iku_earth_messages").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();

        mChatList.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        mChatList.setLayoutManager(linearLayoutManager);

        chatadapter = new ChatAdapter(getContext(), options);
        chatadapter.startListening();
        chatadapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mChatList.smoothScrollToPosition(0);
            }
        });
        mChatList.setAdapter(chatadapter);
        ItemClickSupport.addTo(mChatList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.d("ITEM CLICK", "Item single clicked " + chatadapter.getItem(position).getMessage());
            }

            @Override
            public void onItemDoubleClicked(RecyclerView recyclerView, int position, View v) {
                Log.d("ITEM CLICK", "Item double clicked " + chatadapter.getItem(position).getUserName());
            }
        });

        chatadapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent userProfileIntent = new Intent(getContext(), UserProfileActivity.class);

                ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
                String id = documentSnapshot.getId();
                String name = chatModel.getUserName();
                Log.i(TAG, "DOCUMENT ID: " + id);
                if (name != null) {
                    userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                    startActivity(userProfileIntent);
                } else
                    return;
                //Toast displaying the document id

                Toast.makeText(getActivity(),
                        "Position: " + position + " ID: " + id + "Name" + name, Toast.LENGTH_LONG).show();

            }
        });

        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    addImageButton.setVisibility(View.VISIBLE);
                } else {
                    addImageButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = String.valueOf(messageBox.getText());
                if (!message.isEmpty()) {
                    sendTheMessage(message);
                    messageBox.setText("");
                    messageBox.requestFocus();
                } else {
                    Log.i(TAG, "No message entered!");
                }
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToImageSend = new Intent(getActivity(), ChatImageActivity.class);
                startActivity(goToImageSend);
            }
        });

        return view;
    }


    private void sendTheMessage(String message) {
        Date d = new Date();
        long timestamp = d.getTime();
        Log.i(TAG, "onClick: " + message + "\n TIME:" + timestamp);
        if (user != null) {
            Map<String, Object> docData = new HashMap<>();
            docData.put("message", message);
            docData.put("timestamp", timestamp);
            docData.put("uid", user.getUid());
            docData.put("type", "text");
            docData.put("userName", user.getDisplayName());

            db.collection("iku_earth_messages")
                    .add(docData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }
        else {
            Log.i(TAG, "sendTheMessage: No user login ");
        }
    }
}
