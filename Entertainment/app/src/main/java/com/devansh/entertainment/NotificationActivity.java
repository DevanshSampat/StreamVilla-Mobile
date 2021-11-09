package com.devansh.entertainment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
        recyclerView.setHasFixedSize(true);
        FirebaseDatabase.getInstance().getReference("scheduled_notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<NotificationData> notificationDataArrayList = new ArrayList<>();
                for(DataSnapshot data : snapshot.getChildren()) {
                    try {
                            NotificationData notificationData = new NotificationData();
                            notificationData.setTitle(data.child("title").getValue().toString());
                            notificationData.setText(data.child("text").getValue().toString());
                            notificationData.setToken(data.child("token").getValue().toString());
                            notificationData.setExtras(data.child("extras").getValue().toString());
                            try{
                                notificationData.setImage(data.child("image").getValue().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            notificationData.setId(data.getKey());
                            String str = "";
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(Long.parseLong(data.getKey()));
                            str = str + calendar.get(Calendar.DAY_OF_MONTH) + "/";
                            str = str + (calendar.get(Calendar.MONTH)+1) + "/";
                            str = str + calendar.get(Calendar.YEAR) + " at ";
                            if(calendar.get(Calendar.HOUR_OF_DAY)<10) str = str+"0";
                            str = str + calendar.get(Calendar.HOUR_OF_DAY)+":";
                            if(calendar.get(Calendar.MINUTE)<10) str = str+"0";
                            str = str + calendar.get(Calendar.MINUTE);
                            notificationData.setTime(str);
                            notificationDataArrayList.add(notificationData);
                     } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                NotificationData[] notificationData = new NotificationData[notificationDataArrayList.size()];
                for(int i=0;i<notificationDataArrayList.size();i++) notificationData[i] = notificationDataArrayList.get(notificationDataArrayList.size()-1-i);
                notificationDataArrayList.clear();
                notificationDataArrayList.addAll(Arrays.asList(notificationData));
                if(recyclerView.getAdapter()==null) recyclerView.setAdapter(new NotificationAdapter(notificationDataArrayList));
                else ((NotificationAdapter)recyclerView.getAdapter()).setNotificationDataArrayList(notificationDataArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}