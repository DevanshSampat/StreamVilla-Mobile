package com.example.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DeleteVideoAdapter extends RecyclerView.Adapter<DeleteVideoAdapter.ViewHolder> {
    private ArrayList<File> fileArrayList;
    private Context context;
    private boolean[] delete;
    public DeleteVideoAdapter(ArrayList<File> fileArrayList) {
        this.fileArrayList = fileArrayList;
        delete = new boolean[fileArrayList.size()];
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_video_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteVideoAdapter.ViewHolder holder, int position) {
        ((TextView)holder.itemView.findViewById(R.id.text)).setText(
                fileArrayList.get(position).getName().substring(0,fileArrayList.get(position).getName().length()-".mp4".length())
                .replace("___"," : ").replace('_',' ')
        );
        if(new Theme(context).isInDarkMode()) {
            ((TextView)holder.itemView.findViewById(R.id.text)).setTextColor(Color.WHITE);
            ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setBackgroundColor(Color.WHITE);
        }
        ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                delete[position] = check;
            }
        });
        ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setChecked(delete[position]);
    }

    public void setFileArrayList(ArrayList<File> fileArrayList) {
        this.fileArrayList = fileArrayList;
    }
    public void setDeleteAll(boolean delete){
        for(int i=0;i<this.delete.length;i++)
        {
            this.delete[i] = delete;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }

    public void deleteFiles() {
        for(int i=0;i<delete.length;i++){
            if(delete[i]){
                File file = fileArrayList.get(i);
                Uri uri = FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID+".provider",file);
                context.getContentResolver().delete(uri,null,null);
            }
        }
        context.sendBroadcast(new Intent("FILES_DELETED"));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
