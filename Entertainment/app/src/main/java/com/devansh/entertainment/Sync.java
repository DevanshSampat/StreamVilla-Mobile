package com.devansh.entertainment;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Sync {

    private DocumentReference documentReference;
    private boolean add=false;
    private int attempt;
    private ArrayList<String> watchList;
    private String quickPicks;
    private boolean dataSynced = false;
    private ArrayList<String> serverDataForViewedGenres;

    public void uploadHistory(Context context)
    {
        String uid = null;
        try{
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(uid==null) return;
        try {
            FileInputStream fis = context.openFileInput("History.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str,data_to_be_uploaded="";
            while((str=br.readLine())!=null) data_to_be_uploaded = data_to_be_uploaded + str + "\n";
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("Watch History").setValue(data_to_be_uploaded);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addToWatchList(Context context,String str){
        String uid = null;
        try{
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(uid==null) return;
        final boolean[] isLoaded = {false};
        String finalUid = uid;
        FirebaseDatabase.getInstance().getReference("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isLoaded[0]) return;
                isLoaded[0] = true;
                try{
                    watchList = (ArrayList<String>) snapshot.child("Watchlist").getValue();
                } catch (Exception e) {
                    watchList = new ArrayList<String>();
                }
                if(watchList==null) watchList = new ArrayList<String>();
                if(!watchList.contains(str)) watchList.add(str);
                UserName.setWatchlist(watchList);
                FirebaseDatabase.getInstance().getReference("users").child(finalUid).child("Watchlist").setValue(watchList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void removeFromWatchList(Context context,String str){
        String uid = null;
        try{
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(uid==null) return;
        final boolean[] isLoaded = {false};
        String finalUid = uid;
        FirebaseDatabase.getInstance().getReference("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isLoaded[0]) return;
                isLoaded[0] = true;
                try{
                    watchList = (ArrayList<String>) snapshot.child("Watchlist").getValue();
                } catch (Exception e) {
                    watchList = new ArrayList<String>();
                }
                if(watchList==null) return;
                watchList.remove(str);
                UserName.setWatchlist(watchList);
                FirebaseDatabase.getInstance().getReference("users").child(finalUid).child("Watchlist").setValue(watchList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void addToQuickPicks(final Context context, final String str)
    {
        if(UserName.getUsername(context)==null) return;
        quickPicks = UserName.getQuickPicks();
        if(quickPicks==null)
        {
            if(attempt==1) return;
            attempt++;
            boolean[] isLoaded = new boolean[]{false};
            FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(isLoaded[0]) return;
                    isLoaded[0] = true;
                    quickPicks = snapshot.child("Quick Picks").getValue().toString();
                    UserName.setQuickPicks(quickPicks);
                    addToQuickPicks(context, str);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return;
        }
        String[] list = new String[5];
        String temp=str+"\n";
        int count=1;
        for(int i=1;i<=5;i++)
        {
            String name = quickPicks.substring(0,quickPicks.indexOf('\n'));
            if(!name.equals(str)&&count<5)
            {
                temp=temp+name+"\n";
                count++;
            }
            if(i<5) quickPicks=quickPicks.substring(quickPicks.indexOf('\n')+1);
        }
        quickPicks=temp;
        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Quick Picks").setValue(quickPicks);
        UserName.setQuickPicks(quickPicks);
    }
    
    public void uploadTimeFiles(Context context){
        try {
            FileInputStream fis = context.openFileInput("DownloadedVideos.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str,data_to_be_uploaded="";
            while((str=br.readLine())!=null)
            {
                File file = new File(context.getFilesDir(),str.replace(' ','+')+".txt");
                if(file.exists())
                {
                    str=str.replace(' ','+')+".txt";
                    FileInputStream fileInputStream = context.openFileInput(str);
                    BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(fileInputStream));
                    data_to_be_uploaded = data_to_be_uploaded + str + "\n" +bufferedReader.readLine() + "\n";
                }
            }
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Watch Files").setValue(data_to_be_uploaded);
            data_to_be_uploaded="";
            FileInputStream inputHistory = context.openFileInput("History.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputHistory));
            String strTemp;
            while ((strTemp=bufferedReader.readLine())!=null)
            {
                data_to_be_uploaded = data_to_be_uploaded + strTemp + "\n";
            }
            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Watch History").setValue(data_to_be_uploaded);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToViewedGenres(ArrayList<String> arrayList){
        serverDataForViewedGenres = new ArrayList<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("viewed_genres").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(dataSynced) return;
                dataSynced = true;
                try{
                    serverDataForViewedGenres = (ArrayList<String>) snapshot.getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ArrayList<String> dataToPut = new ArrayList<>();
                dataToPut.addAll(arrayList);
                if(serverDataForViewedGenres==null) serverDataForViewedGenres = new ArrayList<>();
                for(String str : serverDataForViewedGenres) if(!dataToPut.contains(str)) dataToPut.add(str);
                snapshot.getRef().setValue(dataToPut);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
