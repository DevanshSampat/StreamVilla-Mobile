package com.devansh.entertainment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.VideoView;

public class TutorialActivity extends AppCompatActivity {

    private String[] title,text,video;
    private VideoView videoView;
    TextView title_text,subtitle;
    private int position=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    }
}