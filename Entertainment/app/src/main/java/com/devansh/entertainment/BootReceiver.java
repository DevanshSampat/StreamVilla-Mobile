package com.devansh.entertainment;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.println(Log.ASSERT,"Boot Status","Completed");
        if(new File(context.getFilesDir(),"isDownloading.txt").exists()&&DownloadFileData.getText()==null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context,RemainingDownloadService.class));
            }
            else context.startService(new Intent(context,RemainingDownloadService.class));
        }
        Log.println(Log.ASSERT,"email",UserName.getUsername(context)+"      .");
        if(UserName.getUsername(context).equals("devansh.sampat@gmail.com")&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context,RandomNotificationService.class));
        }
    }
}
