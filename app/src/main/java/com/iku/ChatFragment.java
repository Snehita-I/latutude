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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.iku.models.ChatModel;

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

    TextInputEditText messageBox;
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
                chatViewHolder.messageText.setText(chatModel.getMessage());
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
        String message = String.valueOf(messageBox.getText());
        Log.i(TAG, "onClick: " + message);
        addMessage(message)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                Object details = ffe.getDetails();
                            }

                            // [START_EXCLUDE]
                            Log.w(TAG, "addMessage:onFailure", e);
                            return;
                            // [END_EXCLUDE]
                        }

                        // [START_EXCLUDE]
                        String result = task.getResult();
                        Log.e(TAG, "onComplete: " + result);
                        // [END_EXCLUDE]
                    }
                });
    }

    private Task<String> addMessage(String message) {
        // Create the arguments to the callable function.
        Map<String, String> data = new HashMap<>();
        data.put("message", message);

        return mFunctions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        Boolean result = (boolean) task.getResult().getData();
                        String a = String.valueOf(result);
                        return a;
                    }
                });
    }

    private class ChatViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView messageText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message);
        }
    }

}
