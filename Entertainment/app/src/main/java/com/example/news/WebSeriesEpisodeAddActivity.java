package com.example.news;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WebSeriesEpisodeAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_series_episode_add);
    }

    public void submitData(View view) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String dbName = null, startEp = null , links = null;
        try{
            dbName = ((EditText)findViewById(R.id.db)).getText().toString();
            startEp = ((EditText)findViewById(R.id.start_episode)).getText().toString();
            links = ((EditText)findViewById(R.id.links)).getText().toString();
            if(dbName.trim().length()==0||startEp.trim().length()==0||links.trim().length()==0) return;
        } catch (Exception e) {
            return;
        }
        int i = Integer.parseInt(startEp);
        String sub = "";
        if(dbName.endsWith("sub")){
            dbName = dbName.substring(0,dbName.length()-3);
            sub = "sub";
        }
        while(links!=null){
            if(links.contains("\n")){
                reference.child(dbName+"e"+i+sub).setValue(links.substring(0,links.indexOf('\n')));
                links = links.substring(links.indexOf('\n')+1);
            }
            else {
                reference.child(dbName+"e"+i+sub).setValue(links);
                links = null;
            }
            i++;
        }
        finish();
    }
}