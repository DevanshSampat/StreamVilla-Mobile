package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

public class SeasonPickerActivity extends AppCompatActivity {
    private int count;
    private String imageURL;
    private String dbName;
    private String name;
    private String[] size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season_picker);
        if(new Theme(this).isInDarkMode()) findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        name = getIntent().getStringExtra("name");
        dbName = getIntent().getStringExtra("dbName");
        imageURL = getIntent().getStringExtra("image");
        ((TextView)findViewById(R.id.header)).setText(name);
        final RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    count = Integer.parseInt(snapshot.child(dbName + "seasons").getValue().toString());
                    size = new String[count];
                    for(int i=0;i<count;i++){
                        try {
                            File file = new File(getFilesDir(), dbName + "s" + (i + 1) + ".txt");
                            if (file.exists()) {
                                FileInputStream fileInputStream = openFileInput(dbName + "s" + (i + 1) + ".txt");
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
                recyclerView.setAdapter(new SeasonAdapter(SeasonPickerActivity.this,name,imageURL,dbName,count));
                ((SeasonAdapter)recyclerView.getAdapter()).setSize(size);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(size==null) size = new String[count];
                        double totalSize;
                        try {
                            for(int i=0;i<count;i++){
                                File file = new File(getFilesDir(),dbName+"s"+(i+1)+".txt");
                                if(file.exists()) {
                                    FileInputStream fileInputStream = openFileInput(dbName+"s"+(i+1)+".txt");
                                    BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
                                    SeasonPickerActivity.this.size[i] = br.readLine();
                                }
                                totalSize=0;
                                for(int j=1;j<=Integer.parseInt(snapshot.child(dbName+"s"+(i+1)).getValue().toString());j++) {
                                    try{
                                        snapshot.child(dbName+"s"+(i+1)+"e"+j).getValue().toString();
                                    } catch (Exception e) {
                                        Log.println(Log.ASSERT,"problem",j+"");
                                        e.printStackTrace();
                                        continue;
                                    }
                                    URLConnection connection = new URL(snapshot.child(dbName+"s"+(i+1)+"e"+j).getValue().toString()).openConnection();
                                    connection.connect();
                                    totalSize = totalSize+connection.getContentLengthLong();
                                    String string = "";
                                    String index = "MB";
                                    double size = totalSize;
                                    size = size / (1024 * 1024);
                                    if (size >= 1024) {
                                        size = size / 1024;
                                        index = "GB";
                                    }
                                    string = String.valueOf(size);
                                    try {
                                        string = string.substring(0, string.indexOf('.') + 3);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    SeasonPickerActivity.this.size[i] = string +" "+ index +" : "+j+" Episodes";
                                }
                                if(!file.exists()) file.createNewFile();
                                FileOutputStream fileOutputStream = openFileOutput(dbName+"s"+(i+1)+".txt",MODE_PRIVATE);
                                fileOutputStream.write((SeasonPickerActivity.this.size[i]+"\n").getBytes());
                                fileOutputStream.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((SeasonAdapter) recyclerView.getAdapter()).setSize(SeasonPickerActivity.this.size);
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