package com.example.news;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {
    private String[] links;
    private String[] subtitles;
    private String name;
    private int count;
    private Context context;
    private String imageURL;
    private String dbName;
    private boolean dark;
    public EpisodeAdapter(Context context, String name, String imageURL, String[] links,String[] subtitles, String dbName, int count){
        this.count = count;
        this.links = links;
        this.name = name;
        this.dbName = dbName;
        this.context = context;
        this.imageURL = imageURL;
        this.subtitles = subtitles;
        this.dark = new Theme(context).isInDarkMode();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        view = layoutInflater.inflate(R.layout.season_episode_item,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if(dark){
            holder.layout.setBackgroundColor(Color.BLACK);
            holder.textView.setTextColor(Color.WHITE);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        }
        holder.textView.setText("Episode "+(position+1));
        Picasso.with(context).load(imageURL).into(holder.imageView);
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context,R.anim.zoom_in_recycle));
        holder.setIsRecyclable(false);
        String seasonNumber = dbName.substring(dbName.lastIndexOf('s')+1);
        if(new File(context.getExternalFilesDir(null),(name + "E" + (position+1))
                .replace(':', '_').replace(' ', '_')+".mp4").exists()){
            ((ImageView)holder.itemView.findViewById(R.id.download_delete)).setImageResource(R.drawable.ic_baseline_delete_24);
        }
        holder.itemView.findViewById(R.id.download_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(new File(context.getExternalFilesDir(null),(name + "E" + (position+1))
                        .replace(':', '_').replace(' ', '_')+".mp4").exists()){
                    Dialog dialog = new Dialog(context);
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
                    textView.setText("Do you want to delete "+name+"E"+(position+1)+"?");
                    if(new Theme(context).isInDarkMode()) {
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
                            Uri updateFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",
                                    new File(context.getExternalFilesDir(null),
                                            (name + "E" + (position+1))
                                            .replace(':', '_').replace(' ', '_')+".mp4"));
                            context.getContentResolver().delete(updateFileUri, null, null);
                            dialog.dismiss();
                            Toast.makeText(context, "Deleted "+name + "E" + (position+1), Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }
                    });
                    dialog.show();
                }
                else{
                    new DownloadNotification().enqueueDownload(context,name + "E" + (position+1),links[position]);
                    String temp = dbName;
                    if(temp.contains("s")) temp = temp.substring(0,temp.indexOf('s'));
                    new WebSeriesDownloader().downloadThumbnail(context,temp);
                    if(!new File(context.getFilesDir(),"isDownloading.txt").exists())
                        new DownloadNotification().goToNextTask(context,new Theme(context).isInDarkMode());
                    try{
                        String replacedName = (name+"E"+(position+1)).replace(':','_').replace(' ','_');
                        if(!new File(context.getExternalFilesDir(null), replacedName +".srt").exists()) {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(subtitles[position]));
                            request.setTitle(name + " : S" + seasonNumber + "E" + (position+1));
                            request.setDescription("subtitles");
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                            request.setVisibleInDownloadsUi(false);
                            request.setDestinationInExternalFilesDir(context, null, replacedName + ".srt");
                            DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,VideoPlayerActivity.class);
                intent.putExtra("name",name+"E"+(position+1));
                intent.putExtra("image",imageURL);
                intent.putExtra("raw_name",name+"E");
                intent.putExtra("online",true);
                intent.putExtra("dbName",dbName);
                intent.putExtra("episode_number",(position+1)+"");
                intent.putExtra("link",links[position]);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView textView;
        ImageView imageView;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            textView = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.image);
            cardView = itemView.findViewById(R.id.card);
        }
    }
}
