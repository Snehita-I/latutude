package com.iku.service;


import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iku.HomeActivity;
import com.iku.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class IkuFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String TAG = IkuFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";
        try {
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        } catch (NullPointerException e) {
            Log.e(TAG, "onMessageReceived: NullPointerException: " + e.getMessage());
        }
        Log.d(TAG, "onMessageReceived: data: " + notificationData);
        Log.d(TAG, "onMessageReceived: notification body: " + notificationBody);
        Log.d(TAG, "onMessageReceived: notification title: " + notificationTitle);


        String dataType = remoteMessage.getData().get(getString(R.string.data_type));
        if (dataType.equals("direct_message")) {
            Log.d(TAG, "onMessageReceived: new incoming message.");
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String messageId = remoteMessage.getData().get("message_id");
            sendMessageNotification(title, message, messageId);
        }
    }

    private void sendMessageNotification(String title, String message, String messageId) {
        Log.d(TAG, "sendChatmessageNotification: building a chatmessage notification");

        //get the notification id
        int notificationId = buildNotificationId(messageId);

        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(this, HomeActivity.class);
        // Sets the Activity to start in a new, empty task
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        String GROUP_KEY = "com.iku.test.Notifications";

        // Instantiate a Builder object.
        NotificationCompat.Builder groupBuilder = new NotificationCompat.Builder(this, "iku-Notifications");

        groupBuilder.setContentTitle(title)
                .setContentText(message)
                .setGroupSummary(true)
                .setGroup("GROUP_IKU")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(notifyPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "iku-Notifications")
                .setSmallIcon(R.drawable.ic_iku)
                .setContentTitle(title)
                .setContentText(message)
                .setGroup("GROUP_IKU")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(notifyPendingIntent);
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1, groupBuilder.build());
        manager.notify(notificationId, builder.build());
    }

    private int buildNotificationId(String id) {
        Log.d(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for (int i = 0; i < 9; i++) {
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(TAG, "buildNotificationId: id: " + id);
        Log.d(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
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
