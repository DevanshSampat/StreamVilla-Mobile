package com.example.news;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class WebSeriesDownloader {
    private boolean found = false;
    public void downloadSeason(Context context, String dbName){
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    if(found) return;
                    found = true;
                    String name = snapshot.child(dbName.substring(0,dbName.lastIndexOf('s'))).getValue().toString();
                    String seasonNumber = dbName.substring(dbName.lastIndexOf('s')+1);
                    int numberOfEpisodes = Integer.parseInt(snapshot.child(dbName).getValue().toString());
                    int i;
                    for(i=1;i<=numberOfEpisodes;i++){
                        String replacedName = (name + " : S" + seasonNumber + "E" + i)
                                .replace(':', '_').replace(' ', '_');
                        if(!new File(context.getExternalFilesDir(null), replacedName +".mp4").exists())
                        new DownloadNotification(false).enqueueDownload(context,name+" : S"+seasonNumber+"E"+i,
                                snapshot.child(dbName+"e"+i).getValue().toString());
                        try{
                            if(!new File(context.getExternalFilesDir(null), replacedName +".srt").exists()) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.child(dbName+"e"+i+"sub").getValue().toString()));
                                request.setTitle(name + " : S" + seasonNumber + "E" + i);
                                request.setDescription("subtitles");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setVisibleInDownloadsUi(false);
                                request.setDestinationInExternalFilesDir(context, null, replacedName + ".srt");
                                DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                                manager.enqueue(request);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String temp = dbName;
                        if(temp.contains("s")) temp = temp.substring(0,temp.indexOf('s'));
                        downloadThumbnail(context,temp);
                        if(!new File(context.getFilesDir(),"isDownloading.txt").exists())
                            new DownloadNotification().goToNextTask(context,new Theme(context).isInDarkMode());
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Something went wrong, try again later", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void downloadThumbnail(Context context, String dbNameWithoutSeason){
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String name = snapshot.child(dbNameWithoutSeason).getValue().toString();
                    if(!new File(context.getExternalFilesDir(null),"image_for_"+name.replace(':','_')
                    .replace(' ','_')+".jpg").exists()){
                        String imageReference = "comedyimage"+dbNameWithoutSeason.substring("comedy".length());
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.child(imageReference).getValue().toString()));
                        request.setTitle(name);
                        request.setDescription("Thumbnail");
                        request.setDestinationInExternalFilesDir(context,null,"image_for_"+name.replace(':','_')
                                .replace(' ','_')+".jpg");
                        request.setVisibleInDownloadsUi(false);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                        DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                } catch (Exception e) {
                    //Log.println(Log.ASSERT,"exception",e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
