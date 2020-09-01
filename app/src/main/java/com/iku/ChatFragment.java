package com.iku;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iku.adapter.ChatAdapter;
import com.iku.databinding.FragmentChatBinding;
import com.iku.models.ChatModel;
import com.iku.models.LeaderboardModel;
import com.iku.utils.ItemClickSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private SimpleDateFormat sfdMainDate = new SimpleDateFormat("MMMM dd, yyyy");

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private MaterialTextView memberCount;

    private FirebaseFirestore db;

    private FragmentChatBinding binding;

    private RecyclerView mChatRecyclerview;

    private BottomSheetBehavior mBottomSheetBehavior;

    private LinearLayout mBottomSheet;

    private ChatAdapter chatadapter;

    private int STORAGE_PERMISSION_CODE = 10;

    public ChatFragment() {
        // Required empty public constructor
    }

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        memberCount = view.findViewById(R.id.memberCount);

        mChatRecyclerview = view.findViewById(R.id.chatRecyclerView);
        mBottomSheet = view.findViewById(R.id.user_bottom_sheet);

        initItems();
        initButtons();
        watchTextBox();
        getGroupMemberCount();


        Query query = db.collection("iku_earth_messages").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();

        binding.chatRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        ((SimpleItemAnimator) binding.chatRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        linearLayoutManager.setReverseLayout(true);
        binding.chatRecyclerView.setLayoutManager(linearLayoutManager);

        binding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                Log.i(TAG, "onScrolled: " + firstVisiblePosition);
                if (dy < 0) {
                    binding.chatDate.setVisibility(View.VISIBLE);
                    binding.chatDate.setText(sfdMainDate.format(new Date(chatadapter.getItem(firstVisiblePosition).getTimestamp())));
                } else if (dy > 0) {
                    binding.chatDate.setVisibility(View.GONE);
                }
            }
        });

        chatadapter = new ChatAdapter(getContext(), options);
        chatadapter.startListening();
        chatadapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mChatRecyclerview.smoothScrollToPosition(0);
            }
        });
        binding.chatRecyclerView.setAdapter(chatadapter);
        ItemClickSupport.addTo(binding.chatRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
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

                                                                        //Log event
                                                                        Bundle params = new Bundle();
                                                                        params.putString("heart_count", "heart up");
                                                                        mFirebaseAnalytics.logEvent("heart_up", params);
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

                                                                        //Log event
                                                                        Bundle params = new Bundle();
                                                                        params.putString("heart_count", "heart down");
                                                                        mFirebaseAnalytics.logEvent("heart_down", params);
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


        chatadapter.setOnItemLongClickListener(new ChatAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {
                Log.i(TAG, "Long Click");
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                View parentView = getLayoutInflater().inflate(R.layout.user_bottom_sheet, null);
                LinearLayout profileView = parentView.findViewById(R.id.profile_layout);
                profileView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent userProfileIntent = new Intent(ChatFragment.this.getContext(), UserProfileActivity.class);

                        ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
                        String id = documentSnapshot.getId();
                        String name = chatModel.getUserName();
                        Log.i(TAG, "DOCUMENT ID: " + id);
                        if (name != null) {
                            userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                            ChatFragment.this.startActivity(userProfileIntent);
                        } else
                            return;
                        //Toast displaying the document id

                        Toast.makeText(ChatFragment.this.getActivity(),
                                "Position: " + position + " ID: " + id + "Name" + name, Toast.LENGTH_LONG).show();
                    }
                });
                bottomSheetDialog.setContentView(parentView);
                bottomSheetDialog.show();
            }
        });



        return view;
    }

    private void initItems() {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        binding.chatDate.setVisibility(View.GONE);

    }

    private void initButtons() {

        binding.groupIcon.setOnClickListener(view -> {
            Intent goToLeaderboard = new Intent(getActivity(), LeaderboardActivity.class);
            startActivity(goToLeaderboard);

            /*Log event*/
            Bundle leaderboard_bundle = new Bundle();
            leaderboard_bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Leaderboard");
            leaderboard_bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "View");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, leaderboard_bundle);
        });

        binding.sendMessageButton.setOnClickListener(view -> {
            final String message = String.valueOf(binding.messageTextField.getText());
            if (!message.isEmpty()) {
                sendTheMessage(message);
                binding.messageTextField.setText("");
                binding.messageTextField.requestFocus();
            } else {
                Log.i(TAG, "No message entered!");
            }
        });

        binding.choose.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else {
                Intent goToImageSend = new Intent(getActivity(), ChatImageActivity.class);
                startActivity(goToImageSend);
            }
        });
    }

    private void watchTextBox() {
        binding.messageTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    binding.choose.setVisibility(View.VISIBLE);
                } else {
                    binding.choose.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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

                            //Log event
                            Bundle params = new Bundle();
                            params.putString("messaging", "message sent");
                            mFirebaseAnalytics.logEvent("messaging", params);
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

    private void getGroupMemberCount() {
        db.collection("groups").whereEqualTo("name", "iku Experiment")
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                ArrayList<String> group = (ArrayList<String>) change.getDocument().get("members");
                                memberCount.setText("Ikulogists: " + group.size());
                            }
                        }
                    }
                });
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent goToImageSend = new Intent(getActivity(), ChatImageActivity.class);
                startActivity(goToImageSend);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
