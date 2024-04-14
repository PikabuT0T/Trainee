package com.example.traine;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class MenuActivity extends AppCompatActivity {

    ImageButton btnUserProfile;
    Button userName;

    private FirebaseDatabase db;
    private DatabaseReference users;
    ProfileActivity test = new ProfileActivity();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUserProfile = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);

        btnUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        Picasso.get()
                .load("https://firebasestorage.googleapis.com/v0/b/traine-11a25.appspot.com/o/images%2Fprof%2F1185768966?alt=media&token=67595cd2-1675-4d4f-bfeb-cab2f24a7ca3")
                .into(btnUserProfile);

        userName.setText("Test");
    }
}