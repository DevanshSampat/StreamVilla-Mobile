package com.devansh.entertainment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListDisplayActivity extends AppCompatActivity {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String reference = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_display);
        if(new Theme(this).isInDarkMode()) findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        ((TextView)findViewById(R.id.tv_header)).setText(getIntent().getStringExtra("name"));
        switch (getIntent().getStringExtra("name")){
            case "You may like":{
                reference = "recommendations/"+ FirebaseAuth.getInstance().getCurrentUser().getUid();
                break;
            }
            case "Trending":{
                reference = "weekly_trending";
                break;
            }
        }
        RecyclerView recyclerView = findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ContentAdapter(new ContentData[0],this,new Theme(this).isInDarkMode()));
        FirebaseDatabase.getInstance().getReference(reference).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> arrayList = (ArrayList<String>) snapshot.getValue();
                if(arrayList==null) return;
                FirebaseDatabase.getInstance().getReference("content").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ContentData[] contentData = new ContentData[arrayList.size()];
                        for(int i=0;i<contentData.length;i++) contentData[i] = Utility.getContentData(ListDisplayActivity.this,snapshot,arrayList.get(i));
                        ((ContentAdapter)recyclerView.getAdapter()).setContentData(contentData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}