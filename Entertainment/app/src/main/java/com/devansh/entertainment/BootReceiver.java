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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.HOUR_OF_DAY,13);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmingIntent = new Intent(context,NotificationReceiver.class);
        alarmingIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,alarmingIntent,
                Build.VERSION.SDK_INT>=Build.VERSION_CODES.S? PendingIntent.FLAG_IMMUTABLE :PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        Log.println(Log.ASSERT,"Alarm",calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+" at "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
    }
}
