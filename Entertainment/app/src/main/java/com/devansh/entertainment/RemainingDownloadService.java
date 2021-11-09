package com.devansh.entertainment;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class RemainingDownloadService extends Service {
    public RemainingDownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkDownloadNotification();
        Intent downloadIntent = new Intent(getApplicationContext(),QueuedDownloadsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),432,downloadIntent,(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"personal")
                .setSmallIcon(R.drawable.notification)
                .setColor(Color.RED)
                .setContentTitle("Downloading videos")
                .setContentText("Tap to see the list")
                .setContentIntent(pendingIntent);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) startForeground(444,builder.build());
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) stopForeground(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter("STOP_DOWNLOAD_SERVICE"));
        return START_STICKY;
    }

    private void checkDownloadNotification() {
        if(new File(getApplicationContext().getFilesDir(),"isDownloading.txt").exists()&&DownloadFileData.getText()==null)
            new DownloadNotification().generateDownloadNotification(getApplicationContext(),null,new Theme(getApplicationContext()).isInDarkMode());
        else if(!new File(getApplicationContext().getFilesDir(),"isDownloading.txt").exists()) {
            try {
                stopForeground(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkDownloadNotification();
            }
        },10000);
    }
}