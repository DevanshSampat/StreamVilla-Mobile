package com.devansh.entertainment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_RESULT_CODE = 999;
    private int trend_hour;
    private ContentData[] trendData,recentlyAddedContent;
    private boolean app_opened=true;
    private boolean first=true;
    private RecyclerView recyclerView;
    private RecyclerView trendView;
    private RecyclerView recentlyAdded;
    private boolean show = false;
    private boolean clear = false;
    private String online_history;
    private String local_history;
    private String beta_name;
    private String beta_log;
    private String beta_link;
    private String userType;
    private String quick_picks;
    private FirebaseFirestore firestore;
    private boolean dark = false;
    private FirebaseUser user;
    private String name="";
    private DatabaseReference reference;
    private int version = 0;
    private String version_name;
    private String changelog;
    private String update_link;
    private Button btn;
    private ImageView imageView;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private TextView tv;
    private int RC_SIGN_IN =0;
    private FirebaseAuth auth;
    private ContentData recentData[];
    private GoogleSignInClient mGoogleSignInClient;
    private String[] latestContent;
    private String notification_token;
    private StringBuilder watch_time;
    private String downloadedMovies;
    private String restoreMovies;
    private String movie_watch_session_times="";
    private Notification notification;
    private boolean personal = true;
    private boolean restarted=false;
    private boolean clicked = false;
    private String last_sign_in="0000/00/00 at 00:00";
    private String next_notification;
    private Object watchList;
    private BroadcastReceiver toggleCastreceiver;
    private DatabaseReference contentReference;
    private boolean isLoaded = false;
    private long timeOffset;
    private ArrayList<String> trendingArrayList;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        printDate();
        getTimeOffset();
        findViewById(R.id.watchlist).setVisibility(View.GONE);
        trendingArrayList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("weekly_trending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trendingArrayList = (ArrayList<String>) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession()==null) findViewById(R.id.castCard).setVisibility(View.GONE);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch toggle = findViewById(R.id.dark_toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) setDarkTheme();
                else setLightTheme();
            }
        });
        toggleCastreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    if(intent.getStringExtra("mode").equals("end")) findViewById(R.id.castCard).setVisibility(View.GONE);
                    else findViewById(R.id.castCard).setVisibility(View.VISIBLE);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        };
        registerReceiver(toggleCastreceiver,new IntentFilter("CAST"));
        downloadedMovies=getDownloadedMovies();
        findViewById(R.id.search).setVisibility(View.INVISIBLE);
        watch_time=new StringBuilder();
        deleteExistingApk();
        setAppropriateTheme();
        findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setVisibility(View.GONE);
        findViewById(R.id.quick).setVisibility(View.GONE);
        trendView = (RecyclerView) findViewById(R.id.recycler_trend);
        trendView.setHasFixedSize(true);
        trendView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        trendView.setVisibility(View.GONE);
        findViewById(R.id.trend).setVisibility(View.GONE);
        recentlyAdded = (RecyclerView) findViewById(R.id.recycler_recently_added);
        recentlyAdded.setHasFixedSize(true);
        recentlyAdded.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recentlyAdded.setVisibility(View.GONE);
        findViewById(R.id.recently_added).setVisibility(View.GONE);
        findViewById(R.id.settings).setVisibility(View.INVISIBLE);
        userType = new String();
        userType = "";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_sign_in_auth))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null)
        {
            Button btn = (Button) findViewById(R.id.sign_out_button);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    signIn();
                }
            }).start();
            btn.setVisibility(View.VISIBLE);
            getInformation();
        }
        else
        {
            app_opened=false;
            if(getIntent().hasExtra("search")) signIn();
            else checkForUpdates();
        }
        FirebaseAnalytics.getInstance(this);
        findViewById(R.id.show_quick_and_trending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
                clicked = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkUserType();
                    }
                }).start();
            }
        });
        findViewById(R.id.trend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ListDisplayActivity.class);
                intent.putExtra("name","Trending");
                startActivity(intent);
            }
        });
        findViewById(R.id.recommended).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ListDisplayActivity.class);
                intent.putExtra("name","You may like");
                startActivity(intent);
            }
        });
        findViewById(R.id.profile_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(user==null) signIn();
                 else if(findViewById(R.id.settings).getVisibility()==View.VISIBLE)
                 {
                     showSignOutDialog();
                 }
            }
        });
        if(!checkInternetConnection())
        {
            Intent i = new Intent(this,WebActivity.class);
            i.putExtra("link","null");
            startActivity(i);
        }
        loadImages();
        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ShareActivity.class);
                if(dark) intent.putExtra("dark",true);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in,R.anim.fade_out);
            }
        });
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("general",
                    "General", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Latest content and news");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            channel = new NotificationChannel("app_update",
                    "App Update", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notify about app update, whenever available");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            channel = new NotificationChannel("personal",
                    "Personalized Notifications", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Based on your activity");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            channel = new NotificationChannel("service",
                    "Background Notification Service", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Developer exclusive feature");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("general");
        FirebaseMessaging.getInstance().subscribeToTopic("personal");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        notification_token=task.getResult();
                    }
                });
    }

    private void getTimeOffset() {
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timeOffset = Long.parseLong(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showSignOutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.sign_out_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.y = 100;
        layoutParams.verticalMargin=10;
        layoutParams.horizontalMargin=10;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(true);
        if(dark)
        {
            ((CardView)dialog.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#363636"));
            ((TextView)dialog.findViewById(R.id.name)).setTextColor(Color.WHITE);
            ((TextView)dialog.findViewById(R.id.email)).setTextColor(Color.WHITE);
            ((TextView)dialog.findViewById(R.id.info)).setTextColor(Color.WHITE);
        }
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        ((TextView)dialog.findViewById(R.id.name)).setText(account.getDisplayName());
        ((TextView)dialog.findViewById(R.id.email)).setText(account.getEmail());
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null) return;
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String temp = snapshot.child("Last Sign in").getValue().toString();
                String dateAndTime;
                String[] month = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
                dateAndTime = temp.substring(8,10);
                temp = temp.substring(temp.indexOf('/')+1);
                dateAndTime = dateAndTime +" "+month[Integer.parseInt(temp.substring(0,temp.indexOf('/')))-1]+" "+
                        snapshot.child("Last Sign in").getValue().toString().substring(0,4);
                String str = "Signed in on " + dateAndTime + snapshot.child("Last Sign in").getValue().toString().substring(10)+
                        " on "+snapshot.child("Last device used").getValue().toString();
                ((TextView)dialog.findViewById(R.id.info)).setText(str);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dialog.findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut(findViewById(R.id.sign_out_button));
            }
        });
        dialog.findViewById(R.id.all_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(!show) showSettings(new View(getApplicationContext()));
            }
        });
        if(account.getPhotoUrl()!=null)
            Picasso.get().load(account.getPhotoUrl()).into((ImageView)dialog.findViewById(R.id.profile_image));
        dialog.show();
        dialog.getWindow().setDimAmount(0.90f);
        dialog.findViewById(R.id.profile_image).startAnimation(AnimationUtils.loadAnimation(this,R.anim.rotate_come));
        dialog.findViewById(R.id.layout).startAnimation(AnimationUtils.loadAnimation(this,R.anim.appear_up));

    }

    private void setAppropriateTheme()
    {
        try {
            FileInputStream fis = null;
            fis = this.openFileInput("Theme.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            sb.append(br.readLine());
            while ((str=br.readLine())!=null)
            {
                sb.append(str);
            }
            TextView textView = (TextView) findViewById(R.id.app_info);
            Switch toggle = (Switch) findViewById(R.id.dark_toggle);
            toggle.setChecked(sb.toString().equals("true"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setDarkTheme()
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        scrollView.setBackgroundColor(Color.parseColor("#000000"));
        CardView cardView = (CardView) findViewById(R.id.profile_info);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        TextView textView = (TextView) findViewById(R.id.profile_name);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.profile_email);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        cardView = (CardView) findViewById(R.id.news);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.comedy);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.sports);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.movies);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.classics);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.kids);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.search);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.share);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.settings);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        textView = (TextView) findViewById(R.id.news_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.comedy_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.sports_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.movies_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.classics_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.kids_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.contact);
        textView.setTextColor(Color.WHITE);
        Button button = (Button) findViewById(R.id.update);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.add_content);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.user_details);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.download_apk);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.watchlist);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.history);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.beta);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.my_downloads);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.play_local_video);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.app_info);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.broadcast);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        dark = true;
        try {

            File file = new File(this.getFilesDir(), "Theme.txt");
            FileOutputStream fos = openFileOutput("Theme.txt",MODE_PRIVATE);
            fos.write(("true").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setLightTheme()
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        scrollView.setBackgroundColor(Color.WHITE);
        CardView cardView = (CardView) findViewById(R.id.profile_info);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        TextView textView = (TextView) findViewById(R.id.profile_name);
        textView.setTextColor(Color.parseColor("#333333"));
        textView = (TextView) findViewById(R.id.profile_email);
        textView.setTextColor(Color.parseColor("#333333"));
        cardView = (CardView) findViewById(R.id.news);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.comedy);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.sports);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.movies);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.classics);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.kids);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.search);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.share);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.settings);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        textView = (TextView) findViewById(R.id.news_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.comedy_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.sports_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.movies_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.classics_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.kids_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.contact);
        textView.setTextColor(Color.BLACK);
        Button button = (Button) findViewById(R.id.update);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.add_content);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.user_details);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.download_apk);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.watchlist);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.history);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.beta);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.my_downloads);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.play_local_video);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.app_info);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.broadcast);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        dark = false;
        try {
            FileOutputStream fos = openFileOutput("Theme.txt",MODE_PRIVATE);
            fos.write("false".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showHistory(View v)
    {
        Intent intent = new Intent(this,HistoryActivity.class);
        if(dark) intent.putExtra("dark",true);
        if(user!=null) intent.putExtra("signed_in",true);
        startActivity(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void printDate()
    {
        File temp_history = new File(this.getFilesDir(),"TempHistory.txt");
        try {
            File file = new File(this.getFilesDir(),"Date.txt");
            if(!file.exists())
            {
                file.createNewFile();
                try {
                    FileOutputStream fileOutputStream = openFileOutput("Date.txt",MODE_PRIVATE);
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
            while ((str=br.readLine())!=null)
            {
                sb.append(str);
            }
            Calendar calendar = Calendar.getInstance();
            if(!sb.toString().substring(0,4).equals(String.valueOf(calendar.get(Calendar.YEAR))))
            {
                try {
                    FileOutputStream fileOutputStream = openFileOutput("History.txt",MODE_PRIVATE);
                    fileOutputStream.write(("History of "+String.valueOf(calendar.get(Calendar.YEAR))).getBytes());
                    fileOutputStream.close();
                    if(!sb.toString().substring(0,4).equals("0000")) clear=true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!sb.toString().equals(calendar.get(Calendar.YEAR)+""+(calendar.get(Calendar.MONTH)+1)+""+calendar.get(Calendar.DAY_OF_MONTH)))
            {
                try {
                    FileOutputStream fos = openFileOutput("Date.txt", MODE_PRIVATE);
                    fos.write((calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH)).getBytes());
                    fos.close();
                    File file_history = new File(this.getFilesDir(),"History.txt");
                    fos = openFileOutput("History.txt",MODE_APPEND);
                    fos.write(("\n"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+"\n"+"\n").getBytes());
                    fos = openFileOutput("TempHistory.txt",MODE_APPEND);
                    fos.write(("\n"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+"\n"+"\n").getBytes());
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("Backup.txt",MODE_PRIVATE);
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
            TextView textView = (TextView) findViewById(R.id.app_info);
            textView.setText("Not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void signOut(final View v)
    {
        new File(getFilesDir(),"isSignedIn.txt").delete();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                TextView tv;
                tv = (TextView) findViewById(R.id.profile_name);
                tv.setText("Tap to sign-in");
                tv = (TextView) findViewById(R.id.profile_email);
                tv.setText("to enjoy latest content");
                Button btn = (Button) findViewById(R.id.sign_out_button);
                btn.setVisibility(View.GONE);
                btn = (Button) findViewById(R.id.sign_out);
                btn.setVisibility(View.GONE);
                btn = (Button) findViewById(R.id.add_content);
                btn.setVisibility(View.GONE);
                findViewById(R.id.broadcast).setVisibility(View.GONE);
                btn = (Button) findViewById(R.id.beta);
                btn.setVisibility(View.GONE);
                imageView = (ImageView) findViewById(R.id.profile_photo);
                storage = FirebaseStorage.getInstance();
                imageView.setVisibility(View.GONE);
                imageView = (ImageView) findViewById(R.id.blank_profile);
                imageView.setVisibility(View.VISIBLE);
                revokeAccess();
                user=null;
                tv = (TextView) findViewById(R.id.verify);
                tv.setVisibility(View.GONE);
                userType="";
                if(v == (Button) findViewById(R.id.sign_out))show=false;
                checkForUpdates();
                recyclerView.setVisibility(View.GONE);
                findViewById(R.id.quick).setVisibility(View.GONE);
                trendView.setVisibility(View.GONE);
                findViewById(R.id.trend).setVisibility(View.GONE);
                findViewById(R.id.recently_added).setVisibility(View.GONE);
                recentlyAdded.setVisibility(View.GONE);
                findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
                findViewById(R.id.search).setVisibility(View.INVISIBLE);
                findViewById(R.id.watchlist).setVisibility(View.GONE);
                popup("Signed out successfully");
                try {
                    FileOutputStream fos = openFileOutput("Date.txt",MODE_PRIVATE);
                    fos.write("00000000".getBytes());
                    fos.close();
                    FileInputStream fis = openFileInput("DownloadedVideos.txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String string;
                    while((string=br.readLine())!=null)
                    {
                        File file = new File(getApplicationContext().getFilesDir(),string.replace(' ','+')+".txt");
                        if(file.exists()) file.delete();
                    }
                    Intent intent = new Intent(getApplicationContext(),SplashActivity.class);
                    finish();
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void popup(String str)
    {
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }
    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        first=true;
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else if(requestCode==PICKFILE_RESULT_CODE)
        {
            if(resultCode==RESULT_OK)
            {
                Uri uri = data.getData();
                String path=uri.getPath();
                int i=path.length()-1;
                while(path.charAt(i)!='/') i--;
                path=path.substring(i+1);
                Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                intent.putExtra("uri",String.valueOf(uri));
                intent.putExtra("name",path);
                startActivity(intent);
            }
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            getInformation();
            firebaseAuthWithGoogle(acct.getIdToken());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google", "signInResult:failed code=" + e.getStatusCode());
        }
    }
    public void verify(View v) {}
    private void getInformation()
    {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        TextView name = (TextView) findViewById(R.id.profile_name);
        TextView email = (TextView) findViewById(R.id.profile_email);
        Uri url = account.getPhotoUrl();
        name.setText("Google Account");
        email.setText("getting information");
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        if(url!=null) {
            ImageView imageView = (ImageView) findViewById(R.id.blank_profile);
            imageView.setVisibility(View.GONE);
            imageView = (ImageView) findViewById(R.id.profile_photo);
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(animation);
            Picasso.get().load(String.valueOf(url)).into(imageView);
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user=auth.getCurrentUser();
                            clicked=true;
                            first=true;
                            Log.println(Log.ASSERT,"status","signed in");
                            validate();
                            // Sign in success, update UI with the signed-in user's information
                            if (getIntent().hasExtra("search"))
                            {
                                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                                intent.putExtra("search", getIntent().getStringExtra("search"));
                                if (getIntent().hasExtra("open")) intent.putExtra("open", true);
                                intent.putExtra("signed_in", true);
                                if (dark) intent.putExtra("dark", true);
                                startActivity(intent);
                            }
                        }
                    }
                });
    }

    private void loadRecommendedContent(DataSnapshot contentSnapshot) {
        try{
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference("recommendations").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> recommendedList = (ArrayList<String>) snapshot.getValue();
                    if(recommendedList==null) return;
                    ContentData[] contentData = new ContentData[recommendedList.size()];
                    for(int i=0;i<recommendedList.size();i++){
                        contentData[i] = Utility.getContentData(MainActivity.this,contentSnapshot,recommendedList.get(i));
                    }
                    RecyclerView recyclerView = findViewById(R.id.recycler_recommended);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false));
                    recyclerView.setAdapter(new SmallContentAdapter(contentData,MainActivity.this,false));
                    findViewById(R.id.recommended).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validate()
    {
        if(!new File(getFilesDir(),"isSignedIn.txt").exists()) {
            try {
                new File(getFilesDir(),"isSignedIn.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null) {
            findViewById(R.id.search).setVisibility(View.VISIBLE);
            findViewById(R.id.settings).setVisibility(View.INVISIBLE);
            findViewById(R.id.watchlist).setVisibility(View.VISIBLE);
            UserName.setUsername(acct.getEmail());
            UserName.setUsername(getApplicationContext(),acct.getEmail());
            auth = FirebaseAuth.getInstance();
            name = acct.getDisplayName();
            user=auth.getCurrentUser();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (user != null) checkUserType();
                }
            }).start();
            if (acct.getEmail().equals("devansh.sampat@gmail.com")) {
                Button btn = (Button) findViewById(R.id.add_content);
                btn.setVisibility(View.VISIBLE);
                findViewById(R.id.user_details).setVisibility(View.VISIBLE);
                findViewById(R.id.broadcast).setVisibility(View.VISIBLE);
                if(RandomNotificationService.getInstance()==null) startService(new Intent(getApplicationContext(),RandomNotificationService.class));
            }
            Button btn = (Button) findViewById(R.id.sign_out_button);
            btn.setVisibility(View.VISIBLE);
            btn = (Button) findViewById(R.id.beta);
            btn.setVisibility(View.VISIBLE);
            RecyclerView demoView;
            findViewById(R.id.recently_added).setVisibility(View.VISIBLE);
            findViewById(R.id.trend).setVisibility(View.VISIBLE);
            findViewById(R.id.quick).setVisibility(View.VISIBLE);
            demoView = findViewById(R.id.recycler);
            demoView.setVisibility(View.VISIBLE);
            demoView.setAdapter(new SmallContentAdapter(true,getApplicationContext(),dark));
            demoView = findViewById(R.id.recycler_trend);
            demoView.setVisibility(View.VISIBLE);
            demoView.setAdapter(new SmallContentAdapter(true,getApplicationContext(),dark));
            demoView = findViewById(R.id.recycler_recently_added);
            demoView.setVisibility(View.VISIBLE);
            demoView.setAdapter(new SmallContentAdapter(true,getApplicationContext(),dark));
        }
    }
    private void checkUserType()
    {
        String path = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if(snapshot.getValue()==null){
                        FirebaseFirestore.getInstance().collection("Users")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.getData()==null) return;
                                FirebaseDatabase.getInstance().getReference("users").child(path).setValue(documentSnapshot.getData());
                                Toast.makeText(MainActivity.this, "Syncing data with cloud", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                if(last_sign_in.equals("0000/00/00 at 00:00")) last_sign_in=snapshot.child("Last Sign in").getValue().toString();
                    restoreMovies="";
                    restoreMovies=snapshot.child("Downloads").getValue().toString();
                    quick_picks=snapshot.child("Quick Picks").getValue().toString();
                    try{
                        watchList = snapshot.child("Watchlist").getValue();
                    } catch (Exception e) {
                        watchList = null;
                    }
                    userType = snapshot.child("Type").getValue().toString();
                    movie_watch_session_times = snapshot.child("Watch Files").getValue().toString();
                    try {
                        FileOutputStream fos = new FileOutputStream(new File(getFilesDir(),"UserWatchTimings.txt"));
                        fos.write(movie_watch_session_times.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                    Button btn = (Button) findViewById(R.id.beta);
                    if(userType.equals("beta"))
                    {
                        btn.setText("Revert to stable");
                    }
                    else btn.setText("Become a tester");
                    online_history=snapshot.child("Watch History").getValue().toString();
                    Toast.makeText(MainActivity.this,online_history.trim(),Toast.LENGTH_SHORT);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FileInputStream fis = null;
                                fis = getApplicationContext().openFileInput("History.txt");
                                BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                                StringBuilder sb = new StringBuilder();
                                String str;
                                while ((str = br.readLine()) != null) {
                                    sb.append(str).append("\n");
                                }
                                local_history = sb.toString();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!local_history.substring(0,7).equals("History"))
                            {
                                Calendar calendar = Calendar.getInstance();
                                local_history= "History of "+String.valueOf(calendar.get(Calendar.YEAR)) +"\n"+local_history;
                            }
                        }
                    }, 500);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadUserDetails();
                        }
                    }).start();
                } catch (Exception e) {
                    if(userType==null||!userType.equals("beta"))userType = "stable";
                    online_history= new String();
                    try{
                        FileInputStream fis = null;
                        fis = getApplicationContext().openFileInput("History.txt");
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        String str;
                        while ((str=br.readLine())!=null)
                        {
                            sb.append(str).append("\n");
                        }
                        local_history = sb.toString();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadUserDetails();
                            }
                        }, 1000);
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void uploadUserDetails()
    {
        makeTimeFiles();
        sendBroadcast(new Intent("watch_files_changed"));
        String history="";
        try {
            if(online_history.trim().length()==0) history = local_history;
            else if(online_history.trim().length()<=local_history.trim().length()) history=local_history;
            else
            {
                if(clear&&!online_history.substring(0,"History of 0000".length()).equals("History of "+Calendar.getInstance().get(Calendar.YEAR)))
                {
                    history="";
                    popup("Watch history reset");
                    clear=false;
                }
                else
                {
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("History.txt", MODE_PRIVATE);
                        fileOutputStream.write((online_history).getBytes());
                        fileOutputStream.close();
                        history = online_history;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.println(Log.ASSERT,"error",e.toString());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkUserType();
                }
            }).start();
            return;
        }
        final Map<String,Object> userInfo = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null) return;
        userInfo.put("Name",user.getDisplayName());
        userInfo.put("Email",user.getEmail());
        userInfo.put("Image",user.getPhotoUrl().toString());
        userInfo.put("Type",userType);
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.get(Calendar.YEAR)+"/");
        if(calendar.get(Calendar.MONTH)+1<10) sb.append("0");
        sb.append((calendar.get(Calendar.MONTH)+1)+"/");
        if(calendar.get(Calendar.DAY_OF_MONTH)<10) sb.append("0");
        sb.append(calendar.get(Calendar.DAY_OF_MONTH)+" at ");
        if(calendar.get(Calendar.HOUR_OF_DAY)<10) sb.append("0");
        sb.append(calendar.get(Calendar.HOUR_OF_DAY)+":");
        if(calendar.get(Calendar.MINUTE)<10) sb.append("0");
        sb.append(calendar.get(Calendar.MINUTE));
        if(quick_picks==null) quick_picks="no data";
        //Log.println(Log.ASSERT,"Data","Uploading");
        userInfo.put("Last Sign in",sb.toString());
        userInfo.put("Last device used",Build.BRAND+" "+Build.MODEL+"("+Build.DEVICE+")");
        userInfo.put("Android version",Build.VERSION.RELEASE);
        userInfo.put("App version",BuildConfig.VERSION_NAME);
        userInfo.put("Watch History",history);
        userInfo.put("Notification Token",notification_token);
        userInfo.put("Watch Files",movie_watch_session_times);
        userInfo.put("Downloads",downloadedMovies);
        userInfo.put("Quick Picks",quick_picks);
        userInfo.put("Watchlist",watchList);
        UserName.setQuickPicks(quick_picks);
        UserName.setWatchlist(watchList);
        //findViewById(R.id.show_quick_and_trending).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getRecents();
            }
        }).start();
        if(first)
        {
            if(clicked)
            {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userInfo);
                clicked=false;
            }
            if(app_opened)
            {
                checkForUpdates();
                //findViewById(R.id.show_quick_and_trending).callOnClick();
            }
            else if(findViewById(R.id.options).getVisibility()!=View.VISIBLE) findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
            app_opened=false;
        }
        else
        {
            findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
            findViewById(R.id.load_options).setVisibility(View.GONE);
        }
        if(userType.equals("beta"))
        {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("stable_update");
            FirebaseMessaging.getInstance().subscribeToTopic("beta_update");
        }
        else
        {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("beta_update");
            FirebaseMessaging.getInstance().subscribeToTopic("stable_update");
        }
    }
    public void addContent(View v)
    {
        if(!Utility.checkForBiometrics(this)) return;
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent = new Intent(MainActivity.this,AddContentActivity.class);
                if(dark) intent.putExtra("dark",true);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Sensitive feature")
                .setSubtitle("Verify your identity")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
    public void downloadUpdates(final View v)
    {
        reference = FirebaseDatabase.getInstance().getReference("app_versions");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(userType.equals("beta")) version=Integer.parseInt(snapshot.child("beta_code").getValue().toString());
                else version=Integer.parseInt(snapshot.child("version_code").getValue().toString());
                version_name=snapshot.child("version_name").getValue().toString();
                changelog=snapshot.child("changelog").getValue().toString();
                update_link=snapshot.child("update_link").getValue().toString();
                beta_log=snapshot.child("beta_log").getValue().toString();
                beta_name=snapshot.child("beta_name").getValue().toString();
                beta_link=snapshot.child("beta_link").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(BuildConfig.VERSION_CODE<version) {
            findViewById(R.id.update_notification).setVisibility(View.VISIBLE);
            Intent i = new Intent(this, UpdaterActivity.class);
            if(dark) i.putExtra("dark",true);
            if(userType.equals("beta"))
            {
                i.putExtra("beta",true);
                i.putExtra("version",beta_name);
                i.putExtra("changelog",beta_log);
                i.putExtra("update_link",beta_link);
            }
            else
            {
                i.putExtra("version", version_name);
                i.putExtra("changelog", changelog);
                i.putExtra("update_link",update_link);
            }
            startActivity(i);
            overridePendingTransition(R.anim.right_start,R.anim.right_end);
            finish();
        }
        else
        {
            Toast.makeText(this,getString(R.string.app_name)+" is up-to-date",Toast.LENGTH_SHORT).show();
            findViewById(R.id.update_notification).setVisibility(View.GONE);
        }
    }
    public void checkForUpdates()
    {
        reference = FirebaseDatabase.getInstance().getReference("app_versions");
        reference.child("trending").removeValue();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (account != null && userType.equals("")) checkForUpdates();
                    version = Integer.parseInt(snapshot.child("version_code").getValue().toString());
                    if (userType.equals("beta"))
                        version = Integer.parseInt(snapshot.child("beta_code").getValue().toString());
                    version_name = snapshot.child("version_name").getValue().toString();
                    changelog = snapshot.child("changelog").getValue().toString();
                    update_link = snapshot.child("update_link").getValue().toString();
                    beta_log = snapshot.child("beta_log").getValue().toString();
                    beta_name = snapshot.child("beta_name").getValue().toString();
                    beta_link = snapshot.child("beta_link").getValue().toString();
                    btn = (Button) findViewById(R.id.update);
                    btn.setVisibility(View.VISIBLE);
                    if (BuildConfig.VERSION_CODE < version) {
                        btn.setTextColor(Color.parseColor("#FF8313"));
                        btn.setBackgroundColor(Color.parseColor("#0C61C8"));
                        if (userType.equals("beta"))
                            btn.setText("Update available\n(" + beta_name + ")");
                        else btn.setText("Update available\n(" + version_name + ")");
                        downloadUpdates(btn);
                    } else {
                        findViewById(R.id.update_notification).setVisibility(View.GONE);
                        btn.setText("Check For Updates");
                        setAppropriateTheme();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void loadImages()
    {
        reference = FirebaseDatabase.getInstance().getReference("images");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ImageView imageView = (ImageView) findViewById(R.id.sports_image);
                Picasso.get().load(snapshot.child("sports").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.news_image);
                Picasso.get().load(snapshot.child("news").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.comedy_image);
                Picasso.get().load(snapshot.child("comedy").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.movies_image);
                Picasso.get().load(snapshot.child("movies").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.classics_image);
                Picasso.get().load(snapshot.child("classics").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.kids_image);
                Picasso.get().load(snapshot.child("kids").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.settings_image);
                Picasso.get().load(snapshot.child("settings").getValue().toString()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void openActivity(View v)
    {
        if(user==null)
        {
            signIn();
            return;
        }
        TextView textView = (TextView) findViewById(R.id.verify);
        textView.setVisibility(View.GONE);
        Intent i = new Intent(getApplicationContext(),ContentDisplayActivity.class);
        switch (v.getId())
        {
            case R.id.news : i.putExtra("content_type","news");
                break;
            case R.id.comedy : i.putExtra("content_type","comedy");
                break;
            case R.id.sports : i.putExtra("content_type","sport");
                break;
            case R.id.movies : i.putExtra("content_type","movie");
                break;
            case R.id.classics : i.putExtra("content_type","classic");
                break;
            case R.id.kids : i.putExtra("content_type","kids");
                break;
        }
        if(dark) i.putExtra("dark",true);
        startActivity(i);
        overridePendingTransition(R.anim.up_start,R.anim.up_end);
    }
    public void showAppInfo(View v)
    {
        Intent i = new Intent(this,AppInfoActivity.class);
        if(dark) i.putExtra("dark",true);
        startActivity(i);
        overridePendingTransition(R.anim.zoom_in_bottom,R.anim.fade_out);
    }
    public boolean checkInternetConnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetwork()==null)
        {
            return false;
        }
        return true;
    }
    public void toggleBeta(View v)
    {
        Button btn = (Button) findViewById(R.id.beta);
        if(userType.equals("beta"))
        {
            userType = "stable";
            btn.setText("Become a tester");
            popup("Reverted to stable user");
            checkForUpdates();
        }
        else
        {
            userType = "beta";
            btn.setText("Revert to stable");
            popup("You are a beta user now\nStay signed in for beta updates");
            checkForUpdates();
        }
        FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid()).child("Type").setValue(userType);
    }

    public void searchContent(View view)
    {
        Intent intent = new Intent(this,SearchActivity.class);
        if(dark) intent.putExtra("dark",true);
        if(user!=null) intent.putExtra("signed_in",true);
        startActivity(intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,view,view.getTransitionName()).toBundle());
        overridePendingTransition(R.anim.up_start,R.anim.up_end);
    }

    public void showSettings(View view)
    {
        Animation go = AnimationUtils.loadAnimation(this,R.anim.right_end);
        Animation come = AnimationUtils.loadAnimation(this,R.anim.right_start);
        if(findViewById(R.id.sign_out).getVisibility()==View.VISIBLE)
        {
            findViewById(R.id.sign_out).startAnimation(go);
            findViewById(R.id.sign_out).setVisibility(View.GONE);
            show=false;
        }
        if(!show)
        {
            GridLayout gridLayout = (GridLayout) findViewById(R.id.options);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.toggles);
            gridLayout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            relativeLayout.startAnimation(come);
            findViewById(R.id.popular_categories).setVisibility(View.GONE);
            findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
            findViewById(R.id.contact).setVisibility(View.GONE);
            findViewById(R.id.lv_container_of_all_recycler_views).setVisibility(View.GONE);
            show=true;
        }
        else
        {
            GridLayout gridLayout = (GridLayout) findViewById(R.id.options);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.toggles);
            findViewById(R.id.popular_categories).setVisibility(View.VISIBLE);
            gridLayout.setVisibility(View.VISIBLE);
            gridLayout.startAnimation(come);
            relativeLayout.setVisibility(View.GONE);
            if(user!=null&&recentData!=null) {
                findViewById(R.id.contact).setVisibility(View.VISIBLE);
                findViewById(R.id.lv_container_of_all_recycler_views).setVisibility(View.VISIBLE);
                findViewById(R.id.lv_container_of_all_recycler_views).startAnimation(come);
            }
            else if(user!=null)
            {
                findViewById(R.id.show_quick_and_trending).setVisibility(View.VISIBLE);
                findViewById(R.id.show_quick_and_trending).startAnimation(come);
            }
            show=false;
        }
    }
    private void getRecents()
    {
        findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
        contentReference = FirebaseDatabase.getInstance().getReference("content");
        contentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(((RecyclerView)findViewById(R.id.recycler_recommended)).getAdapter()==null) loadRecommendedContent(snapshot);
                user = FirebaseAuth.getInstance().getCurrentUser();
                if(user==null) return;
                int i;
                int count=0;
                String[] name,link,tag,date,image,database_name;
                latestContent = new String[5];
                String downloadedTempData = "";
                FileOutputStream fileOutputStream = null;
                File file = new File(getApplicationContext().getFilesDir(),"DownloadedVideos.txt");
                if(!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fileOutputStream = openFileOutput("DownloadedVideos.txt",MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                for(i=0;i<=4;i++)
                {
                    latestContent[i]=snapshot.child("latest"+(i+1)).getValue().toString();
                }
                if(user!=null) {
                    ContentData contentData[] = new ContentData[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    ContentData movieData[] = new ContentData[Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    int track = 0;
                    for (i = 1; i <= Integer.parseInt(snapshot.child("movie").getValue().toString()); i++) {
                        contentData[count] = Utility.getContentData(MainActivity.this,snapshot,"movie"+i);
                        movieData[track] = contentData[count];
                        track++;
                        downloadedTempData = downloadedTempData+contentData[count].getName()+"\n";
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[count].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                            if (!new File(getApplicationContext().getExternalFilesDir(null),
                                    contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(contentData[count].getImage()));
                                request.setTitle("Thumbnail for Videos");
                                request.setDescription(getString(R.string.app_name));
                                request.allowScanningByMediaScanner();
                                request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                                        contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setVisibleInDownloadsUi(false);
                                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Objects.requireNonNull(manager).enqueue(request);
                            }
                        } else if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                            Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
                                    new File(getApplicationContext().getExternalFilesDir(null),
                                            contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg"));
                            getContentResolver().delete(updateFileUri, null, null);
                        }
                        count++;
                    }
                    File webSeriesFile = new File(getFilesDir(),"webseries.txt");
                    if(!webSeriesFile.exists()) {
                        try {
                            webSeriesFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    FileOutputStream webSeriesOutputStream = null;
                    try {
                        webSeriesOutputStream = openFileOutput("webseries.txt",MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("comedy").getValue().toString()); i++) {
                        contentData[count] = Utility.getContentData(MainActivity.this,snapshot,"comedy"+i);
                        if(contentData[count].getLink().equals("webseries")) {
                            try {
                                webSeriesOutputStream.write((contentData[count].getName()+"\n").getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        downloadedTempData = downloadedTempData+contentData[count].getName()+"\n";
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("sport").getValue().toString()); i++) {
                        contentData[count] = Utility.getContentData(MainActivity.this,snapshot,"sport"+i);
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("kids").getValue().toString()); i++) {
                        contentData[count] = Utility.getContentData(MainActivity.this,snapshot,"kids"+i);
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("classic").getValue().toString()); i++) {
                        contentData[count] = Utility.getContentData(MainActivity.this,snapshot,"classic"+i);
                        movieData[track] = contentData[count];
                        track++;
                        downloadedTempData = downloadedTempData + contentData[count].getName() + "\n";
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[count].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                            if (!new File(getApplicationContext().getExternalFilesDir(null),
                                    contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(contentData[count].getImage()));
                                request.setTitle("Thumbnail for Videos");
                                request.setDescription(getString(R.string.app_name));
                                request.allowScanningByMediaScanner();
                                request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                                        contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setVisibleInDownloadsUi(false);
                                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Objects.requireNonNull(manager).enqueue(request);
                            }
                        } else if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                            Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
                                    new File(getApplicationContext().getExternalFilesDir(null),
                                            contentData[count].getName().replace(' ', '_').replace(':', '_') + ".jpg"));
                            getContentResolver().delete(updateFileUri, null, null);
                        }
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("news").getValue().toString()); i++) {
                        contentData[count] = Utility.getContentData(MainActivity.this,snapshot,"news"+i);
                        count++;
                    }
                    try {
                        fileOutputStream.write(downloadedTempData.getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recentData = new ContentData[5];
                    String tempString = quick_picks;
                    try
                    {
                        quick_picks.length();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        quick_picks="no data";
                    }
                    if (!quick_picks.equals("no data")) {
                        for (i = 0; i < 5; i++) {
                            String str = tempString.substring(0, tempString.indexOf('\n'));
                            if (i < 4)
                                tempString = tempString.substring(tempString.indexOf('\n') + 1);
                            String category = null, number = null;
                            int j;
                            for (j = 0; j < str.length(); j++) {
                                if (str.charAt(j) > '0' && str.charAt(j) <= '9') {
                                    category = str.substring(0, j);
                                    number = str.substring(j);
                                    break;
                                }
                            }
                            String imageTemp = "", dateTemp = "";
                            if (category.equals("sport"))
                                imageTemp = snapshot.child("sports_image_link").getValue().toString();
                            else
                                imageTemp = snapshot.child(category + "image" + number).getValue().toString();
                            if (!category.equals("kids") && !category.equals("sport"))
                                dateTemp = snapshot.child(category + "date" + number).getValue().toString();
                            recentData[i] = new ContentData(getApplicationContext(), snapshot.child(category + number).getValue().toString(),
                                    imageTemp, snapshot.child(category + "link" + number).getValue().toString(), dateTemp, "", str);
                        }
                    } else {
                        int latestData[] = new int[contentData.length];
                        if(local_history==null) return;
                        for (i = 0; i < contentData.length; i++) {
                            latestData[i] = 0;
                            for (count = local_history.length() - contentData[i].getName().length() - 1; count >= 0 && count >= local_history.length() - 500 && latestData[i] == 0; count--) {
                                    if (local_history.substring(count).contains("\t" + contentData[i].getName() + "\n"))
                                        latestData[i] = count;
                            }
                        }
                        int temp;
                        ContentData tempData;
                        ContentData dummyData[] = contentData;
                        for (i = 0; i <= contentData.length - 2; i++) {
                            for (int j = 0; j <= dummyData.length - 2 - i; j++) {
                                if (latestData[j] < latestData[j + 1]) {
                                    temp = latestData[j];
                                    latestData[j] = latestData[j + 1];
                                    latestData[j + 1] = temp;
                                    tempData = dummyData[j];
                                    dummyData[j] = dummyData[j + 1];
                                    dummyData[j + 1] = tempData;
                                }
                            }
                        }
                        recentData = new ContentData[5];
                        quick_picks = "";
                        for (i = 0; i <= recentData.length - 1; i++) {
                            recentData[i] = dummyData[i];
                            quick_picks = quick_picks + recentData[i].getDataBaseName() + "\n";
                        }
                        UserName.setQuickPicks(quick_picks);
                        new Sync().addToQuickPicks(getApplicationContext(), recentData[0].getDataBaseName());
                    }
                    SmallContentAdapter smallContentAdapter = new SmallContentAdapter(recentData,MainActivity.this, dark);
                    smallContentAdapter.setShowDetails(true);
                    if (smallContentAdapter.getItemCount() == 0) {
                        getRecents();
                        return;
                    }
                    Map<String, Object> trend = new HashMap<>();
                    for (i = 0; i < contentData.length; i++) {
                        if (i < 5) trend.put(String.valueOf(i + 1), recentData[i].getName());
                        else trend.put(String.valueOf(i + 1), contentData[i].getName());
                    }
                    trendData = new ContentData[trendingArrayList.size()];
                    String temporary_name, contentName, contentImage, contentLink, contentDescription;
                    for (i = 0; i < trendData.length; i++) {
                        trendData[i] = Utility.getContentData(MainActivity.this,snapshot,trendingArrayList.get(i));
                    }
                    count = 0;
                    if(show) return;
                    SmallContentAdapter trendAdapter = new SmallContentAdapter(trendData,MainActivity.this, dark);
                    if(((SmallContentAdapter)trendView.getAdapter()).isShimmer()) trendView.setAdapter(trendAdapter);
                    else ((SmallContentAdapter)trendView.getAdapter()).setContentData(trendData);
                    trendView.setVisibility(View.VISIBLE);
                    findViewById(R.id.trend).setVisibility(View.VISIBLE);
                    if(((SmallContentAdapter)recyclerView.getAdapter()).isShimmer()) recyclerView.setAdapter(smallContentAdapter);
                    else ((SmallContentAdapter)recyclerView.getAdapter()).setContentData(recentData);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.quick).setVisibility(View.VISIBLE);
                    recentlyAddedContent = new ContentData[5];
                    for (i = 0; i <= 4; i++) {
                        recentlyAddedContent[i] = Utility.getContentData(MainActivity.this,snapshot,latestContent[i]);
                    }
                    if(((SmallContentAdapter)recentlyAdded.getAdapter()).isShimmer()) recentlyAdded.setAdapter(new SmallContentAdapter(recentlyAddedContent, MainActivity.this, dark));
                    else ((SmallContentAdapter)recentlyAdded.getAdapter()).setContentData(recentlyAddedContent);
                    recentlyAdded.setVisibility(View.VISIBLE);
                    findViewById(R.id.recently_added).setVisibility(View.VISIBLE);
                    findViewById(R.id.settings).setVisibility(View.VISIBLE);
                    findViewById(R.id.load_options).setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.profile_email)).setText("Tap for more information");
                    first = false;
                    int select = (int) (System.currentTimeMillis() % movieData.length);
                    if (getIntent().hasExtra("personal") && personal) {
                        personal = false;
                        Intent intent;
                        intent = new Intent(getApplicationContext(), SearchActivity.class);
                        if (dark) intent.putExtra("dark", true);
                        intent.putExtra("search", movieData[select].getName());
                        if(movieData[select].getLink().startsWith("(video)")) intent.putExtra("open",true);
                        intent.putExtra("signed_in", true);
                        intent.putExtra("personal", true);
                        startActivity(intent);
                        finish();
                    }
                    personal = false;
                    if (restarted || new File(getApplicationContext().getFilesDir(), "Alarm.txt").exists()) {
                        restarted = false;
                        return;
                    }
                    try {
                        new File(getApplicationContext().getFilesDir(), "Alarm.txt").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    select = (int) ((System.currentTimeMillis() + 5) % movieData.length);
                    AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();
                    Calendar actualCalendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int hour = actualCalendar.get(Calendar.HOUR_OF_DAY) + 3;
                    int minute = 0;
                    if(last_sign_in==null) last_sign_in="0000/00/00 at 00:00";
                    if (Integer.parseInt(last_sign_in.substring(0, 4)) == actualCalendar.get(Calendar.YEAR)
                            && Integer.parseInt(last_sign_in.substring(5, 7)) == actualCalendar.get(Calendar.MONTH) + 1
                            && Integer.parseInt(last_sign_in.substring(8, 10)) == actualCalendar.get(Calendar.DAY_OF_MONTH)
                            && Integer.parseInt(last_sign_in.substring(14, 16)) > actualCalendar.get(Calendar.HOUR_OF_DAY) - 3) {
                        restarted = false;
                        return;
                    }
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getDownloadedMovies()
    {
        String name, downloaded;
        downloaded="";
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput("DownloadedVideos.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((name= bufferedReader.readLine())!=null)
            {
                if(new File(getApplicationContext().getExternalFilesDir(null),name.replace(' ','_').replace(':','_')+".mp4").exists())
                {
                    downloaded = downloaded + name + "\n";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return downloaded;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloaded;
    }
    private void makeTimeFiles()
    {
        if(movie_watch_session_times.equals("0")) return;
        try {
            if(!new File(this.getFilesDir(),"UserWatchTimings.txt").exists()) new File(this.getFilesDir(),"UserWatchTimings.txt").createNewFile();
            FileOutputStream fos = openFileOutput("UserWatchTimings.txt",MODE_PRIVATE);
            fos.write(movie_watch_session_times.getBytes());
            fos.close();
            try {
                FileInputStream fis = openFileInput("UserWatchTimings.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                FileOutputStream fileOutputStream;
                String str;
                while ((str=br.readLine())!=null)
                {
                    File file = new File(getApplicationContext().getFilesDir(),str);
                    getContentResolver().delete(FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID+".provider",file),null,null);
                    file.createNewFile();
                    fileOutputStream=openFileOutput(str,MODE_PRIVATE);
                    str=br.readLine();
                    if(str==null)
                    {
                        findViewById(R.id.show_quick_and_trending).callOnClick();
                        return;
                    }
                    fileOutputStream.write(str.getBytes());
                    fileOutputStream.close();
                }
                if(movie_watch_session_times.equals("")) movie_watch_session_times="0";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(show) showSettings(findViewById(R.id.settings));
        else super.onBackPressed();
    }

    private void deleteExistingApk()
    {
        if(new File(getApplicationContext().getExternalFilesDir(null),getString(R.string.app_name)+"_"+
                BuildConfig.VERSION_NAME.replace(' ','_')+".apk").exists())
        {
            Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",
                    new File(getApplicationContext().getExternalFilesDir(null),getString(R.string.app_name)+"_"+
                            BuildConfig.VERSION_NAME.replace(' ','_')+".apk"));
            getContentResolver().delete(updateFileUri,null,null);
        }
    }

    public void showDownloads(View view) {
        Intent intent = new Intent(this,QueuedDownloadsActivity.class);
        intent.putExtra("download",true);
        intent.putExtra("offline",true);
        //intent.putExtra("content_type","download");
        if(dark) intent.putExtra("dark",true);
        startActivity(intent);
    }

    public void playLocalVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent,"Complete action using..."),PICKFILE_RESULT_CODE);
    }
    private void sendIntroductoryNotification(int delay)
    {
        Intent intent = new Intent(getApplicationContext(),SplashActivity.class);
        intent.putExtra("search","movie");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),100,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"personal")
                .setContentTitle("Welcome "+user.getDisplayName())
                .setContentText("Let's explore some movies")
                .setSmallIcon(R.drawable.notification)
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        notification = builder.build();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManagerCompat.from(getApplicationContext()).notify(999,notification);
            }
        },delay);
    }

    public void sendFeedback(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.devansh.talkative","com.devansh.talkative.activities.MainActivity"));
        intent.putExtra("email_id","team.entertainment108@gmail.com");
        try{
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Install Talk-A-Tive app for feedback", Toast.LENGTH_SHORT).show();
            Intent talkativeIntent = new Intent(this,WebActivity.class);
            talkativeIntent.putExtra("link","https://drive.google.com/drive/folders/1QFoRHHc70aJVnlIDlvDBBCq3LP2IvF03?usp=sharing");
            startActivity(talkativeIntent);
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getRecents();
            }
        }).start();
        registerReceiver(toggleCastreceiver,new IntentFilter("CAST"));
    }
    public void broadcastMessage(View view) {
        if(!Utility.checkForBiometrics(this)) return;
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                startActivity(new Intent(MainActivity.this,FirebaseNotificationActivity.class));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Sensitive feature")
                .setSubtitle("Verify your identity")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    @SuppressLint("SetTextI18n")
    public void downloadApk(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.watch_pop_up);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ((TextView)dialog.findViewById(R.id.title)).setText(getString(R.string.app_name));
        dialog.findViewById(R.id.watchlist).setVisibility(View.GONE);
        FirebaseDatabase.getInstance().getReference("app_versions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if(userType.equals("beta")){
                    ((TextView) dialog.findViewById(R.id.watch_text)).setText("Mobile version (" + snapshot.child("beta_name").getValue().toString() + ")");
                    ((TextView) dialog.findViewById(R.id.text_download)).setText("Android TV version (" + snapshot.child("tv_beta_name").getValue().toString() + ")");
                }
                else {
                    ((TextView) dialog.findViewById(R.id.watch_text)).setText("Mobile version (" + snapshot.child("version_name").getValue().toString() + ")");
                    ((TextView) dialog.findViewById(R.id.text_download)).setText("Android TV version (" + snapshot.child("tv_version_name").getValue().toString() + ")");
                }
                if(dark){
                    dialog.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#303030"));
                    ((TextView)dialog.findViewById(R.id.title)).setTextColor(Color.WHITE);
                    ((TextView)dialog.findViewById(R.id.description)).setTextColor(Color.WHITE);
                }
                dialog.findViewById(R.id.watch_card).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(userType.equals("beta")) downloadWithParams(getString(R.string.app_name)+" Mobile",snapshot.child("beta_name").getValue().toString(),snapshot.child("beta_link").getValue().toString());
                        else downloadWithParams(getString(R.string.app_name)+" Mobile",snapshot.child("version_name").getValue().toString(),snapshot.child("update_link").getValue().toString());
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.more_options).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(userType.equals("beta")) downloadWithParams(getString(R.string.app_name)+" Android TV",snapshot.child("tv_beta_name").getValue().toString(),snapshot.child("tv_beta_link").getValue().toString());
                        else downloadWithParams(getString(R.string.app_name)+" Android TV",snapshot.child("tv_version_name").getValue().toString(),snapshot.child("tv_update_link").getValue().toString());
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dialog.show();
    }


    public void downloadWithParams(String name, String description, String link){
        if(link.equals("no link")||description.equals("0")) {
            Toast.makeText(this,"Apk not published yet",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
            request.setTitle(name);
            request.setDescription(description);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name.replace(' ', '_') + ".apk");
            request.allowScanningByMediaScanner();
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(request);
            Toast.makeText(this, "Downloading " + name, Toast.LENGTH_SHORT).show();
        } catch (SecurityException exception) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1008);
            }
        }

    }

    public void goToCastControl(View view) {
        startActivity(new Intent(getApplicationContext(),ExpandedControls.class));
    }

    public void stopCasting(View view) {
        try {
            CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
            findViewById(R.id.castCard).setVisibility(View.GONE);
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(),"An error occured",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),ExpandedControls.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(toggleCastreceiver);
    }

    public void showWatchlist(View view) {
        startActivity(new Intent(this,WatchlistActivity.class));
    }
    
    public void showUserIdDialog(View view){
        if(!Utility.checkForBiometrics(this)) return;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_user_id);
        ((EditText)dialog.findViewById(R.id.et_user_id)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{
                    String userId = charSequence.toString();
                    if(userId.length()>0){
                        dialog.dismiss();
                        Intent intent = new Intent(MainActivity.this,UserDetailsActivity.class);
                        intent.putExtra("id",userId);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                dialog.show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Sensitive feature")
                .setSubtitle("Verify your identity")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}