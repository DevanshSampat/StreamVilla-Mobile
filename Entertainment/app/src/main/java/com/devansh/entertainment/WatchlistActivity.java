package com.devansh.entertainment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

public class WatchlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<String> watchList;
    private ContentData[] contentData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        if(!new File(getFilesDir(),"isSignedIn.txt").exists()){
            Toast.makeText(this, "Sign in to view your watchlist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (new Theme(this).isInDarkMode()) {
            findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
            ((TextView)findViewById(R.id.tv_empty_list)).setTextColor(Color.parseColor("#CCCCCC"));
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(uid==null) return;
        FirebaseDatabase.getInstance().getReference("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Object object = snapshot.child("Watchlist").getValue();
                    watchList = (ArrayList<String>) object;
                    loadWatchList();
                } catch (Exception ignored) {}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadWatchList() {
        FirebaseDatabase.getInstance().getReference("content").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                findViewById(R.id.pb_loading).setVisibility(View.GONE);
                UserName.setWatchlist(watchList);
                if(watchList==null) watchList = new ArrayList<>();
                if(watchList.size()==0){
                    findViewById(R.id.empty_list_layout).setVisibility(View.VISIBLE);
                    return;
                }
                contentData = new ContentData[watchList.size()];
                for(int i=0;i<watchList.size();i++){
                    String genre = null;
                    String number = null;
                    String item = watchList.get(i);
                    String[] category = new String[]{"comedy","movie","classic"};
                    for (String genreName : category) {
                        if (item.startsWith(genreName)) {
                            genre = genreName;
                            number = item.substring(genreName.length());
                            break;
                        }
                    }
                    contentData[i] = new ContentData(WatchlistActivity.this,snapshot.child(genre+number).getValue().toString(),
                            snapshot.child(genre+"image"+number).getValue().toString(),
                            snapshot.child(genre+"link"+number).getValue().toString(),
                            snapshot.child(genre+"date"+number).getValue().toString(),
                            "",genre+number);
                }
                ContentAdapter contentAdapter = new ContentAdapter(contentData,WatchlistActivity.this,new Theme(getApplicationContext()).isInDarkMode());
                contentAdapter.setAnimate(false);
                if(recyclerView.getAdapter()==null) recyclerView.setAdapter(contentAdapter);
                else ((ContentAdapter)recyclerView.getAdapter()).setContentData(contentData);
                findViewById(R.id.empty_list_layout).setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadWatchList();
    }

    public void goToSearch(View view) {
        Intent searchIntent = new Intent(this,SearchActivity.class);
        searchIntent.putExtra("signed_in",true);
        startActivity(searchIntent);
    }
}