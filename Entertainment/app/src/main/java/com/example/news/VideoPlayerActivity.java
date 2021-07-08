package com.example.news;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.mediarouter.app.MediaRouteButton;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.wtekiela.opensub4j.api.OpenSubtitlesClient;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesClientImpl;
import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.Response;
import com.github.wtekiela.opensub4j.response.ResponseStatus;
import com.github.wtekiela.opensub4j.response.ServerInfo;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VideoPlayerActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static VideoView videoView;
    private CastContext castContext;
    private boolean activity_start=true;
    private int position=0;
    private boolean complete=false;
    private long time_hide_show;
    private View decorView;
    private boolean pip=false;
    private boolean lock=false;
    private static boolean pause_by_pip=false;
    private SeekBar seekBar;
    private boolean negative=false;
    private boolean videoOpened = false;
    private ListView listView;
    private MediaPlayer player;
    private List<String> stringList;
    private GestureDetector gestureDetector;
    private int trackIndex = 0;
    private long time;
    private int maxBrightness=0;
    private int originalBrightness=0;
    private int brightnessMode;
    private int brightness = 0;
    private long vol_bright_time;
    private int seriesVideoPosition;
    private boolean initialSeekDone = false;
    private boolean isSeriesComplete = false;
    private boolean goingForPermission = false;
    private boolean isThereAPreviousEpisode;
    private boolean isThereANextEpisode;
    private boolean goToNext = false;
    private boolean goToPrevious = false;
    private int currentSeason;
    private int currentEpisode;
    private PlaybackState playbackState;
    private PlaybackLocation mLocation;
    private PhoneIncomingCallListener phoneCallListener;
    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;
    private int video_length;
    private String subtitleLink;
    private boolean isThereSubtitle;

    public enum PlaybackLocation{
        LOCAL,
        REMOTE
    }
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }
    BroadcastReceiver toggleReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            if(videoView.isPlaying()) videoView.pause();
            else videoView.start();
            int width,height;
            width=videoView.getMeasuredWidth();
            height=videoView.getMeasuredHeight();
            if(width==0||height==0){
                width=height=9;
            }
            else if(width>2*height){
                width=18;
                height=9;
            }
            else if(height>2*width){
                height=18;
                width=9;
            }
            PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(width, height))
                    .setActions(createAction())
                    .build();
            setPictureInPictureParams(pictureInPictureParams);
        }
    };
    BroadcastReceiver rewindReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(videoView.getCurrentPosition()>=10000)
                videoView.seekTo(videoView.getCurrentPosition()-10000);
        }
    };
    BroadcastReceiver forwardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(videoView.getCurrentPosition()<=videoView.getDuration()-10000)
                videoView.seekTo(videoView.getCurrentPosition()+10000);
        }
    };
    BroadcastReceiver playReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isInPictureInPictureMode())
            {
                videoView.start();
                int width,height;
                width=videoView.getMeasuredWidth();
                height=videoView.getMeasuredHeight();
                if(width==0||height==0){
                    width=height=9;
                }
                else if(width>2*height){
                    width=18;
                    height=9;
                }
                else if(height>2*width){
                    height=18;
                    width=9;
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder()
                            .setAspectRatio(new Rational(width, height))
                            .setActions(createAction())
                            .build();
                    setPictureInPictureParams(pictureInPictureParams);
                }
                return;
            }
            findViewById(R.id.play_button).callOnClick();
            if(lock)
            {
                findViewById(R.id.play_button).setVisibility(View.GONE);
                findViewById(R.id.pause_button).setVisibility(View.GONE);
            }
        }
    };
    BroadcastReceiver pauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isInPictureInPictureMode())
            {
                videoView.pause();
                int width,height;
                width=videoView.getMeasuredWidth();
                height=videoView.getMeasuredHeight();
                if(width==0||height==0){
                    width=height=9;
                }
                else if(width>2*height){
                    width=18;
                    height=9;
                }
                else if(height>2*width){
                    height=18;
                    width=9;
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder()
                            .setAspectRatio(new Rational(width, height))
                            .setActions(createAction())
                            .build();
                    setPictureInPictureParams(pictureInPictureParams);
                }
                return;
            }
            findViewById(R.id.pause_button).callOnClick();
            if(lock)
            {
                findViewById(R.id.play_button).setVisibility(View.GONE);
                findViewById(R.id.pause_button).setVisibility(View.GONE);
            }
        }
    };
    BroadcastReceiver toggleByMediaButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isInPictureInPictureMode())
            {
                if(videoView.isPlaying()) videoView.pause();
                else videoView.start();
                int width,height;
                width=videoView.getMeasuredWidth();
                height=videoView.getMeasuredHeight();
                if(width==0||height==0){
                    width=height=9;
                }
                else if(width>2*height){
                    width=18;
                    height=9;
                }
                else if(height>2*width){
                    height=18;
                    width=9;
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder()
                            .setAspectRatio(new Rational(width, height))
                            .setActions(createAction())
                            .build();
                    setPictureInPictureParams(pictureInPictureParams);
                }
            }
            else {
                if (videoView.isPlaying()) findViewById(R.id.pause_button).callOnClick();
                else findViewById(R.id.play_button).callOnClick();
                if(lock)
                {
                    findViewById(R.id.play_button).setVisibility(View.GONE);
                    findViewById(R.id.pause_button).setVisibility(View.GONE);
                }
            }
            unregisterReceiver(this);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    registerReceiver(toggleByMediaButtonReceiver,new IntentFilter("TOGGLE"));
                }
            },1000);
        }
    };
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try {
            if (hasFocus) decorView.setSystemUiVisibility(hideSystemBars());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    private List<RemoteAction> createAction()
    {
        RemoteAction remoteAction;
        List<RemoteAction> list = new ArrayList<>();
        Intent intent;
        PendingIntent pendingIntent;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            intent = new Intent("REWIND");
            pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteAction = new RemoteAction(Icon.createWithResource(getApplicationContext(),
                    R.drawable.ic_baseline_fast_rewind_24),
                    "rewind",
                    "rewind",
                    pendingIntent);
            list.add(remoteAction);
            intent = new Intent("PLAY/PAUSE");
            pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteAction = new RemoteAction(Icon.createWithResource(getApplicationContext(),
                    videoView.isPlaying()?R.drawable.ic_baseline_pause_24:R.drawable.ic_baseline_play_arrow_24),
                    "play/pause",
                    "play/pause",
                    pendingIntent);
            list.add(remoteAction);
            intent = new Intent("FORWARD");
            pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteAction = new RemoteAction(Icon.createWithResource(getApplicationContext(),
                    R.drawable.ic_baseline_fast_forward_24),
                    "forward",
                    "forward",
                    pendingIntent);
            list.add(remoteAction);
            return list;
        }
        return null;
    }
    private void startTimeCounting()
    {
        int progress;
        String str="";
        seekBar.setMax(videoView.getDuration()/1000);
        position=videoView.getCurrentPosition()/1000;
        if(negative) {
            str = "-";
            position = (videoView.getDuration() - videoView.getCurrentPosition())/1000;
        }
        if(position/3600>0) str=str+position/3600+":";
        if((position%3600)/60<10) str=str+"0";
        str=str+(position%3600)/60+":";
        if(position%60<10) str=str+"0";
        str=str+position%60+" / ";
        position=videoView.getDuration()/1000;
        if(position/3600>0) str=str+position/3600+":";
        if((position%3600)/60<10) str=str+"0";
        str=str+(position%3600)/60+":";
        if(position%60<10) str=str+"0";
        str=str+position%60;
        TextView textView = findViewById(R.id.time_track);
        textView.setText(str);
        textView = findViewById(R.id.time_track_center);
        textView.setText(str);
        if(position>0) seekBar.setProgress(videoView.getCurrentPosition()/1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if((findViewById(R.id.time_track).getVisibility()==View.VISIBLE||findViewById(R.id.time_track_center).getVisibility()==View.VISIBLE)&&videoView.isPlaying()) startTimeCounting();
            }
        },1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isThereSubtitle = false;
        if(!Settings.System.canWrite(getApplicationContext()))
        {
            Toast.makeText(getApplicationContext(),"Permission required to adjust volume and brightness in video player",Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+BuildConfig.APPLICATION_ID));
                    startActivity(intent);
                    goingForPermission = true;
                    finish();
                }
            },1500);
        }
        if(getIntent().hasExtra("dbName")){
            FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("dbName")+"e"+getIntent().getStringExtra("episode_number")+"sub")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try{
                                subtitleLink = snapshot.getValue().toString();
                            } catch (Exception e) {
                                subtitleLink = null;
                            }
                            if(snapshot.getValue()==null
                                    || new File(getExternalFilesDir(null),getIntent().getStringExtra("name")
                                    .replace(':','_').replace(' ','_')+".srt").exists()) return;
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.getValue().toString()));
                            request.setTitle(getIntent().getStringExtra("name"));
                            request.setDescription("subtitles");
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                            request.setVisibleInDownloadsUi(false);
                            request.setDestinationInExternalFilesDir(getApplicationContext(),null,getIntent().getStringExtra("name")
                            .replace(':','_').replace(' ','_')+".srt");
                            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        else if(getIntent().hasExtra("movie_db")&&getIntent().getStringExtra("movie_db")!=null){
            FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("movie_db")+"sub").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try{
                        subtitleLink = snapshot.getValue().toString();
                    } catch (Exception e) {
                        subtitleLink = null;
                    }
                    if(snapshot.getValue()==null
                            || new File(getExternalFilesDir(null),getIntent().getStringExtra("name")
                            .replace(':','_').replace(' ','_')+".srt").exists()) return;
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.getValue().toString()));
                    request.setTitle(getIntent().getStringExtra("name"));
                    request.setDescription("subtitles");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                    request.setVisibleInDownloadsUi(false);
                    request.setDestinationInExternalFilesDir(getApplicationContext(),null,getIntent().getStringExtra("name")
                            .replace(':','_').replace(' ','_')+".srt");
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        setContentView(R.layout.activity_video_player);
        MediaRouteButton mMediaRouteButton = (androidx.mediarouter.app.MediaRouteButton) findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);
        castContext = CastContext.getSharedInstance(this);
        mCastSession = castContext.getSessionManager().getCurrentCastSession();
        findViewById(R.id.video_layout).setLayerType(View.LAYER_TYPE_NONE,null);
        try {
            originalBrightness=Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
            brightnessMode=Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        brightness=originalBrightness;
        if(originalBrightness>2047) maxBrightness=4095;
        else if(originalBrightness>255) maxBrightness=2047;
        else maxBrightness=255;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        registerReceiver(toggleByMediaButtonReceiver,new IntentFilter("TOGGLE"));
        registerReceiver(playReceiver,new IntentFilter("PLAY"));
        registerReceiver(pauseReceiver,new IntentFilter("PAUSE"));
        registerReceiver(toggleReceiver,new IntentFilter("PLAY/PAUSE"));
        registerReceiver(forwardReceiver,new IntentFilter("FORWARD"));
        registerReceiver(rewindReceiver,new IntentFilter("REWIND"));
        gestureDetector = new GestureDetector(this,this);
        gestureDetector.setOnDoubleTapListener(this);
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        ComponentName componentName = new ComponentName(getPackageName(),BluetoothPlayPauseReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(componentName);
        videoView = (VideoView) findViewById(R.id.video);
        listView = findViewById(R.id.list_of_audio_tracks);
        seekBar=findViewById(R.id.seekbar);
        startTimeCounting();
        hideControls();
        checkPreviousAndNextEpisodes();
        phoneCallListener = new PhoneIncomingCallListener(getApplicationContext());
        if(getIntent().hasExtra("online"))
        {
            final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Video Data").document(getIntent().getStringExtra("name"));
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    long view_count = 0;
                    try {
                        view_count = Long.parseLong(documentSnapshot.getString("Views"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    Map<String,Object> data = new HashMap<>();
                    data.put("Views",String.valueOf(view_count+1));
                    ((TextView)findViewById(R.id.view_count)).setText((view_count+1)+" Views");
                    documentReference.set(data);
                }
            });
        }
        if(!getIntent().hasExtra("name")&&getIntent().getType()==null)
        {
            if(mCastSession==null) {
                Toast.makeText(getApplicationContext(), "No video detected", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            return;
        }
        if(isInPictureInPictureMode())
        {
            Intent intent = new Intent(this,VideoPlayerActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        }
        //     mediaController = new MediaController(this);
    //    mediaController.setAnchorView(videoView);
        if(getIntent().hasExtra("uri")) videoView.setVideoURI(Uri.parse(getIntent().getStringExtra("uri")));
        else if(getIntent().getType()!=null)
        {
            videoView.setVideoURI(getIntent().getData());
        }
        else if(new File(getApplicationContext().getExternalFilesDir(null),
                getIntent().getStringExtra("name").replace(' ','_').replace(':','_')+".mp4").exists()
                &&!isFileDownloading())
        {
            /*videoView.setVideoPath("/storage/emulated/0/Download/Entertainment/"+
                    getIntent().getStringExtra("name").replace(' ','_').replace(':','_')+".mp4");*/
            //videoView.setVideoURI(Uri.parse(getIntent().getStringExtra("uri")));
            videoView.setVideoPath("/storage/emulated/0/android/data/com.example.news/files/"+
                    getIntent().getStringExtra("name").replace(' ','_').replace(':','_')+".mp4");
            //Toast.makeText(getApplicationContext(),"Playing download file\nsaving internet",Toast.LENGTH_SHORT).show();
        }
        else if(!getIntent().hasExtra("link"))
        {
            Toast.makeText(getApplicationContext(),"Video not found",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else videoView.setVideoURI(Uri.parse(getIntent().getStringExtra("link")));
        //videoView.setMediaController(mediaController);
        videoView.requestFocus();
        decorView=getWindow().getDecorView();
        final TextView textView = findViewById(R.id.video_title);
        textView.setText(getIntent().getStringExtra("name"));
        findViewById(R.id.time_track).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                negative=!negative;
                startTimeCounting();
            }
        });
        findViewById(R.id.time_track_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                negative=!negative;
                startTimeCounting();
            }
        });
        findViewById(R.id.track_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listView.getVisibility()==View.VISIBLE)
                {
                    listView.setVisibility(View.GONE);
                    findViewById(R.id.play_button).callOnClick();
                    return;
                }
                findViewById(R.id.pause_button).callOnClick();
                listView.setVisibility(View.VISIBLE);
                listView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in_bottom));
            }
        });
        findViewById(R.id.video_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //builder.show();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {
                player = mediaPlayer;
                video_length = videoView.getDuration();
                videoView.start();
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                telephonyManager.listen(phoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
                        ((ProgressBar)findViewById(R.id.buffered_progress)).setProgress(percent);
                    }
                });
                String name = getIntent().getStringExtra("name");
                if(name==null){
                        name = getIntent().getData().getPath();
                        name = name.substring(name.lastIndexOf('/') + 1);
                        if(name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
                }
                if(new File(getApplicationContext().getExternalFilesDir(null),name.replace(' ','_')
                        .replace(':','_')+".srt").exists()){
                    try {
                        mediaPlayer.addTimedTextSource("storage/emulated/0/android/data/com.example.news/files/"+name.replace(' ','_')
                                .replace(':','_')+".srt",MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final MediaPlayer.TrackInfo trackInfos[] = mediaPlayer.getTrackInfo();
                final ArrayList<Integer> audioTracksIndex = new ArrayList<>();
                for(int i=0;i<trackInfos.length;i++)
                {
                    Log.println(Log.ASSERT,"track",trackInfos[i].toString());
                    if(trackInfos[i].getTrackType()== MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)
                    {
                        audioTracksIndex.add(i);
                    }
                    else if(trackInfos[i].getTrackType()== MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT)
                    {
                        isThereSubtitle = true;
                        Log.println(Log.ASSERT,"srt","yes");
                        mediaPlayer.selectTrack(i);
                        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            @Override
                            public void onSeekComplete(MediaPlayer mediaPlayer) {

                            }
                        });
                        mediaPlayer.setOnTimedTextListener(new MediaPlayer.OnTimedTextListener() {
                            @Override
                            public void onTimedText(MediaPlayer mediaPlayer, TimedText timedText) {
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (timedText == null) {
                                            ((TextView) findViewById(R.id.subtitle)).setText("");
                                            return;
                                        }
                                        try {
                                            ((TextView) findViewById(R.id.subtitle)).setText(timedText.getText()
                                                    .replace("<i>", "").replace("</i>", ""));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (timedText.getText().replace("<i>","").replace("</i>","").equals(((TextView) findViewById(R.id.subtitle)).getText().toString()))
                                                        ((TextView) findViewById(R.id.subtitle)).setText("");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },5000);
                                    }
                                });
                            }
                        });
                        mediaPlayer.start();
                    }
                }
                mediaPlayer.start();
                stringList= new ArrayList<>();
                for(int i=0;i<audioTracksIndex.size();i++)
                {
                    String temp = "Track "+(i+1);
                    if(trackInfos[audioTracksIndex.get(i)].getLanguage()!=null
                            &&!trackInfos[audioTracksIndex.get(i)].getLanguage().equals("und"))
                    {
                        String language = trackInfos[audioTracksIndex.get(i)].getLanguage();
                        if(language.equals("eng")) temp="English";
                        else if(language.equals("hin")) temp="Hindi";
                    }
                    if(player.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)==i+1) temp = temp + " (current)";
                    stringList.add(temp);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,stringList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, final int position_in_list, long l) {
                        videoView.pause();
                        final int temp = videoView.getCurrentPosition();
                        mediaPlayer.selectTrack(audioTracksIndex.get(position_in_list));
                        findViewById(R.id.play_button).callOnClick();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!stringList.get(position_in_list).contains("(current)")) videoView.seekTo(temp);
                                stringList.clear();
                                for(int i=0;i<audioTracksIndex.size();i++)
                                {
                                    String temp = "Track "+(i+1);
                                    if(trackInfos[audioTracksIndex.get(i)].getLanguage()!=null
                                            &&!trackInfos[audioTracksIndex.get(i)].getLanguage().equals("und"))
                                    {
                                        String language = trackInfos[audioTracksIndex.get(i)].getLanguage();
                                        if(language.equals("eng")) temp="English";
                                        else if(language.equals("hin")) temp="Hindi";
                                    }
                                    if(player.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)==i+1) temp = temp + " (current)";
                                    stringList.add(temp);
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,stringList);
                                listView.setAdapter(adapter);
                            }
                        },500);
                        findViewById(R.id.list_of_audio_tracks).setVisibility(View.GONE);
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(videoView.getMeasuredWidth()>videoView.getMeasuredHeight())
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                },500);
                if(!getIntent().hasExtra("name"))
                {
                    findViewById(R.id.load).setVisibility(View.GONE);
                    TextView textView = findViewById(R.id.video_title);
                    textView.setText(getIntent().getData().getPath().substring(getIntent().getData().getPath().lastIndexOf('/')+1));
                    findViewById(R.id.play_button).callOnClick();
                }
                if(!pip)
                {

                    try {
                        FileInputStream fis = null;
                        if(!getIntent().hasExtra("name"))
                        {
                            String str=getIntent().getData().getPath();
                            str=str.substring(str.lastIndexOf('/')+1).replace(' ','_').replace(':','_');
                            fis = getApplicationContext().openFileInput(str+".txt");
                        }
                        else fis = getApplicationContext().openFileInput(getIntent().getStringExtra("name").replace(' ','+')+".txt");
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                        int temp_track=0;
                        //Toast.makeText(getApplicationContext(),br.readLine(),Toast.LENGTH_SHORT).show();
                        try {
                            String str = br.readLine();
                            String temp = str;
                            if(str.contains("\t")) str=str.substring(0,str.indexOf('\t'));
                            position = Integer.parseInt(str);
                            if(temp.contains("\t")) temp = temp.substring(temp.indexOf('\t')+1);
                            if(temp.contains("\t")) temp = temp.substring(temp.indexOf('\t')+1);
                            else temp = null;
                            str = temp;
                            if(str!=null) temp_track = Integer.parseInt(str);
                            if(temp_track>0&&player!=null)
                            {
                                player.selectTrack(audioTracksIndex.get(temp_track));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        stringList.clear();
                                        for(int i=0;i<audioTracksIndex.size();i++)
                                        {
                                            String string = "Track "+(i+1);
                                            if(player.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)==i+1) string=string+" (current)";
                                            stringList.add(string);
                                        }
                                        listView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, stringList));
                                    }
                                },500);
                            }
                        }
                        catch (Exception exception)
                        {
                            position=0;
                        }
                        if(!getIntent().hasExtra("begin")&&activity_start&&!getIntent().hasExtra("position")) videoView.seekTo(position);
                        else if(initialSeekDone) videoView.seekTo(position);
                        else if(getIntent().hasExtra("position")) videoView.seekTo(Integer.parseInt(getIntent().getStringExtra("position")));
                        initialSeekDone = true;
                    } catch (FileNotFoundException e) {
                        if(getIntent().hasExtra("position")&&!initialSeekDone) videoView.seekTo(Integer.parseInt(getIntent().getStringExtra("position")));
                        initialSeekDone = true;
                    }
                    activity_start=false;
                }
                if(isInPictureInPictureMode())
                {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int width,height;
                                width=videoView.getMeasuredWidth();
                                height=videoView.getMeasuredHeight();
                                if(width==0||height==0){
                                    width=height=9;
                                }
                                else if(width>2*height){
                                    width=18;
                                    height=9;
                                }
                                else if(height>2*width){
                                    height=18;
                                    width=9;
                                }
                                PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder()
                                        .setAspectRatio(new Rational(width, height))
                                        .setActions(createAction())
                                        .build();
                                setPictureInPictureParams(pictureInPictureParams);
                            }
                        },1000);
                    }
                    return;
                }
                if(mCastSession!=null){
                    castContext.getSessionManager().endCurrentSession(true);
                }
                showControls();
                seekBar.setMax(videoView.getDuration()/1000);
                startTimeCounting();
                time_hide_show = System.currentTimeMillis();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!videoView.isPlaying()||System.currentTimeMillis()<time_hide_show+5000) return;
                        if(listView.getVisibility()==View.VISIBLE) return;
                        hideControls();
                    }
                }, 5000);
            }
        });
        findViewById(R.id.next_episode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextEpisode();
            }
        });
        findViewById(R.id.previous_episode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPreviousEpisode();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean pause_while_seek=false;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if(fromTouch)
                {
                    videoView.seekTo(progress*1000);
                    startTimeCounting();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(videoView.isPlaying()) pause_while_seek=true;
                findViewById(R.id.pause_button).callOnClick();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(pause_while_seek) findViewById(R.id.play_button).callOnClick();
                pause_while_seek=false;
            }
        });
        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(getRequestedOrientation())
                {
                    case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
                    case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        break;
                    default:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
        findViewById(R.id.video_layout).setOnTouchListener(new View.OnTouchListener() {
            long time;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.start();
                time_hide_show = System.currentTimeMillis();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                startTimeCounting();
                findViewById(R.id.play_button).setVisibility(View.GONE);
                findViewById(R.id.pause_button).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_come));
                findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
                findViewById(R.id.play_button).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_go));
                if(findViewById(R.id.video_title).getVisibility()!=View.VISIBLE ){
                    if(findViewById(R.id.blackish).getVisibility()==View.VISIBLE) {
                        findViewById(R.id.blackish).setVisibility(View.GONE);
                        findViewById(R.id.blackish).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
                    }
                    findViewById(R.id.pause_button).setVisibility(View.GONE);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView.isPlaying()&&System.currentTimeMillis()>=time_hide_show+5000) {
                            listView.setVisibility(View.GONE);
                            hideControls();
                        }
                    }
                }, 5000);
            }
        });
        findViewById(R.id.pause_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.pause();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                findViewById(R.id.pause_button).setVisibility(View.GONE);
                findViewById(R.id.pause_button).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_go));
                findViewById(R.id.play_button).setVisibility(View.VISIBLE);
                findViewById(R.id.play_button).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_come));
                if (findViewById(R.id.blackish).getVisibility() != View.VISIBLE) {
                    findViewById(R.id.blackish).setVisibility(View.VISIBLE);
                    findViewById(R.id.blackish).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                }
            }
        });
        findViewById(R.id.rewind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoView.getCurrentPosition()>=10000) {
                    time_hide_show = System.currentTimeMillis();
                    videoView.seekTo(videoView.getCurrentPosition() - 10000);
                    findViewById(R.id.rewind).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anticlockwise));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.println(Log.ASSERT,"diff",(System.currentTimeMillis()-time_hide_show)+"");
                            long diff = System.currentTimeMillis()-time_hide_show;
                            if (videoView.isPlaying()&&diff>=5000) {
                                listView.setVisibility(View.GONE);
                                hideControls();
                            }
                        }
                    }, 5000);
                }
                if(!videoView.isPlaying()) startTimeCounting();
            }
        });
        findViewById(R.id.forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoView.getCurrentPosition()<=videoView.getDuration()-10000) {
                    time_hide_show = System.currentTimeMillis();
                    findViewById(R.id.forward).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.clockwise));
                    videoView.seekTo(videoView.getCurrentPosition() + 10000);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (videoView.isPlaying()&&System.currentTimeMillis()>=time_hide_show+5000) {
                                listView.setVisibility(View.GONE);
                                hideControls();
                            }
                        }
                    }, 5000);
                }
                if(!videoView.isPlaying()) startTimeCounting();
            }
        });
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPause();
                finish();
            }
        });
        findViewById(R.id.minimize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUserLeaveHint();
            }
        });
        findViewById(R.id.lock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lock)
                {
                    hideControls();
                    lock=true;
                    showControls();
                }
                else
                {
                    startTimeCounting();
                    lock=false;
                    showControls();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideControls();
                        }
                    },5000);
                }
            }
        });
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility==0) decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                complete=true;
                onPause();
                if(getIntent().hasExtra("dbName")){
                    goToNextEpisode();
                }
                else finish();
            }
        });
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int i) {
                /*MediaPlayer.TrackInfo trackInfoArray[] = mediaPlayer.getTrackInfo();
                int index;
                for(index=0;index<trackInfoArray.length;index++)
                {
                    if(trackInfoArray[index].getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO
                        && trackInfoArray[index].getLanguage().equals(Locale.getDefault().getISO3Language()))
                    {
                        mediaPlayer.selectTrack(index);
                    }
                }*/
                Log.println(Log.ASSERT,"Info",what+"\t"+i);
                switch(what)
                {
                    case MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING:
                        showDialogForIncompatibility();
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        if(findViewById(R.id.play_button).getVisibility()==View.VISIBLE&&videoView.isPlaying()) {
                            findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
                            findViewById(R.id.play_button).setVisibility(View.GONE);
                        }
                        findViewById(R.id.load).setVisibility(View.GONE);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        findViewById(R.id.load).setVisibility(View.VISIBLE);
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN :
                        Log.println(Log.ASSERT,"Error",i+"");
                        break;
                }
                return false;
            }
        });
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                //onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {}

            @Override
            public void onSessionEnding(CastSession session) {
                position = (int) session.getRemoteMediaClient().getMediaStatus().getStreamPosition();
                if(position>1000) {
                    if (complete) position = 0;
                    try {
                        String str = null;
                        File file = null;
                        FileOutputStream fileOutputStream = null;
                        if (!getIntent().hasExtra("name")) {
                            str = getIntent().getData().getPath();
                            str = str.substring(str.lastIndexOf('/') + 1).replace(' ', '_').replace(':', '_');
                            file = new File(getApplicationContext().getFilesDir(), str + ".txt");
                        } else
                            file = new File(getApplicationContext().getFilesDir(), getIntent().getStringExtra("name").replace(' ', '+') + ".txt");
                        if (!file.exists()) file.createNewFile();
                        if (!getIntent().hasExtra("name")) {
                            fileOutputStream = openFileOutput(str + ".txt", MODE_PRIVATE);
                        } else
                            fileOutputStream = openFileOutput(getIntent().getStringExtra("name").replace(' ', '+') + ".txt", MODE_PRIVATE);
                        fileOutputStream.write(String.valueOf(position + "\t" + video_length).getBytes());
                        if (stringList.size() > 1 && player != null) {
                            fileOutputStream.write(("\t" + (player.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) - 1) + "").getBytes());
                        }
                        fileOutputStream.write("\n".getBytes());
                        //Toast.makeText(getApplicationContext(),String.valueOf(position),Toast.LENGTH_SHORT).show();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadTimeDetails();
                        castContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener,CastSession.class);
                    }
                }, 2000);
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                loadRemoteMedia(videoView.getCurrentPosition(), true);
                startActivity(new Intent(getApplicationContext(),ExpandedControls.class));
                sendBroadcast(new Intent("CAST").putExtra("mode","start"));
                finish();
            }
             private void onApplicationDisconnected(){
                mCastSession = null;
                sendBroadcast(new Intent("CAST").putExtra("mode","end"));
             }
        };
        castContext.getSessionManager().addSessionManagerListener(mSessionManagerListener,CastSession.class);
    }

    private void showDialogForIncompatibility() {
        videoView.pause();
        if(!getIntent().hasExtra("link")){
            Toast.makeText(getApplicationContext(),"Not Compatible",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_download);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(getIntent().getStringExtra("link")));
                    String name = getIntent().getStringExtra("name");
                    request.setTitle(name);
                    request.setDescription("Entertainment");
                    request.allowScanningByMediaScanner();
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name.replace(':', '_').replace(' ', '_') + ".mp4");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                    dialog.dismiss();
                    finish();
                } catch (SecurityException exception) {
                    ActivityCompat.requestPermissions(VideoPlayerActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                }
            }
        });
        dialog.show();
    }

    @Override
    protected void onPause() {
        position=videoView.getCurrentPosition();
        seriesVideoPosition = position;
        if(position>1000)
        {
            if(complete) position=0;
            try {
                String str = null;
                File file=null;
                FileOutputStream fileOutputStream=null;
                if(!getIntent().hasExtra("name"))
                {
                    str = getIntent().getData().getPath();
                    str=str.substring(str.lastIndexOf('/')+1).replace(' ','_').replace(':','_');
                    file = new File(getApplicationContext().getFilesDir(),str+".txt");
                }
                else file = new File(this.getFilesDir(), getIntent().getStringExtra("name").replace(' ', '+') + ".txt");
                if (!file.exists()) file.createNewFile();
                if(!getIntent().hasExtra("name"))
                {
                    fileOutputStream = openFileOutput(str+".txt",MODE_PRIVATE);
                }
                else fileOutputStream = openFileOutput(getIntent().getStringExtra("name").replace(' ', '+') + ".txt", MODE_PRIVATE);
                fileOutputStream.write(String.valueOf(position+"\t"+videoView.getDuration()).getBytes());
                if(stringList.size()>1&&player!=null)
                {
                    fileOutputStream.write(("\t"+(player.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)-1)+"").getBytes());
                }
                fileOutputStream.write("\n".getBytes());
                //Toast.makeText(getApplicationContext(),String.valueOf(position),Toast.LENGTH_SHORT).show();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
        if(!Settings.System.canWrite(getApplicationContext())) return;
        }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        pip=true;
        if(!isInPictureInPictureMode&&videoView.getCurrentPosition()==0) finish();
        else if(!isInPictureInPictureMode)
        {
            pause_by_pip=false;
            videoView.start();
            toggleSubtitles(findViewById(R.id.subtitle_button));
            if(videoView.getMeasuredWidth()>videoView.getMeasuredHeight()) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            toggleSubtitles(findViewById(R.id.subtitle_button));
            findViewById(R.id.subtitle).setVisibility(View.GONE);
            keepTrackOfCurrentTimeState();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pip=false;
            }
        },1000);
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!Settings.System.canWrite(getApplicationContext())) {
                return;
            }
            if (!videoView.isPlaying()) findViewById(R.id.load).setVisibility(View.VISIBLE);
            videoView.start();
            if (!pip) {
                String str;
                if (getIntent().getType() == null)
                    str = getIntent().getStringExtra("name").replace(' ', '+') + ".txt";
                else
                    str = String.valueOf(getIntent().getData().getPath().substring(getIntent().getData().getPath().lastIndexOf('/') + 1))
                            .replace(' ', '_').replace(':', '_') + ".txt";
                try {
                    FileInputStream fis = openFileInput(str);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    str = br.readLine();
                    if (str.contains("\t")) str = str.substring(0, str.indexOf('\t'));
                    if (activity_start && getIntent().hasExtra("begin")) ;
                    else videoView.seekTo(Integer.parseInt(str));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            pip = false;
            activity_start = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            int width,height;
            width=videoView.getMeasuredWidth();
            height=videoView.getMeasuredHeight();
            if(width==0||height==0){
                width=height=9;
            }
            else if(width>2*height){
                width=18;
                height=9;
            }
            else if(height>2*width){
                height=18;
                width=9;
            }
            PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(width,height))
                    .setActions(createAction())
                    .build();
            listView.setVisibility(View.GONE);
            hideControls();
            enterPictureInPictureMode(pictureInPictureParams);
        }
        super.onUserLeaveHint();
    }


    @Override
    public void onBackPressed() {
        if(findViewById(R.id.list_of_audio_tracks).getVisibility()==View.VISIBLE)
        {
            findViewById(R.id.list_of_audio_tracks).setVisibility(View.GONE);
            videoView.start();
            return;
        }
        if(lock)
        {
            Toast.makeText(getApplicationContext(),"Video Player is locked",Toast.LENGTH_SHORT).show();
            return;
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) onUserLeaveHint();
        else
        {
            onPause();
            finish();
        }
    }
    private void uploadTimeDetails()
    {
        if(UserName.getUsername()==null&&UserName.getUsername(getApplicationContext())==null) return;
        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users")
                .document(UserName.getUsername(getApplicationContext()));
        try {
            FileInputStream fis = getApplicationContext().openFileInput("DownloadedVideos.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str,data_to_be_uploaded="";
            while((str=br.readLine())!=null)
            {
                File file = new File(getApplicationContext().getFilesDir(),str.replace(' ','+')+".txt");
                if(file.exists())
                {
                    str=str.replace(' ','+')+".txt";
                    FileInputStream fileInputStream = openFileInput(str);
                    BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(fileInputStream));
                    data_to_be_uploaded = data_to_be_uploaded + str + "\n" +bufferedReader.readLine() + "\n";
                }
            }
            documentReference.update("Watch Files",data_to_be_uploaded);
            data_to_be_uploaded="";
            FileInputStream inputHistory = openFileInput("History.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputHistory));
            String strTemp;
            while ((strTemp=bufferedReader.readLine())!=null)
            {
                data_to_be_uploaded = data_to_be_uploaded + strTemp + "\n";
            }
            documentReference.update("Watch History",data_to_be_uploaded);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int hideSystemBars()
    {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    }


    private boolean isFileDownloading()
    {
        try {
            FileInputStream fis = openFileInput("isDownloading.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            if(br.readLine().equals(getIntent().getStringExtra("name"))) return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(toggleReceiver);
            unregisterReceiver(forwardReceiver);
            unregisterReceiver(rewindReceiver);
            unregisterReceiver(toggleByMediaButtonReceiver);
            unregisterReceiver(playReceiver);
            unregisterReceiver(pauseReceiver);
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(phoneCallListener, PhoneStateListener.LISTEN_NONE);
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            ComponentName componentName = new ComponentName(getPackageName(), BluetoothPlayPauseReceiver.class.getName());
            audioManager.unregisterMediaButtonEventReceiver(componentName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if(listView.getVisibility()==View.VISIBLE)
        {
            listView.setVisibility(View.GONE);
            findViewById(R.id.play_button).callOnClick();
        }
        time=System.currentTimeMillis();
        final TextView textView = findViewById(R.id.video_title);
        if(textView.getVisibility()==View.GONE) {
            showControls();
            time_hide_show = System.currentTimeMillis();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (videoView.isPlaying()&&System.currentTimeMillis()>=time_hide_show+5000) {
                        hideControls();
                    }
                }
            }, 5000);
        }
        else hideControls();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if(lock) return false;
        if(videoView.isPlaying()) findViewById(R.id.pause_button).callOnClick();
        else
        {
            findViewById(R.id.play_button).callOnClick();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent eHorizontal, MotionEvent eVertical, float distanceX, float distanceY) {
        if(lock) return false;
        float x,y;
        x=distanceX;
        y=distanceY;
        if(distanceX<0) x=-1*distanceX;
        if(distanceY<0) y=-1*distanceY;
        if(y>2*x&&y>3&&eVertical.getY()>100)
        {
            ImageView imageView = findViewById(R.id.vol_bright_image);
            findViewById(R.id.vol_bright_info).setVisibility(View.VISIBLE);
            ProgressBar progressBar = findViewById(R.id.vol_bright_progress);
            vol_bright_time=System.currentTimeMillis();
            TextView textView = findViewById(R.id.vol_bright_text);
            if(eVertical.getX()<(float)videoView.getWidth()/2) {
                try {
                    brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    if(brightness>2047&&maxBrightness<4095) maxBrightness=4095;
                    else if(brightness>255&&maxBrightness<2047) maxBrightness=2047;
                    else if(maxBrightness<2047) maxBrightness=255;

                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                if(distanceY<0)
                {
                    brightness = brightness - maxBrightness/20;
                    if(brightness<0) brightness=0;
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.screenBrightness=lp.screenBrightness-0.01f;
                    if(lp.screenBrightness<0.01f) lp.screenBrightness=0.01f;
                    getWindow().setAttributes(lp);
                }
                else
                {
                    brightness = brightness + maxBrightness/20;
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.screenBrightness=lp.screenBrightness+0.01f;
                    if(lp.screenBrightness>1f) lp.screenBrightness=1f;
                    else if(lp.screenBrightness<0.01f) lp.screenBrightness=0.01f;
                    getWindow().setAttributes(lp);
                }
                try {
                    brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                progressBar.setProgress((int)(100*lp.screenBrightness));
                imageView.setImageResource(R.drawable.ic_baseline_brightness_mid_24);
                textView.setText((int)(100*lp.screenBrightness)+"%");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(System.currentTimeMillis()-vol_bright_time>=3000) findViewById(R.id.vol_bright_info).setVisibility(View.GONE);
                    }
                },3000);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.vol_bright_info).getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END,RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,RelativeLayout.TRUE);
                layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
                layoutParams.rightMargin=150;
                findViewById(R.id.vol_bright_info).setLayoutParams(layoutParams);
                return false;
            }
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int volume;
            if(distanceY>0)
            {
                if(maxVolume<25&&currentVolume<maxVolume) volume = currentVolume+1;
                else{
                    volume = currentVolume + ((4*maxVolume)/100);
                    if(volume>maxVolume) volume = maxVolume;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_PLAY_SOUND);
            }
            else {
                if(maxVolume<25&&currentVolume>0) volume = currentVolume-1;
                else{
                    volume = currentVolume - ((4*maxVolume)/100);
                    if(volume<0) volume = 0;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
            }
            int percent = 100*currentVolume/maxVolume;
            if(percent>50) imageView.setImageResource(R.drawable.ic_baseline_volume_up_24);
            else if(percent>0) imageView.setImageResource(R.drawable.ic_baseline_volume_down_24);
            else imageView.setImageResource(R.drawable.ic_baseline_volume_mute_24);
            progressBar.setProgress(percent);
            String text = percent+"%";
            textView.setText(text);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.vol_bright_info).getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START,RelativeLayout.TRUE);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,RelativeLayout.TRUE);
            layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
            layoutParams.leftMargin=150;
            findViewById(R.id.vol_bright_info).setLayoutParams(layoutParams);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(System.currentTimeMillis()-vol_bright_time>=3000) findViewById(R.id.vol_bright_info).setVisibility(View.GONE);
                }
            },3000);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        ImageView imageView = findViewById(R.id.vol_bright_image);
        ProgressBar progressBar = findViewById(R.id.vol_bright_progress);
        vol_bright_time=System.currentTimeMillis();
        TextView textView = findViewById(R.id.vol_bright_text);
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume;
        switch (event.getKeyCode())
        {
            case KeyEvent.KEYCODE_VOLUME_UP:{
                if(maxVolume<25&&currentVolume<maxVolume) volume = currentVolume+1;
                else{
                    volume = currentVolume + ((4*maxVolume)/100);
                    if(volume>maxVolume) volume = maxVolume;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_PLAY_SOUND);
                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN:{
                if(maxVolume<25&&currentVolume>0) volume = currentVolume-1;
                else{
                    volume = currentVolume - ((4*maxVolume)/100);
                    if(volume<0) volume = 0;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
                break;
            }
            default:
            {
                pip=false;
                return super.dispatchKeyEvent(event);
            }
        }
        findViewById(R.id.vol_bright_info).setVisibility(View.VISIBLE);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int percent = 100*currentVolume/maxVolume;
        if(percent>50) imageView.setImageResource(R.drawable.ic_baseline_volume_up_24);
        else if(percent>0) imageView.setImageResource(R.drawable.ic_baseline_volume_down_24);
        else imageView.setImageResource(R.drawable.ic_baseline_volume_mute_24);
        progressBar.setProgress(percent);
        String text = percent+"%";
        textView.setText(text);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.vol_bright_info).getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START,RelativeLayout.TRUE);
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,RelativeLayout.TRUE);
        layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.leftMargin=150;
        findViewById(R.id.vol_bright_info).setLayoutParams(layoutParams);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(System.currentTimeMillis()-vol_bright_time>=3000) findViewById(R.id.vol_bright_info).setVisibility(View.GONE);
            }
        },3000);
        return true;
    }
    private void keepTrackOfCurrentTimeState()
    {
        position=videoView.getCurrentPosition();
        if(position>1000)
        {
            if(complete) position=0;
            try {
                String str = null;
                File file=null;
                FileOutputStream fileOutputStream=null;
                if(!getIntent().hasExtra("name"))
                {
                    str = getIntent().getData().getPath();
                    str=str.substring(str.lastIndexOf('/')+1).replace(' ','_').replace(':','_');
                    file = new File(getApplicationContext().getFilesDir(),str+".txt");
                }
                else file = new File(this.getFilesDir(), getIntent().getStringExtra("name").replace(' ', '+') + ".txt");
                if (!file.exists()) file.createNewFile();
                if(!getIntent().hasExtra("name"))
                {
                    fileOutputStream = openFileOutput(str+".txt",MODE_PRIVATE);
                }
                else fileOutputStream = openFileOutput(getIntent().getStringExtra("name").replace(' ', '+') + ".txt", MODE_PRIVATE);
                fileOutputStream.write(String.valueOf(position+"\t"+videoView.getDuration()+"\t").getBytes());
                try{
                    if(stringList.size()>1&&player!=null)
                    {
                        fileOutputStream.write((player.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO)-1+"\n").getBytes());
                    }
                    else fileOutputStream.write("\n".getBytes());
                } catch (IOException e) {
                    fileOutputStream.write("\n".getBytes());
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(getApplicationContext(),String.valuecOf(position),Toast.LENGTH_SHORT).show();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(isInPictureInPictureMode())
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    keepTrackOfCurrentTimeState();
                }
            },5000);
        }
    }

    @Override
    protected void onStop() {
        if(!goingForPermission) {
            if(getIntent().hasExtra("dbName")){
                if(!complete&&seriesVideoPosition==0) {
                    super.onStop();
                    return;
                }
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
                    fos.write((season+"\t"+episode+"\t"+seriesVideoPosition+"\n").getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(isSeriesComplete){
                String fileName = getIntent().getStringExtra("raw_name").substring(0,getIntent().getStringExtra("raw_name").lastIndexOf(':')-1);
                fileName = fileName.replace(' ','+')+".txt";
                new File(getFilesDir(),fileName).delete();
            }
        }
        else {
            super.onStop();
            return;
        }
        if(getIntent().hasExtra("online"))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadTimeDetails();;
                }
            }).start();
        super.onStop();
    }
    public void toggleSubtitles(View view){
        ImageView imageView = (ImageView)view;
        if(findViewById(R.id.subtitle).getVisibility()==View.VISIBLE){
            findViewById(R.id.subtitle).setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.ic_baseline_closed_caption_disabled_24);
        }
        else{
            findViewById(R.id.subtitle).setVisibility(View.VISIBLE);
            imageView.setImageResource(R.drawable.ic_baseline_closed_caption_24);
        }
    }
    private void showControls(){
        findViewById(R.id.video_title).setVisibility(View.VISIBLE);
        if(getIntent().hasExtra("online")) findViewById(R.id.view_count).setVisibility(View.VISIBLE);
        findViewById(R.id.lock).setVisibility(View.VISIBLE);
        findViewById(R.id.time_track).setVisibility(View.VISIBLE);
        findViewById(R.id.blackish).setVisibility(View.VISIBLE);
        findViewById(R.id.blackish).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in));
        Animation animationUp = AnimationUtils.loadAnimation(this,R.anim.appear_up);
        Animation animationDown = AnimationUtils.loadAnimation(this,R.anim.appear_down);
        startTimeCounting();
        findViewById(R.id.video_title).startAnimation(animationDown);
        findViewById(R.id.view_count).startAnimation(animationDown);
        findViewById(R.id.lock).startAnimation(animationUp);
        if(!lock)
        {
            if(isThereSubtitle) findViewById(R.id.subtitle_button).setVisibility(View.VISIBLE);
            findViewById(R.id.time_track).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            findViewById(R.id.time_track_center).setVisibility(View.GONE);
            findViewById(R.id.close).setVisibility(View.VISIBLE);
            if(getIntent().hasExtra("link")&&videoView.getDuration()>0) {
                findViewById(R.id.media_route_button).setVisibility(View.VISIBLE);
                findViewById(R.id.media_route_button).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            }
            findViewById(R.id.close).startAnimation(animationUp);
            findViewById(R.id.forward).setVisibility(View.VISIBLE);
            findViewById(R.id.forward).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            findViewById(R.id.rewind).setVisibility(View.VISIBLE);
            findViewById(R.id.rewind).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            findViewById(R.id.seekbar).setVisibility(View.VISIBLE);
            findViewById(R.id.seekbar).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            findViewById(R.id.track_select).setVisibility(View.VISIBLE);
            findViewById(R.id.track_select).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            findViewById(R.id.rotate).setVisibility(View.VISIBLE);
            findViewById(R.id.rotate).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            findViewById(R.id.buffered_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.buffered_progress).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                findViewById(R.id.minimize).setVisibility(View.VISIBLE);
                findViewById(R.id.minimize).startAnimation(animationUp);
            }
            if(videoView.isPlaying())
            {
                findViewById(R.id.pause_button).setVisibility(View.VISIBLE);
                findViewById(R.id.pause_button).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            }
            else
            {
                findViewById(R.id.play_button).setVisibility(View.VISIBLE);
                findViewById(R.id.play_button).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_in));
            }
            if(isThereAPreviousEpisode){
                findViewById(R.id.previous_episode).setVisibility(View.VISIBLE);
            }
            if(isThereANextEpisode){
                findViewById(R.id.next_episode).setVisibility(View.VISIBLE);
            }
        }
        else{
            findViewById(R.id.time_track_center).setVisibility(View.VISIBLE);
            findViewById(R.id.time_track).setVisibility(View.GONE);
        }

    }
    private void hideControls(){
        boolean animate = false;
        if(findViewById(R.id.lock).getVisibility()==View.VISIBLE) animate = true;
        Animation animationUp = AnimationUtils.loadAnimation(this,R.anim.disappear_up);
        Animation animationDown = AnimationUtils.loadAnimation(this,R.anim.disappear_down);
        findViewById(R.id.video_title).setVisibility(View.GONE);
        findViewById(R.id.media_route_button).setVisibility(View.GONE);
        findViewById(R.id.view_count).setVisibility(View.GONE);
        findViewById(R.id.minimize).setVisibility(View.GONE);
        findViewById(R.id.close).setVisibility(View.GONE);
        findViewById(R.id.lock).setVisibility(View.GONE);
        findViewById(R.id.play_button).setVisibility(View.GONE);
        findViewById(R.id.pause_button).setVisibility(View.GONE);
        findViewById(R.id.forward).setVisibility(View.GONE);
        findViewById(R.id.rewind).setVisibility(View.GONE);
        findViewById(R.id.time_track).setVisibility(View.GONE);
        findViewById(R.id.seekbar).setVisibility(View.GONE);
        findViewById(R.id.buffered_progress).setVisibility(View.GONE);
        findViewById(R.id.rotate).setVisibility(View.GONE);
        findViewById(R.id.track_select).setVisibility(View.GONE);
        findViewById(R.id.blackish).setVisibility(View.GONE);
        findViewById(R.id.previous_episode).setVisibility(View.GONE);
        findViewById(R.id.next_episode).setVisibility(View.GONE);
        findViewById(R.id.subtitle_button).setVisibility(View.GONE);
        findViewById(R.id.time_track_center).setVisibility(View.GONE);
        if (animate) {
            findViewById(R.id.lock).startAnimation(animationDown);
            findViewById(R.id.video_title).startAnimation(animationUp);
            findViewById(R.id.view_count).startAnimation(animationUp);
            findViewById(R.id.blackish).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
            if (!lock) {
                findViewById(R.id.close).startAnimation(animationDown);
                findViewById(R.id.forward).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_out));
                if(videoView.isPlaying()) findViewById(R.id.pause_button).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_out));
                else findViewById(R.id.play_button).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_out));
                findViewById(R.id.rewind).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fade_out));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    findViewById(R.id.minimize).startAnimation(animationDown);
            }
        }
    }
    private void checkPreviousAndNextEpisodes(){
        if(!getIntent().hasExtra("dbName")){
            isThereANextEpisode = false;
            isThereAPreviousEpisode = false;
            return;
        }
        final int season = Integer.parseInt(getIntent().getStringExtra("dbName").substring(getIntent().getStringExtra("dbName").lastIndexOf('s')+1));
        final int number = Integer.parseInt(getIntent().getStringExtra("episode_number"));
        currentSeason = season;
        currentEpisode = number;
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(getIntent().getStringExtra("dbName")+"e"+(number+1)).getValue()!=null) isThereANextEpisode = true;
                else {
                    isThereANextEpisode = false;
                    int number = 1;
                    int season = currentSeason + 1;
                    String dbName = getIntent().getStringExtra("dbName");
                    dbName = dbName.substring(0,dbName.lastIndexOf('s')+1);
                    dbName = dbName+season;
                    if(snapshot.child(dbName+"e"+number).getValue()!=null) isThereANextEpisode = true;
                    else isThereANextEpisode = false;
                }
                String name = getIntent().getStringExtra("dbName");
                name = name.substring(0,name.lastIndexOf('s')+1);
                if(season==1&&number==1) isThereAPreviousEpisode = false;
                else isThereAPreviousEpisode = true;
             }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void goToNextEpisode(){
        goToNext = true;
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!goToNext) return;
                try{
                    String link = "";
                    int number = Integer.parseInt(getIntent().getStringExtra("episode_number"));
                    try {
                        link = snapshot.child(getIntent().getStringExtra("dbName") + "e" + (number + 1)).getValue().toString();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        try {
                            number = 1;
                            int season = Integer.parseInt(getIntent().getStringExtra("dbName").substring(getIntent().getStringExtra("dbName").lastIndexOf('s')+1));
                            season = season + 1;
                            String dbName = getIntent().getStringExtra("dbName");
                            dbName = dbName.substring(0,dbName.lastIndexOf('s')+1);
                            dbName = dbName+season;
                            link = snapshot.child(dbName+"e"+number).getValue().toString();
                            String rawName = getIntent().getStringExtra("raw_name");
                            rawName = rawName.substring(0,rawName.lastIndexOf(':')+1);
                            rawName = rawName+" S"+season+"E";
                            String name = getIntent().getStringExtra("raw_name");
                            Intent intent;
                            if(!complete) intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                            else intent = new Intent(getApplicationContext(),NextVideoActivity.class);
                            intent.putExtra("name",rawName+number);
                            intent.putExtra("image",getIntent().getStringExtra("image"));
                            intent.putExtra("raw_name",rawName);
                            intent.putExtra("online",true);
                            intent.putExtra("dbName",dbName);
                            intent.putExtra("episode_number",number+"");
                            intent.putExtra("link",link);
                            finish();
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Series Completed",Toast.LENGTH_SHORT).show();
                            String fileName = getIntent().getStringExtra("raw_name").substring(0,getIntent().getStringExtra("raw_name").lastIndexOf(':')-1);
                            fileName = fileName.replace(' ','+')+".txt";
                            new File(getApplicationContext().getFilesDir(),fileName).delete();
                            finish();
                            isSeriesComplete = true;
                            return;
                        }
                    }
                    String name = getIntent().getStringExtra("raw_name");
                    Intent intent;
                    if(!complete) intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                    else intent = new Intent(getApplicationContext(),NextVideoActivity.class);
                    intent.putExtra("name",name+(number+1));
                    intent.putExtra("image",getIntent().getStringExtra("image"));
                    intent.putExtra("raw_name",name);
                    intent.putExtra("online",true);
                    intent.putExtra("dbName",getIntent().getStringExtra("dbName"));
                    intent.putExtra("episode_number",(number+1)+"");
                    intent.putExtra("link",link);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                } catch (Exception exception) {
                    finish();
                    Toast.makeText(getApplicationContext(),"Series Completed",Toast.LENGTH_SHORT).show();
                    String fileName = getIntent().getStringExtra("raw_name").substring(0,getIntent().getStringExtra("raw_name").lastIndexOf(':')-1);
                    fileName = fileName.replace(' ','+')+".txt";
                    new File(getApplicationContext().getFilesDir(),fileName).delete();
                    isSeriesComplete = true;
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void goToPreviousEpisode(){
        goToPrevious = true;

        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!goToPrevious) return;
                try{
                    String link = "";
                    int number = Integer.parseInt(getIntent().getStringExtra("episode_number"));
                    try {
                        link = snapshot.child(getIntent().getStringExtra("dbName") + "e" + (number - 1)).getValue().toString();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        try {
                            int season = Integer.parseInt(getIntent().getStringExtra("dbName").substring(getIntent().getStringExtra("dbName").lastIndexOf('s')+1));
                            season = season - 1;
                            String dbName = getIntent().getStringExtra("dbName");
                            dbName = dbName.substring(0,dbName.lastIndexOf('s')+1);
                            dbName = dbName+season;
                            number = Integer.parseInt(snapshot.child(dbName).getValue().toString());
                            link = snapshot.child(dbName+"e"+number).getValue().toString();
                            String rawName = getIntent().getStringExtra("raw_name");
                            rawName = rawName.substring(0,rawName.lastIndexOf(':')+1);
                            rawName = rawName+" S"+season+"E";
                            String name = getIntent().getStringExtra("raw_name");
                            Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                            intent.putExtra("name",rawName+number);
                            intent.putExtra("raw_name",rawName);
                            intent.putExtra("online",true);
                            intent.putExtra("dbName",dbName);
                            intent.putExtra("episode_number",number+"");
                            intent.putExtra("link",link);
                            finish();
                            startActivity(intent);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Series Completed",Toast.LENGTH_SHORT).show();
                            String fileName = getIntent().getStringExtra("raw_name").substring(0,getIntent().getStringExtra("raw_name").lastIndexOf(':')-1);
                            fileName = fileName.replace(' ','+')+".txt";
                            new File(getApplicationContext().getFilesDir(),fileName).delete();
                            finish();
                            isSeriesComplete = true;
                            return;
                        }
                    }
                    String name = getIntent().getStringExtra("raw_name");
                    Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                    intent.putExtra("name",name+(number-1));
                    intent.putExtra("raw_name",name);
                    intent.putExtra("online",true);
                    intent.putExtra("dbName",getIntent().getStringExtra("dbName"));
                    intent.putExtra("episode_number",(number-1)+"");
                    intent.putExtra("link",link);
                    finish();
                    startActivity(intent);
                } catch (Exception exception) {
                    finish();
                    Toast.makeText(getApplicationContext(),"Series Completed",Toast.LENGTH_SHORT).show();
                    String fileName = getIntent().getStringExtra("raw_name").substring(0,getIntent().getStringExtra("raw_name").lastIndexOf(':')-1);
                    fileName = fileName.replace(' ','+')+".txt";
                    new File(getApplicationContext().getFilesDir(),fileName).delete();
                    isSeriesComplete = true;
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_button, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }
    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, getIntent().getStringExtra("description"));
        if(getIntent().hasExtra("image")) movieMetadata.addImage(new WebImage(Uri.parse(getIntent().getStringExtra("image"))));
        movieMetadata.putString(MediaMetadata.KEY_TITLE, getIntent().getStringExtra("name"));
        MediaInfo.Builder builder = new MediaInfo.Builder(getIntent().getStringExtra("link"))
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/*")
                .setMetadata(movieMetadata)
                .setStreamDuration(videoView.getDuration());
        if(subtitleLink!=null){
            List<MediaTrack> tracks = new ArrayList<>();
            MediaTrack subtitle = new MediaTrack.Builder(9,MediaTrack.TYPE_TEXT)
                    .setName("English")
                    .setContentId(subtitleLink)
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentType("text/vtt")
                    .setLanguage("en-US")
                    .build();
            tracks.add(subtitle);
            builder.setMediaTracks(tracks);
        }
        return builder.build();
    }
    private void loadRemoteMedia(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                .setAutoplay(autoPlay)
                .setCurrentTime(position)
                .build());
        remoteMediaClient.setActiveMediaTracks(new long[]{9});
        remoteMediaClient.setTextTrackStyle(TextTrackStyle.fromSystemSettings(this));
        Log.println(Log.ASSERT,"Load","true");
    }
}