package com.example.news;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class NextVideoActivity extends AppCompatActivity {

    private boolean isCancelled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_next_video);
        Picasso.with(this).load(getIntent().getStringExtra("image")).into((ImageView)findViewById(R.id.image));
        ((TextView)findViewById(R.id.text)).setText(getIntent().getStringExtra("name"));
        countdown(5);
    }

    @SuppressLint("SetTextI18n")
    private void countdown(final int i) {
        ((TextView)findViewById(R.id.up_next)).setText("Up Next in "+i);
        if(i==0&&!isCancelled) openVideo(new View(this));
        else {
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
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        }
    }

    public void cancel(View view) {
        isCancelled = true;
        finish();
    }
}