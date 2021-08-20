package com.example.news;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.WEDNESDAY;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {
    private Notification notification;
    private String name;
    private String image;
    private String date;
    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.println(Log.ASSERT,"Pending Intent","Delivered");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        File file;
        if(day==MONDAY||day==WEDNESDAY||day==FRIDAY||day==SUNDAY){
            file = new File(context.getFilesDir(),"webseries.txt");
        }
        else file = new File(context.getFilesDir(),"DownloadedVideos.txt");
        ArrayList<String> contentList = new ArrayList<>();
        String str;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(file.getName())));
            while((str=br.readLine())!=null) contentList.add(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int randomNumber = calendar.get(DAY_OF_MONTH)+ calendar.get(DAY_OF_WEEK)+calendar.get(HOUR_OF_DAY)
                +calendar.get(MINUTE)+calendar.get(SECOND)+calendar.get(MILLISECOND);
        randomNumber = randomNumber%contentList.size();
        String title = contentList.get(randomNumber);
        String description = "Want to try something new? Let us help!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"general");
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.app_icon));
        builder.setColor(Color.RED);
        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle(title);
        builder.setAutoCancel(true);
        builder.setContentText(description);
        Intent searchIntent = new Intent(context,SearchActivity.class);
        searchIntent.putExtra("signed_in",true);
        searchIntent.putExtra("search",title);
        searchIntent.putExtra("open",true);
        builder.setContentIntent(PendingIntent.getActivity(context,45,searchIntent,PendingIntent.FLAG_UPDATE_CURRENT));
        if(new File(context.getFilesDir(),title.replace(' ','+')+".txt").exists()){
            description = "Pick up from where you left off";
            builder.setContentText(description);
            if(file.getName().equals("webseries.txt")) {
                int season;
                int episode;
                try {
                    FileInputStream fis = context.openFileInput(title.replace(' ','+')+".txt");
                    str = new BufferedReader(new InputStreamReader(fis)).readLine();
                    season = Integer.parseInt(str.substring(0,str.indexOf('\t')));
                    str = str.substring(str.indexOf('\t')+1);
                    episode = Integer.parseInt(str.substring(0,str.indexOf('\t')));
                    String name = title+" : "+"S"+season+"E"+episode;
                    if((file = new File(context.getExternalFilesDir(null),".screenshot_of_"+name.replace(':','_')
                            .replace(' ','_')+".jpeg")).exists()){
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        builder.setLargeIcon(bitmap);
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if((file = new File(context.getExternalFilesDir(null),".screenshot_of_"+title.replace(':','_')
                    .replace(' ','_')+".jpeg")).exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                builder.setLargeIcon(bitmap);
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null));
            }
        }
        NotificationManagerCompat.from(context).notify(207, builder.build());
    }
}
