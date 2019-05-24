package com.licenta.aglomerator.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.licenta.aglomerator.R;
import com.licenta.aglomerator.activities.FriendRequestsActivity;

public class NotificationChannelsSingleton {

    private static final String FRIEND_REQUEST_CHANNEL_ID = "channel_1";
    private NotificationManagerCompat notificationManager;
    private Context currentContext;
    private static NotificationChannelsSingleton instance;

    private NotificationChannelsSingleton(Context context) {
        this.currentContext = context;
    }

    public static NotificationChannelsSingleton initInstance(Context context) {
        if (instance == null) {
            instance = new NotificationChannelsSingleton(context);
        }
        return instance;
    }

    public static NotificationChannelsSingleton getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Context not initialized. Initiate with initInstance()");
        }
        return instance;
    }

    public void createNotificationChannels() {

        notificationManager = NotificationManagerCompat.from(currentContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    FRIEND_REQUEST_CHANNEL_ID,
                    "New Friend Request",
                    NotificationManager.IMPORTANCE_HIGH
            );

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            channel1.setSound(alarmSound, attributes);
            channel1.setDescription("You have a new friend request");


            NotificationManager manager = currentContext.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);

        }
    }

    public void sendOnFriendRequestNotificationChannel(String title, String message) {

        // intent on tap on notification
        Intent activityIntent = new Intent(currentContext, FriendRequestsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(currentContext, 0, activityIntent, 0);

        // intent on tap on toast
        Intent broadcastIntent = new Intent(currentContext, NotificationReceiver.class);
        broadcastIntent.putExtra("toastMessage", "You've got a new notification !");
        PendingIntent actionIntent = PendingIntent.getBroadcast(currentContext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(currentContext.getResources(), R.drawable.logo);

        android.app.Notification notification = new NotificationCompat.Builder(currentContext, FRIEND_REQUEST_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_plus_one)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(getCircularBitmap(largeIcon))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)
                        .setBigContentTitle(title)
                        .setSummaryText("New Friend Request"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                //.setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Toast", actionIntent)
                .build();

        notificationManager.notify(1, notification);
    }

    private Bitmap getCircularBitmap(@NonNull Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
