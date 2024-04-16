package com.example.traine;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.security.spec.ECField;
import java.util.concurrent.ExecutionException;

import Models.User;


public class MenuActivity extends AppCompatActivity {

    String name, email, phone, uri;

    ImageButton btnUserProfile;
    Button userName;
    private RelativeLayout rootMenu;

    ProfileActivity newUser = new ProfileActivity();

    private String uid;

    private FirebaseDatabase db;
    private DatabaseReference users;

    private User user = new User();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootMenu = findViewById(R.id.root_menu);
        btnUserProfile = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // Name, email address, and ??profile photo Url??
            //email = firebaseUser.getEmail();

            // Check if user's email is verified
            boolean emailVerified = firebaseUser.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = firebaseUser.getUid();
        }

        db = FirebaseDatabase.getInstance("https://traine-11a25-default-rtdb.europe-west1.firebasedatabase.app/");
        users = db.getReference("Users").child(uid);

        btnUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue(String.class);
                email = dataSnapshot.child("email").getValue(String.class);
                phone = dataSnapshot.child("phone").getValue(String.class);
                uri = dataSnapshot.child("testProfile").getValue(String.class);

                if (name == null || email == null || phone == null || uri == null) {
                    Snackbar.make(rootMenu, "Error: ", Snackbar.LENGTH_LONG).show();
                }

                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);
                user.setUri(uri);

                try {
                    Picasso.get()
                            .load(user.getUri())
                            .placeholder(R.drawable.ic_profile)
                            .into(btnUserProfile);

                    userName.setText(user.getName());
                }catch (Exception e){
                    Snackbar.make(rootMenu, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            };
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //обработать ошибки
            }
        });


    }

}