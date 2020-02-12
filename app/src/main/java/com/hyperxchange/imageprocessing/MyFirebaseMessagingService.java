
package com.hyperxchange.imageprocessing;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG_CLASS = FirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        String message = remoteMessage.getNotification().getBody();
        String title = remoteMessage.getNotification().getTitle();

        Intent intent = null;

        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putString("title", title);

        Log.d(TAG, "Message " + message);
        Log.d(TAG, "Title " + title);

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message " + message);
            Log.d(TAG, "Title " + title);

            if (title.equals("camera"))
            {
                intent = new Intent(MyFirebaseMessagingService.this, TakeImage.class);

            }else{
                Log.d(TAG, "Not camera " + title);

            }
        }

        if (intent != null) {
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }



    }


}
