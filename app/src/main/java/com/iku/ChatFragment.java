package com.iku;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        mChatRecyclerview.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        ((SimpleItemAnimator) mChatRecyclerview.getItemAnimator()).setSupportsChangeAnimations(false);
        linearLayoutManager.setReverseLayout(true);
        mChatRecyclerview.setLayoutManager(linearLayoutManager);

        mChatRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                if (dy < 0) {
                    binding.chatDate.setVisibility(View.VISIBLE);
                    if (sfdMainDate.format(new Date(chatadapter.getItem(firstVisiblePosition).getTimestamp())).equals(sfdMainDate.format(new Date().getTime())))
                        binding.chatDate.setText("Today");
                    else if (DateUtils.isToday(chatadapter.getItem(firstVisiblePosition).getTimestamp() + DateUtils.DAY_IN_MILLIS)) {
                        binding.chatDate.setText("Yesterday");
                    } else
                        binding.chatDate.setText(sfdMainDate.format(chatadapter.getItem(firstVisiblePosition).getTimestamp()));
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

        mChatRecyclerview.setAdapter(chatadapter);

        ItemClickSupport.addTo(mChatRecyclerview).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            }

            @Override
            public void onItemDoubleClicked(RecyclerView recyclerView, final int position, View v) {
                boolean isLiked = false;
                int upvotesCount = chatadapter.getItem(position).getUpvoteCount();
                ArrayList<String> upvotersList = chatadapter.getItem(position).getupvoters();
                String myUID = user.getUid();
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
                        db.collection("iku_earth_messages").document(documentID)
                                .update("upvoteCount", chatadapter.getItem(position).getUpvoteCount() + 1,
                                        "upvoters", FieldValue.arrayUnion(user.getUid()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (chatadapter.getItem(position).getUID().equals(user.getUid())) {
                                            //Log event
                                            Bundle heart_params = new Bundle();
                                            heart_params.putString("type", "heart_up");
                                            heart_params.putString("messageID", documentID);
                                            heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                            heart_params.putString("action_by", user.getUid());
                                            mFirebaseAnalytics.logEvent("hearts", heart_params);
                                        } else {
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

                                                                            //Log event
                                                                            Bundle heart_params = new Bundle();
                                                                            heart_params.putString("type", "heart_up");
                                                                            heart_params.putString("messageID", documentID);
                                                                            heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                                                            heart_params.putString("action_by", user.getUid());
                                                                            mFirebaseAnalytics.logEvent("hearts", heart_params);
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
                        db.collection("iku_earth_messages").document(documentID)
                                .update("upvoteCount", chatadapter.getItem(position).getUpvoteCount() - 1,
                                        "upvoters", FieldValue.arrayRemove(user.getUid()))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (chatadapter.getItem(position).getUID().equals(user.getUid())) {
                                            //Log event
                                            Bundle params = new Bundle();
                                            params.putString("type", "heart_down");
                                            params.putString("messageID", documentID);
                                            params.putString("author_UID", chatadapter.getItem(position).getUID());
                                            params.putString("action_by", user.getUid());
                                            mFirebaseAnalytics.logEvent("hearts", params);
                                        } else {
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

                                                                            //Log event
                                                                            Bundle heart_params = new Bundle();
                                                                            heart_params.putString("type", "heart_down");
                                                                            heart_params.putString("messageID", documentID);
                                                                            heart_params.putString("author_UID", chatadapter.getItem(position).getUID());
                                                                            heart_params.putString("action_by", user.getUid());
                                                                            mFirebaseAnalytics.logEvent("hearts", heart_params);
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
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                View parentView = getLayoutInflater().inflate(R.layout.user_bottom_sheet, null);
                ChatModel chatModel = documentSnapshot.toObject(ChatModel.class);
                LinearLayout profileView = parentView.findViewById(R.id.profile_layout);
                LinearLayout deleteMessageView = parentView.findViewById(R.id.delete_layout);
                String UID = chatModel.getUID();
                if (UID.equals(user.getUid())) {
                    profileView.setVisibility(View.GONE);
                    deleteMessageView.setVisibility(View.VISIBLE);
                    deleteMessageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteMessage(documentSnapshot.getId());
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(parentView);
                    bottomSheetDialog.show();
                } else {
                    profileView.setVisibility(View.VISIBLE);
                    bottomSheetDialog.setContentView(parentView);
                    bottomSheetDialog.show();

                    profileView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent userProfileIntent = new Intent(ChatFragment.this.getContext(), UserProfileActivity.class);

                            String name = chatModel.getUserName();
                            String userUID = chatModel.getUID();
                            if (name != null) {
                                userProfileIntent.putExtra("EXTRA_PERSON_NAME", name);
                                userProfileIntent.putExtra("EXTRA_PERSON_UID", userUID);
                                ChatFragment.this.startActivity(userProfileIntent);
                            } else
                                return;
                            bottomSheetDialog.dismiss();
                        }
                    });

                }
            }
        });


        return view;
    }

    private void deleteMessage(String messageDocumentID) {
        db.collection("iku_earth_messages").document(messageDocumentID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log event
                        Bundle delete_bundle = new Bundle();
                        delete_bundle.putString("UID", user.getUid());
                        delete_bundle.putString("Name", user.getDisplayName());
                        mFirebaseAnalytics.logEvent("deleted_message", delete_bundle);

                        Toast.makeText(getActivity(), "Message deleted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
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
            }
        });

        binding.choose.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
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
        if (user != null) {
            Map<String, Object> docData = new HashMap<>();
            docData.put("message", message.trim());
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

                            //Log event
                            Bundle params = new Bundle();
                            params.putString("type", "text");
                            params.putString("uid", user.getUid());
                            mFirebaseAnalytics.logEvent("messaging", params);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        } else {
        }
    }

    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Permission Granted!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), ChatImageActivity.class);
            startActivity(i);
        }
    }

    private void getGroupMemberCount() {
        db.collection("groups").whereEqualTo("name", "iku Experiment")
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
