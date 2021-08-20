package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FirebaseNotificationActivity extends AppCompatActivity {

    private ArrayList<ContentData> content;
    private String beta_version,stable_version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_notification);
        content = new ArrayList<>();
        ((EditText)findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
        ((EditText)findViewById(R.id.text)).setText(getIntent().getStringExtra("text"));
        ((EditText)findViewById(R.id.image)).setText(getIntent().getStringExtra("image"));
        ((EditText)findViewById(R.id.extras)).setText(getIntent().getStringExtra("data"));
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                beta_version = snapshot.child("beta_name").getValue().toString();
                stable_version = snapshot.child("version_name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                content.clear();
                int i;
                for(i=1;i<Integer.parseInt(snapshot.child("movie").getValue().toString());i++){
                    try {
                        content.add(new ContentData(snapshot.child("movie" + i).getValue().toString(),
                                snapshot.child("movieimage" + i).getValue().toString(),
                                snapshot.child("movielink" + i).getValue().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for(i=1;i<Integer.parseInt(snapshot.child("classic").getValue().toString());i++){
                    try{
                        content.add(new ContentData(snapshot.child("classic"+i).getValue().toString(),
                                snapshot.child("classicimage"+i).getValue().toString(),
                                snapshot.child("classiclink"+i).getValue().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for(i=1;i<Integer.parseInt(snapshot.child("comedy").getValue().toString());i++){
                    try{
                        if(snapshot.child("comedylink" + i).getValue().toString().equals("webseries")) {
                            content.add(new ContentData(snapshot.child("comedy" + i).getValue().toString(),
                                    snapshot.child("comedyimage" + i).getValue().toString(),
                                    snapshot.child("comedylink" + i).getValue().toString()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        findViewById(R.id.random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRandom();
            }
        });
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUpdateNotification();
            }
        });
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String title = ((EditText) findViewById(R.id.title)).getText().toString();
                    String token = ((EditText) findViewById(R.id.token)).getText().toString();
                    String text = ((EditText) findViewById(R.id.text)).getText().toString();
                    String image = ((EditText) findViewById(R.id.image)).getText().toString();
                    String extras = ((EditText) findViewById(R.id.extras)).getText().toString();
                    HashMap<String, Object> data = new HashMap<>();
                    while(extras.contains("\n")){
                        String temp = extras.substring(0,extras.indexOf('\n'));
                        data.put(temp.substring(0,temp.indexOf(':')),temp.substring(temp.indexOf(':')+1));
                        extras = extras.substring(extras.indexOf('\n')+1);
                    }
                    String temp = extras;
                    data.put(temp.substring(0,temp.indexOf(':')),temp.substring(temp.indexOf(':')+1));
                    NotificationModel notificationModel = new NotificationModel(title,text);
                    notificationModel.setImage(image);
                    notificationModel.setAndroidChannelId("general");
                    RequestNotification requestNotification = new RequestNotification();
                    requestNotification.setToken(token);
                    requestNotification.setNotificationModel(notificationModel);
                    requestNotification.setData(data);
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://fcm.googleapis.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    RetrofitMessagingApi retrofitMessagingApi = retrofit.create(RetrofitMessagingApi.class);
                    Call<ResponseBody> call = retrofitMessagingApi.sendNotification(requestNotification);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try{
                                response.body().toString();
                                Toast.makeText(FirebaseNotificationActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (Exception e) {
                                Log.println(Log.ASSERT,"fail",e.toString());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.println(Log.ASSERT,"fail","cannot send");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FirebaseNotificationActivity.this, "Couldn't send message", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void sendRandom(){
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot testSnapshot : queryDocumentSnapshots)
                {
                    try{
                        String token = testSnapshot.getString("Notification Token");
                        Calendar calendar = Calendar.getInstance();
                        try {
                            String lastSignIn = testSnapshot.getString("Last Sign in");
                            calendar.set(Calendar.YEAR, Integer.parseInt(lastSignIn.substring(0, lastSignIn.indexOf('/'))));
                            lastSignIn = lastSignIn.substring(lastSignIn.indexOf('/') + 1);
                            calendar.set(Calendar.MONTH, Integer.parseInt(lastSignIn.substring(0, lastSignIn.indexOf('/'))) - 1);
                            lastSignIn = lastSignIn.substring(lastSignIn.indexOf('/') + 1);
                            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(lastSignIn.substring(0, lastSignIn.indexOf('/'))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://fcm.googleapis.com/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        RequestNotification requestNotification = new RequestNotification();
                        int random = new Random().nextInt();
                        if(random<0) random = random*-1;
                        random = random%content.size();
                        ContentData randomContent = content.get(random);
                        NotificationModel notificationModel = new NotificationModel(randomContent.getName(), "Today's pick for you");
                        notificationModel.setImage(randomContent.getImage());
                        notificationModel.setAndroidChannelId("general");
                        requestNotification.setNotificationModel(notificationModel);
                        requestNotification.setToken(token);
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("open", true);
                        data.put("search", randomContent.getName());
                        requestNotification.setData(data);
                        RetrofitMessagingApi retrofitMessagingApi = retrofit.create(RetrofitMessagingApi.class);
                        Call<ResponseBody> call = retrofitMessagingApi.sendNotification(requestNotification);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try{
                                    response.body().toString();
                                    Log.println(Log.ASSERT,"sent to",testSnapshot.getId());
                                } catch (Exception e) {
                                    Log.println(Log.ASSERT,"fail",e.toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.println(Log.ASSERT,"fail","cannot send");
                            }
                        });
                    } catch (Exception ignored) {

                    }
                }
                finish();
            }
        });
    }
    private void sendUpdateNotification(){
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot testSnapshot : queryDocumentSnapshots)
                {
                    try{
                        String token = testSnapshot.getString("Notification Token");
                        String version = testSnapshot.getString("App version");
                        String userType = testSnapshot.getString("Type");
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://fcm.googleapis.com/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        RequestNotification requestNotification = new RequestNotification();
                        if((userType.equals("beta")&&!version.equals(beta_version))
                                ||(userType.equals("stable")&&!version.equals(stable_version))) {
                            NotificationModel notificationModel = new NotificationModel("An update is avaiabe", "Version "+
                                    (userType.equals("beta")?beta_version:stable_version)+" is available for download");
                            notificationModel.setAndroidChannelId("app_update");
                            requestNotification.setNotificationModel(notificationModel);
                            requestNotification.setToken(token);
                        }
                        else{
                            NotificationModel notificationModel = new NotificationModel("Want to enjoy watching something?", "Have a look at what's new in our app");
                            requestNotification.setNotificationModel(notificationModel);
                            requestNotification.setToken(token);
                        }
                        RetrofitMessagingApi retrofitMessagingApi = retrofit.create(RetrofitMessagingApi.class);
                        Call<ResponseBody> call = retrofitMessagingApi.sendNotification(requestNotification);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try{
                                    response.body().toString();
                                    Log.println(Log.ASSERT,"sent to",testSnapshot.getId());
                                } catch (Exception e) {
                                    Log.println(Log.ASSERT,"fail",e.toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.println(Log.ASSERT,"fail","cannot send");
                            }
                        });
                    } catch (Exception ignored) {

                    }
                }
                finish();
            }
        });

    }
}