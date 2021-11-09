package com.devansh.entertainment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class EpisodePickerActivity extends AppCompatActivity {
    private int count;
    private String[] links;
    private String[] subtitles;
    private String dbName;
    private String name;
    private String imageURL;
    private String[] size;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_picker);
        if(new Theme(this).isInDarkMode()) findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        name = getIntent().getStringExtra("name");
        dbName = getIntent().getStringExtra("dbName");
        imageURL = getIntent().getStringExtra("image");
        String header = name;
        header = header.substring(0,header.lastIndexOf('S'));
        header = header + "Season "+name.substring(name.lastIndexOf('S')+1);
        ((TextView)findViewById(R.id.header)).setText(header);
        final RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        FirebaseDatabase.getInstance().getReference("content").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    count = Integer.parseInt(snapshot.child(dbName).getValue().toString());
                    size = new String[count];
                    for(int i=0;i<count;i++){
                        try {
                            File file = new File(getFilesDir(), dbName + "e" + (i + 1) + ".txt");
                            if (file.exists()) {
                                FileInputStream fileInputStream = openFileInput(dbName + "e" + (i + 1) + ".txt");
                                BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
                                size[i] = br.readLine();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e){
                    count = 0;
                }
                links = new String[count];
                subtitles = new String[count];
                for(int i=0;i<count;i++){
                    try{
                        links[i] = snapshot.child(dbName+"e"+(i+1)).getValue().toString();
                    } catch (Exception exception) {
                        links[i] = "";
                    }
                    try{
                        subtitles[i] = snapshot.child(dbName+"e"+(i+1)+"sub").getValue().toString();
                    } catch (Exception exception) {
                        subtitles[i] = "";
                    }
                }
                EpisodeAdapter episodeAdapter = new EpisodeAdapter(EpisodePickerActivity.this,name,imageURL,links,subtitles,dbName,count);
                episodeAdapter.setSize(size);
                recyclerView.setAdapter(episodeAdapter);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(size==null) size = new String[count];
                        try {
                            for(int i=0;i<count;i++){
                                URLConnection connection = new URL(links[i]).openConnection();
                                connection.connect();
                                double size = connection.getContentLengthLong();
                                String string = "";
                                String index = "MB";
                                size = size/(1024*1024);
                                if(size>=1024){
                                    size = size/1024;
                                    index = "GB";
                                }
                                string = String.valueOf(size);
                                try{
                                    string = string.substring(0,string.indexOf('.')+3);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                EpisodePickerActivity.this.size[i] = string+" "+index;
                                FileOutputStream fileOutputStream = openFileOutput(dbName+"e"+(i+1)+".txt",MODE_PRIVATE);
                                fileOutputStream.write((EpisodePickerActivity.this.size[i]+"\n").getBytes());
                                fileOutputStream.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((EpisodeAdapter)recyclerView.getAdapter()).setSize(EpisodePickerActivity.this.size);
                                    }
                                });
                            }
                          } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}