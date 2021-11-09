package com.devansh.entertainment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserDetailsActivity extends AppCompatActivity {

    private String notificationToken;
    private String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("id")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    notificationToken = snapshot.child("Notification Token").getValue().toString();
                    type = snapshot.child("Type").getValue().toString();
                    ((TextView)findViewById(R.id.tv_user_type)).setText(type.toUpperCase());
                    ((TextView)findViewById(R.id.tv_user_name)).setText(snapshot.child("Name").getValue().toString());
                    ((TextView)findViewById(R.id.tv_user_email)).setText(snapshot.child("Email").getValue().toString());
                    Picasso.get().load(snapshot.child("Image").getValue().toString()).into((ImageView)findViewById(R.id.iv_profile_image));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendNotification(View view){
        Intent intent = new Intent(this,FirebaseNotificationActivity.class);
        intent.putExtra("token",notificationToken);
        intent.putExtra("data","notification:true");
        startActivity(intent);
    }

    public void viewRawData(View view){
        Intent intent = new Intent(this,RawUserDataActivity.class);
        intent.putExtra("id",getIntent().getStringExtra("id"));
        startActivity(intent);
    }

    public void changeUserType(View view) {
        try {
            FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("id")).child("Type")
                    .setValue(type.equals("beta")?"stable":"beta");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"+((TextView)findViewById(R.id.tv_user_email)).getText().toString()));
        startActivity(intent);
    }
}