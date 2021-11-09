package com.devansh.entertainment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class DeleteVideoAdapter extends RecyclerView.Adapter<DeleteVideoAdapter.ViewHolder> {
    private ArrayList<File> fileArrayList;
    private Context context;

    public boolean[] getDelete() {
        return delete;
    }

    private boolean[] delete;
    private long size;
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
    public void onBindViewHolder(@NonNull DeleteVideoAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String name = fileArrayList.get(position).getName().substring(0,fileArrayList.get(position).getName().length()-".mp4".length())
                .replace("___"," : ").replace('_',' ');
        ((TextView)holder.itemView.findViewById(R.id.text)).setText(name);
        if(new Theme(context).isInDarkMode()) {
            ((TextView)holder.itemView.findViewById(R.id.text)).setTextColor(Color.WHITE);
            ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setBackgroundColor(Color.WHITE);
        }
        String path = "/storage/emulated/0/android/data/com.devansh.entertainment/files/";
        if(new File(path+name.replace(' ','_').replace(':','_')+".jpg").exists())
            ((ImageView)holder.itemView.findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeFile(path+name.replace(' ','_').replace(':','_')+".jpg"));
        else
            try{
                ((ImageView)holder.itemView.findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeFile(path+"image_for_"+
                        name.substring(0,name.lastIndexOf(':')-1).replace(' ','_').replace(':','_')+".jpg"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                delete[position] = check;
                size = 0;
                boolean checkAll = true;
                for(int i=0;i<fileArrayList.size();i++){
                    if(delete[i]) {
                        size = size + fileArrayList.get(i).length();
                    }
                    else checkAll = false;
                }
                Intent intent = new Intent("DELETE_VIDEO_SIZE");
                intent.putExtra("size",size+"");
                context.sendBroadcast(intent);
                context.sendBroadcast(new Intent("SELECT_ALL").putExtra("all",checkAll+""));
            }
        });
        ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setChecked(delete[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CheckBox)holder.itemView.findViewById(R.id.checkbox)).setChecked(!((CheckBox)holder.itemView.findViewById(R.id.checkbox)).isChecked());
            }
        });
    }

    public void setFileArrayList(ArrayList<File> fileArrayList) {
        this.fileArrayList = fileArrayList;
        delete = new boolean[fileArrayList.size()];
        setDeleteAll(false);
        notifyDataSetChanged();
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
