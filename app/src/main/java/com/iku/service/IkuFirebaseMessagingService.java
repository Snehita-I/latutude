package com.iku.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iku.HomeActivity;
import com.iku.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IkuFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String TAG = IkuFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "onMessageReceived: new incoming message.");
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        Log.d(TAG, "onMessageReceived: i am running" + message + title);

        db = FirebaseFirestore.getInstance();

        ArrayList<String> titleList = new ArrayList<>();
        ArrayList<String> messageList = new ArrayList<>();
        db.collection("iku_earth_messages").orderBy("timestamp", Query.Direction.DESCENDING).limit(4)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        titleList.add((String) document.get("userName"));
                        messageList.add((String) document.get("message"));
                    }
                    if (titleList.size()==4)
                        sendMessageNotification(title, message,titleList,messageList);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e );
            }
        });
    }

    private void sendMessageNotification(String title, String message,ArrayList titles,ArrayList messages) {
        Intent resultIntent = new Intent(this, HomeActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent piResult = PendingIntent.getActivity(this, 0, resultIntent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_iku)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(piResult);

        Notification notification = new Notification.InboxStyle(builder)
                .addLine(title + ": " + message)
                .addLine((CharSequence) titles.get(1)+": " + messages.get(2))
                .addLine((CharSequence) titles.get(2)+": " + messages.get(2))
                .addLine((CharSequence) titles.get(3)+": " + messages.get(3))
                .setBigContentTitle("Messages from ikulogists you missed")
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(121, notification);
    }

    @Override
    public void onNewToken(@NotNull String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    public void sendRegistrationToServer(String token) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (user != null) {
            user = mAuth.getCurrentUser();
            Map<String, Object> userRegistrationTokenInfo = new HashMap<>();
            userRegistrationTokenInfo.put("registrationToken", token);
            userRegistrationTokenInfo.put("uid", user.getUid());
            db.collection("registrationTokens").document(user.getUid())
                    .set(userRegistrationTokenInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }


}