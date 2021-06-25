package com.example.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteMultipleVideosActivity extends AppCompatActivity {

    private ArrayList<File> fileArrayList;
    private RecyclerView recyclerView;
    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_multiple);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getAllVideos();
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter("FILES_DELETED"));
        fileArrayList = new ArrayList<>();
        if(new Theme(this).isInDarkMode()) {
            findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
            findViewById(R.id.select_all).setBackgroundColor(Color.WHITE);
            ((TextView)findViewById(R.id.text)).setTextColor(Color.WHITE);
        }
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        getAllVideos();
        ((CheckBox)findViewById(R.id.select_all)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    ((DeleteVideoAdapter)recyclerView.getAdapter()).setDeleteAll(b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void getAllVideos(){
        fileArrayList.clear();
        AssetManager manager = getAssets();
        File file = new File("/storage/emulated/0/android/data/com.example.news/files");
        File[] downloadedFiles = file.listFiles();
        if(downloadedFiles!=null) {
            downloadedFiles = sortFiles(downloadedFiles);
            for (File tempFile : downloadedFiles) {
                if(tempFile.getName().endsWith(".mp4")) fileArrayList.add(tempFile);
            }
        }
        if(fileArrayList.size()==0){
            Toast.makeText(this, "No Downloads to clear", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(recyclerView.getAdapter()==null) recyclerView.setAdapter(new DeleteVideoAdapter(fileArrayList));
        else ((DeleteVideoAdapter)recyclerView.getAdapter()).setFileArrayList(fileArrayList);
    }
    private File[] sortFiles(File[] files) {
        for(int i=0;i<files.length-1;i++){
            for(int j=0;j<files.length-1-i;j++){
                if(files[j].getName().compareTo(files[j+1].getName())>0)
                {
                    File tempFile = files[j];
                    files[j] = files[j+1];
                    files[j+1]=tempFile;
                }
            }
        }
        return files;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getAllVideos();
    }

    public void delete(View view) {
        try{
            ((DeleteVideoAdapter)recyclerView.getAdapter()).deleteFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}