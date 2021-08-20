package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class WebSeriesDescriptionActivity extends AppCompatActivity {

    private String link;
    private int season = 1;
    private int episode = 1;
    private int position;
    private String developer_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_series_description);
        FirebaseDatabase.getInstance().getReference("developer_email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                developer_email = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        findViewById(R.id.image).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()).equals(developer_email)){
                    Intent intent = new Intent(WebSeriesDescriptionActivity.this,FirebaseNotificationActivity.class);
                    intent.putExtra("title",getIntent().getStringExtra("name"));
                    intent.putExtra("text","Now streaming on Entertainment");
                    intent.putExtra("image",getIntent().getStringExtra("image"));
                    intent.putExtra("data","search:"+getIntent().getStringExtra("name")+"\nopen:true");
                    startActivity(intent);
                }
                return false;
            }
        });
        findViewById(R.id.layout).startAnimation(AnimationUtils.loadAnimation(this,R.anim.appear_up));
        findViewById(R.id.top_layout).startAnimation(AnimationUtils.loadAnimation(this,R.anim.appear_down));
        if (getIntent().hasExtra("dark")||new Theme(this).isInDarkMode()) {
            findViewById(R.id.linear_layout).setBackgroundColor(Color.parseColor("#000000"));
            TextView textView = findViewById(R.id.title);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.description);
            textView.setTextColor(Color.parseColor("#CCCCCC"));
            textView = findViewById(R.id.detailed_description);
            textView.setTextColor(Color.parseColor("#CCCCCC"));
        }
        ((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("name"));
        ((TextView)findViewById(R.id.description)).setText(getIntent().getStringExtra("description"));
        FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("dbName")+"info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    ((TextView) findViewById(R.id.detailed_description)).setText(snapshot.getValue().toString());
                } catch (Exception ignored) {}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(getApplicationContext())).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try{
                    ArrayList<String> watchList = (ArrayList<String>) documentSnapshot.get("Watchlist");
                    if(watchList.contains(getIntent().getStringExtra("dbName"))) {
                        ((TextView) findViewById(R.id.text_watchlist)).setText("Added to List");
                        ((ImageView)findViewById(R.id.list_image)).setImageResource(R.drawable.ic_baseline_check_24);
                    }
                } catch (Exception e) {

                }

            }
        });
        findViewById(R.id.watchlist).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(((TextView)findViewById(R.id.text_watchlist)).getText().equals("Add to List")) {
                    new Sync().addToWatchList(getApplicationContext(), getIntent().getStringExtra("dbName"));
                    ((TextView)findViewById(R.id.text_watchlist)).setText("Added to List");
                    ((ImageView)findViewById(R.id.list_image)).setImageResource(R.drawable.ic_baseline_check_24);
                }
                else{
                    new Sync().removeFromWatchList(getApplicationContext(), getIntent().getStringExtra("dbName"));
                    ((TextView)findViewById(R.id.text_watchlist)).setText("Add to List");
                    ((ImageView)findViewById(R.id.list_image)).setImageResource(R.drawable.ic_baseline_add_to_list_24);
                }
            }
        });
        loadData();
    }
    private void loadData(){
        File file = new File(getFilesDir(),getIntent().getStringExtra("name").replace(' ','+')+".txt");
        if(file.exists()){
            try {
                String str = new BufferedReader(new InputStreamReader(openFileInput(file.getName()))).readLine();
                season = Integer.parseInt(str.substring(0,str.indexOf('\t')));
                str = str.substring(str.indexOf('\t')+1);
                episode = Integer.parseInt(str.substring(0,str.indexOf('\t')));
                str = str.substring(str.indexOf('\t')+1);
                position = Integer.parseInt(str);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else Picasso.with(this).load(getIntent().getStringExtra("image")).into((ImageView) findViewById(R.id.image));
        FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("dbName")+"s"+season+"e"+episode).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    link = snapshot.getValue().toString();
                    if(!(season==1&&episode==1&&position==0)) {
                        ((TextView) findViewById(R.id.play_text)).setText("S" + season + "E" + episode);
                        File finalFile;
                        if ((finalFile = new File(getExternalFilesDir(null), ".screenshot_of_" + getIntent().getStringExtra("name").replace(':', '_').replace(' ', '_')
                                + "___S" + season + "E" + episode + ".jpeg")).exists())
                            ((ImageView) findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeFile(finalFile.getPath()));
                        else
                            Picasso.with(WebSeriesDescriptionActivity.this).load(getIntent().getStringExtra("image")).into((ImageView) findViewById(R.id.image));
                    }
                    else
                        Picasso.with(WebSeriesDescriptionActivity.this).load(getIntent().getStringExtra("image")).into((ImageView) findViewById(R.id.image));
                    findViewById(R.id.watch).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(WebSeriesDescriptionActivity.this,VideoPlayerActivity.class);
                            String name = getIntent().getStringExtra("name");
                            String dbName=  getIntent().getStringExtra("dbName");
                            intent.putExtra("name",name+" : S"+season+"E"+episode);
                            intent.putExtra("raw_name",name+" : S"+season+"E");
                            intent.putExtra("online",true);
                            intent.putExtra("dbName",dbName+"s"+ season);
                            intent.putExtra("episode_number", episode+"");
                            intent.putExtra("position", position+"");
                            intent.putExtra("image",getIntent().getStringExtra("image"));
                            intent.putExtra("description",getIntent().getStringExtra("description"));
                            intent.putExtra("link",link);
                            startActivity(intent);
                            printHistory(name);
                            new Thread(()->{
                                new Sync().uploadHistory(WebSeriesDescriptionActivity.this);
                                new Sync().addToQuickPicks(WebSeriesDescriptionActivity.this, dbName);
                            }).start();
                        }
                    });
                    findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            findViewById(R.id.watch).callOnClick();
                        }
                    });
                } catch (Exception ignored) {}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("dbName")+"seasons").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    int numberOfSeasons = Integer.parseInt(snapshot.getValue().toString());
                    ((TextView)findViewById(R.id.text_seasons)).setText(numberOfSeasons+" season"+(numberOfSeasons==1?"":"s"));
                    findViewById(R.id.seasons).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(WebSeriesDescriptionActivity.this,SeasonPickerActivity.class);
                            intent.putExtra("dbName",getIntent().getStringExtra("dbName"));
                            intent.putExtra("name",getIntent().getStringExtra("name"));
                            intent.putExtra("description",getIntent().getStringExtra("description"));
                            intent.putExtra("image",getIntent().getStringExtra("image"));
                            startActivity(intent);
                            new Thread(()->{
                                new Sync().uploadHistory(WebSeriesDescriptionActivity.this);
                                new Sync().addToQuickPicks(WebSeriesDescriptionActivity.this, getIntent().getStringExtra("dbName"));
                            }).start();
                        }
                    });
                } catch (Exception ignored) {}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        },1000);
    }
    public void printHistory(String str) {
        try {
            printDate();
            FileOutputStream fos = openFileOutput("History.txt", MODE_APPEND);
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.HOUR_OF_DAY) < 10) fos.write("0".getBytes());
            fos.write((calendar.get(Calendar.HOUR_OF_DAY) + ":").getBytes());
            if (calendar.get(Calendar.MINUTE) < 10) fos.write("0".getBytes());
            fos.write((calendar.get(Calendar.MINUTE) + "\t\t" + str + "\n").getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printDate() {
        File temp_history = new File(this.getFilesDir(), "TempHistory.txt");
        try {
            File file = new File(this.getFilesDir(), "Date.txt");
            if (!file.exists()) {
                file.createNewFile();
                try {
                    FileOutputStream fileOutputStream = openFileOutput("Date.txt", MODE_PRIVATE);
                    fileOutputStream.write("00000000".getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream fis = null;
            fis = this.openFileInput("Date.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            Calendar calendar = Calendar.getInstance();
            if (!sb.toString().substring(0, 4).equals(String.valueOf(calendar.get(Calendar.YEAR)))) {
                try {
                    FileOutputStream fileOutputStream = openFileOutput("History.txt", MODE_PRIVATE);
                    fileOutputStream.write(("History of " + String.valueOf(calendar.get(Calendar.YEAR))).getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!sb.toString().equals(calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH))) {
                try {
                    FileOutputStream fos = openFileOutput("Date.txt", MODE_PRIVATE);
                    fos.write((calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH)).getBytes());
                    fos.close();
                    File file_history = new File(this.getFilesDir(), "History.txt");
                    fos = openFileOutput("History.txt", MODE_APPEND);
                    fos.write(("\n" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + calendar.get(Calendar.MONTH)) + "\n" + "\n").getBytes());
                    fos = openFileOutput("TempHistory.txt", MODE_APPEND);
                    fos.write(("\n" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + calendar.get(Calendar.MONTH)) + "\n" + "\n").getBytes());
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("Backup.txt", MODE_PRIVATE);
                        fileOutputStream.write("true".getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.top_layout).startAnimation(AnimationUtils.loadAnimation(this,R.anim.disappear_up));
        findViewById(R.id.layout).startAnimation(AnimationUtils.loadAnimation(this,R.anim.disappear_down));
        findViewById(R.id.top_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout).setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WebSeriesDescriptionActivity.super.onBackPressed();
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        },230);
    }
}