package com.licenta.aglomerator.notifications;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.licenta.aglomerator.activities.MainActivity;

public class NotificationTimerSingleton extends CountDownTimer {

    private final String TAG = "NotificationTimerSingleton";
    private static NotificationTimerSingleton instance;
    private DatabaseReference notificationsReference;

    private NotificationTimerSingleton(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        notificationsReference = FirebaseDatabase.getInstance().getReference().child("new_notifications");
    }

    public static NotificationTimerSingleton initInstance(long millisInFuture, long countDownInterval) {
        if (instance == null) {
            return new NotificationTimerSingleton(millisInFuture, countDownInterval);
        }
        return instance;
    }

    public static NotificationTimerSingleton getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Parameters not initialized. Initialize with initInstance");
        }
        return instance;
    }

    @Override
    public void onTick(long millisUntilFinished) {

        final String currentUserUid = MainActivity.currentUser.getUuid().toString();

        notificationsReference.child(currentUserUid)
                .child("friend_requests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                NotificationChannelsSingleton notificationSingleton = NotificationChannelsSingleton.getInstance();
                                long notificationsNumber = dataSnapshot.getChildrenCount();
                                StringBuilder notificationMessage = new StringBuilder();
                                notificationMessage.append("You have ").append(notificationsNumber).append(" new friend request/s\n\n");

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String senderUsername = ds.child("user_from").getValue(String.class);
                                    notificationMessage.append(senderUsername)
                                            .append(" send you a friend request")
                                            .append("\n");

                                    notificationsReference
                                            .child(currentUserUid)
                                            .child("friend_requests")
                                            .child(ds.getKey())
                                            .removeValue();
                                }
                                notificationSingleton.sendOnFriendRequestNotificationChannel("New Friend Requests. Tap to view", notificationMessage.substring(0, notificationMessage.length() - 1));
                            } catch (Exception ex) {
                                Log.d(TAG, "NotificationTimerSingleton exception: " + ex.toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "notificationsReference databaseError: " + databaseError.toString());
                    }
                });

    }

    @Override
    public void onFinish() {
        Log.d(TAG, "NotificationTimerSingleton finished");

    }


}
