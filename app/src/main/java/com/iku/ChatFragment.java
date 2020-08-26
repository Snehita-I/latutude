package com.iku;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iku.databinding.FragmentChatBinding;
import com.iku.models.ChatModel;
import com.iku.models.LeaderboardModel;
import com.iku.utils.ItemClickSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.joery.animatedbottombar.AnimatedBottomBar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private RecyclerView mChatRecyclerView;

    private AnimatedBottomBar animatedBottomBar;

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("MMMM dd, yyyy");

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FirebaseFirestore db;

    private LinearLayout upvoterLayout;

    private MaterialTextView chatDay;

    private EditText messageBox;
    private ImageView sendButton, addImageButton;

    private FragmentChatBinding binding;

    private ChatAdapter chatadapter;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        binding.groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToLeaderboard = new Intent(getActivity(), LeaderboardActivity.class);
                startActivity(goToLeaderboard);
            }
        });

        FirebaseFirestore.getInstance().collection("groups").document("iku_earth").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                ArrayList<String> group = (ArrayList<String>) document.get("members");
                Log.i(TAG, "Group size: " + group.size() + group);
                binding.memberCount.setText("ikulogists: " + group.size());

            }
        });

        chatDay = view.findViewById(R.id.chatDate);
        chatDay.setVisibility(View.GONE);
        messageBox = view.findViewById(R.id.messageTextField);
        sendButton = view.findViewById(R.id.sendMessageButton);
        mChatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        addImageButton = view.findViewById(R.id.choose);
        upvoterLayout = view.findViewById(R.id.upvotesLayout);

        animatedBottomBar = getActivity().findViewById(R.id.animatedBottomBar);

        Query query = db.collection("iku_earth_messages").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();

        mChatRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        ((SimpleItemAnimator) mChatRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        linearLayoutManager.setReverseLayout(true);

        mChatRecyclerView.setLayoutManager(linearLayoutManager);

        mChatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                Log.i(TAG, "onScrolled: " + firstVisiblePosition);
                if (dy < 0) {
                    chatDay.setVisibility(View.VISIBLE);
                    chatDay.setText(sfdMainDate.format(new Date(chatadapter.getItem(firstVisiblePosition).getTimestamp())));
                } else if (dy > 0) {
                    chatDay.setVisibility(View.GONE);
                }
            }
        });

        chatadapter = new ChatAdapter(getContext(), options);
        chatadapter.startListening();
        chatadapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mChatRecyclerView.smoothScrollToPosition(0);
            }
        });
        mChatRecyclerView.setAdapter(chatadapter);
        ItemClickSupport.addTo(mChatRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            }

            @Override
            public void onItemDoubleClicked(RecyclerView recyclerView, final int position, View v) {
                boolean isLiked = false;
                int upvotesCount = chatadapter.getItem(position).getUpvoteCount();
                ArrayList<String> upvotersList = chatadapter.getItem(position).getupvoters();
                String myUID = user.getUid();
                Log.i(TAG, "onItemDoubleClicked: UPVOTECOUNT" + upvotesCount + "\nMY UID: " + myUID + "\nALL UPVOTERS: " + upvotersList);
                if (upvotesCount >= 0) {
                    for (String element : upvotersList) {
                        if (element.contains(myUID)) {
                            isLiked = true;
                            break;
                        }
                    }
                    if (!isLiked) {
                        DocumentSnapshot snapshot = chatadapter.getSnapshots().getSnapshot(position);
                        String documentID = snapshot.getId();
                        Log.i(TAG, "Document ID" + documentID);
                        db.collection("iku_earth_messages").document(documentID)
                                .update("upvoteCount", chatadapter.getItem(position).getUpvoteCount() + 1,
                                        "upvoters", FieldValue.arrayUnion(user.getUid()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                                .update("points", usersData.getPoints() + 1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.i(TAG, "INCREMENTED USER POINT " + usersData.getPoints() + 1);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    } else {
                        DocumentSnapshot snapshot = chatadapter.getSnapshots().getSnapshot(position);
                        String documentID = snapshot.getId();
                        Log.i(TAG, "Document ID" + documentID);
                        db.collection("iku_earth_messages").document(documentID)
                                .update("upvoteCount", chatadapter.getItem(position).getUpvoteCount() - 1,
                                        "upvoters", FieldValue.arrayRemove(user.getUid()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        final LeaderboardModel usersData = documentSnapshot.toObject(LeaderboardModel.class);
                                                        db.collection("users").document(chatadapter.getItem(position).getUID())
                                                                .update("points", usersData.getPoints() - 1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.i(TAG, "DECREMENTED USER POINT BY 1");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                }
                chatadapter.notifyItemChanged(position);
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
            docData.put("upvoteCount", 0);
            ArrayList<Object> upvotersArray = new ArrayList<>();
            docData.put("upvoters", upvotersArray);

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
        } else {
            Log.i(TAG, "sendTheMessage: No user login ");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
