package com.example.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
    private BroadcastReceiver deletedFileReceiver;
    private BroadcastReceiver sizeReceiver;
    private BroadcastReceiver checkAllReceiver;
    private boolean checkAll;
    private Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_multiple);
        deletedFileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getAllVideos();
            }
        };
        checkAllReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAll = true;
                ((CheckBox)findViewById(R.id.select_all)).setChecked(intent.getStringExtra("all").equals("true"));
                checkAll = false;
            }
        };
        sizeReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {
                double size = (double) Long.parseLong(intent.getStringExtra("size"));
                if(size==0){
                    ((TextView)findViewById(R.id.delete_text)).setText("Select items to delete");
                    return;
                }
                size = size/(1024*1024);
                String unit = "MB";
                if(size>1024){
                    size = size/1024;
                    unit = "GB";
                }
                String tempString = size+"";
                try{
                    tempString = tempString.substring(0,tempString.indexOf('.')+3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ((TextView)findViewById(R.id.delete_text)).setText("Free up "+tempString+" "+unit);
            }
        };
        registerReceiver(checkAllReceiver,new IntentFilter("SELECT_ALL"));
        registerReceiver(sizeReceiver,new IntentFilter("DELETE_VIDEO_SIZE"));
        registerReceiver(deletedFileReceiver,new IntentFilter("FILES_DELETED"));
        fileArrayList = new ArrayList<>();
        if(new Theme(this).isInDarkMode()) {
            findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
            findViewById(R.id.select_all).setBackgroundColor(Color.WHITE);
            ((TextView)findViewById(R.id.text_select)).setTextColor(Color.WHITE);
        }
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        getAllVideos();
        findViewById(R.id.text_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CheckBox)findViewById(R.id.select_all)).setChecked(!((CheckBox)findViewById(R.id.select_all)).isChecked());
            }
        });
        ((CheckBox)findViewById(R.id.select_all)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    if(checkAll){
                        checkAll = false;
                        return;
                    }
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

    @SuppressLint("SetTextI18n")
    public void delete(View view) {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.delete_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setDimAmount(0.80f);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.y = 100;
        lp.verticalMargin = 5;
        lp.horizontalMargin = 5;
        dialog.getWindow().setAttributes(lp);
        dialog.setCancelable(true);
        TextView textView = dialog.findViewById(R.id.text_warning);
        int count = 0;
        for(boolean bool : ((DeleteVideoAdapter)recyclerView.getAdapter()).getDelete()) if(bool) count++;
        if(count==0) return;
        textView.setText("Do you want to delete "+count+" video"+(count==1?"":"s")+"?");
        if(new Theme(this).isInDarkMode()) {
            textView = dialog.findViewById(R.id.text_warning);
            CardView cardView = dialog.findViewById(R.id.delete_card);
            textView.setTextColor(Color.parseColor("#CCCCCC"));
            cardView.setCardBackgroundColor(Color.parseColor("#222222"));
            textView = dialog.findViewById(R.id.text_keep);
            cardView = dialog.findViewById(R.id.keep);
            textView.setTextColor(Color.parseColor("#CCCCCC"));
            cardView.setCardBackgroundColor(Color.parseColor("#393939"));
            cardView = dialog.findViewById(R.id.card);
            cardView.setCardBackgroundColor(Color.parseColor("#393939"));
        }
        dialog.findViewById(R.id.keep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.delete_movie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    dialog.dismiss();
                    ((DeleteVideoAdapter)recyclerView.getAdapter()).deleteFiles();
                    Toast.makeText(DeleteMultipleVideosActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    ((TextView)findViewById(R.id.delete_text)).setText("Select items to delete");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }
}