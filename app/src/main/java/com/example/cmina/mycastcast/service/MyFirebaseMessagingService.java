package com.example.cmina.mycastcast.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.cmina.mycastcast.R;
import com.example.cmina.mycastcast.activity.ListActivity;
import com.example.cmina.mycastcast.util.SaveSharedPreference;
import com.google.firebase.messaging.RemoteMessage;


/**
 * Created by cmina on 2017-02-14.
 */

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    int requestID = (int) System.currentTimeMillis();

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        sendNotification(remoteMessage.getData().get("message"));
        Log.e("getMessage", remoteMessage.getData().get("message"));

    }

    private void sendNotification(String messageBody) {
        String[] array;

        array = messageBody.split("`");

        Intent intent = new Intent(this, ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("CAST_DB_NUMBER", Integer.parseInt(array[2]));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestID /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.caticon)
                .setContentTitle(array[1])
                .setContentText(array[0])
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        //  PlayerConstants.CAST_DB_NUMBER = Integer.parseInt(array[2]);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(requestID /* ID of notification */, notificationBuilder.build());
    }

}
