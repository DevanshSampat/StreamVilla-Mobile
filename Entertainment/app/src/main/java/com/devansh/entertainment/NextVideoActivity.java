package com.devansh.entertainment;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NextVideoActivity extends AppCompatActivity {

    private boolean isCancelled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_next_video);
        Picasso.get().load(getIntent().getStringExtra("image")).into((ImageView)findViewById(R.id.image));
        ((TextView)findViewById(R.id.text)).setText(getIntent().getStringExtra("name"));
        countdown(5);
    }

    @SuppressLint("SetTextI18n")
    private void countdown(final int i) {
        ((TextView)findViewById(R.id.up_next)).setText("Up Next in "+i);
        if(i==0&&!isCancelled) openVideo(new View(this));
        else if(i>0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    countdown(i-1);
                }
            },1000);
        }
    }

    public void openVideo(View view) {
        Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
        intent.putExtras(getIntent().getExtras());
        if(!isCancelled) {
            isCancelled = true;
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        }
    }

    public void cancel(View view) {
        isCancelled = true;
        String season = getIntent().getStringExtra("dbName").substring(getIntent().getStringExtra("dbName").lastIndexOf('s')+1);
        String episode = getIntent().getStringExtra("episode_number");
        String fileName = getIntent().getStringExtra("raw_name").substring(0,getIntent().getStringExtra("raw_name").lastIndexOf(':')-1);
        fileName = fileName.replace(' ','+')+".txt";
        if(!new File(getFilesDir(),fileName).exists()){
            try {
                new File(getFilesDir(),fileName).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = openFileOutput(fileName,MODE_PRIVATE);
            fos.write((season+"\t"+episode+"\t"+"0\n").getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Sync().uploadTimeFiles(getApplicationContext());
            }
        }).start();
    }
}