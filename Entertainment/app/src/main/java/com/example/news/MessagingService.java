package com.example.news;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.NetworkOnMainThreadException;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Handler;

public class MessagingService extends FirebaseMessagingService {
    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(getApplicationContext(),"general");
        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setSmallIcon(R.drawable.notification);
        builder.setColor(Color.RED);
        builder.setAutoCancel(true);
        Map<String,String> map = remoteMessage.getData();
        Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
        if(!map.containsKey("search")) return;
        for(String key : map.keySet()) intent.putExtra(key,map.get(key));
        builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(),960,intent,PendingIntent.FLAG_UPDATE_CURRENT));
        if(remoteMessage.getNotification().getImageUrl()==null) NotificationManagerCompat.from(getApplicationContext()).notify(960, builder.build());
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = Picasso.with(getApplicationContext()).load(remoteMessage.getNotification().getImageUrl()).get();
                        builder.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null));
                        builder.setLargeIcon(bitmap);
                        NotificationManagerCompat.from(getApplicationContext()).notify(960,builder.build());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NetworkOnMainThreadException ignored){
                        NotificationManagerCompat.from(getApplicationContext()).notify(960,builder.build());
                    }
                }
            }).start();
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
