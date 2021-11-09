package com.devansh.entertainment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private ArrayList<NotificationData> notificationDataArrayList;
    private Context context;
    public NotificationAdapter(ArrayList<NotificationData> notificationDataArrayList) {
        this.notificationDataArrayList = notificationDataArrayList;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setNotificationDataArrayList(ArrayList<NotificationData> notificationDataArrayList) {
        this.notificationDataArrayList = notificationDataArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.notification_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationData notificationData = notificationDataArrayList.get(position);
        holder.text.setText(notificationData.getText());
        holder.title.setText(notificationData.getTitle());
        holder.date.setText(notificationData.getTime());
        if(notificationData.getImage()!=null||(notificationData.getImage().length()>0&&!notificationData.getImage().equals("null")))
            try{
                Picasso.get().load(notificationData.getImage()).into(holder.imageView);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setVisibility(View.GONE);
            }
        else holder.imageView.setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,FirebaseNotificationActivity.class);
                intent.putExtra("time",notificationData.getId());
                intent.putExtra("token",notificationData.getToken());
                intent.putExtra("title",notificationData.getTitle());
                intent.putExtra("text",notificationData.getText());
                intent.putExtra("image",notificationData.getImage());
                intent.putExtra("data",notificationData.getExtras());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.itemView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FirebaseDatabase.getInstance().getReference("scheduled_notifications").child(notificationData.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference("notifications").child(notificationData.getId()).removeValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title,text,date;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
            date = itemView.findViewById(R.id.time);
        }
    }
}
