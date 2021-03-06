package com.devansh.entertainment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.ViewHolder> {
    private String dbName;
    private String name;
    private int count;
    private Context context;
    private String imageURL;
    private boolean dark;
    private String[] size;
    public SeasonAdapter(Context context, String name, String imageURL, String dbName, int count){
        this.count = count;
        this.dbName = dbName;
        this.name = name;
        this.context = context;
        this.imageURL = imageURL;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if(dark){
            holder.layout.setBackgroundColor(Color.BLACK);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#363636"));
            holder.sizeOfContent.setTextColor(Color.WHITE);
            holder.textView.setTextColor(Color.WHITE);
        }
        holder.setIsRecyclable(false);
        holder.textView.setText("Season "+(position+1));
        try{
            holder.sizeOfContent.setText(size[position]);
        } catch (Exception e) {
            holder.sizeOfContent.setText("");
        }
        Picasso.get().load(imageURL).into(holder.imageView);
        holder.itemView.findViewById(R.id.download_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WebSeriesDownloader().downloadSeason(context,dbName+"s"+(position+1));
                Toast.makeText(context, "Downloading "+name+" : Season "+(position+1), Toast.LENGTH_SHORT).show();
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,EpisodePickerActivity.class);
                intent.putExtra("name",name+" : S"+(position+1));
                intent.putExtra("dbName",dbName+"s"+(position+1));
                intent.putExtra("image",imageURL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setSize(String[] size) {
        this.size = size;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView textView;
        ImageView imageView;
        CardView cardView;
        TextView sizeOfContent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            textView = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.image);
            cardView = itemView.findViewById(R.id.card);
            sizeOfContent = itemView.findViewById(R.id.size);
        }
    }
}
