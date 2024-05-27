package com.example.traine;


import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import Models.User;

public class MenuActivity extends AppCompatActivity {
    private TextView userName;
    private ImageView btnUserProfile;
    private RelativeLayout rootMenu;
    private ProfileViewModel profileViewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        initViews();
        observeViewModel();
        setupButtons();
    }

    private void initViews() {
        rootMenu = findViewById(R.id.root_menu);
        btnUserProfile = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
    }

    private void observeViewModel() {
        profileViewModel.getUser().observe(this, user -> {
            updateUI(user);
            currentUser = user;
        });
    }

    private void updateUI(User user){
        if (user != null) {
            userName.setText(user.getName());
            if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
                Picasso.get()
                        .load(user.getProfileUri())
                        .placeholder(R.drawable.ic_profile)
                        .resize(48, 48)
                        .into(btnUserProfile);
            }
        }
    }

    private void setupButtons() {
        btnUserProfile.setOnClickListener(v -> goToActivity(ProfileActivity.class));
        findViewById(R.id.videoButtonImage).setOnClickListener(v -> goToActivity(PlaylistActivity.class));
        findViewById(R.id.button3).setOnClickListener(v -> goToActivity(ScannerActivity.class));
        findViewById(R.id.button2).setOnClickListener(view -> goToActivity(CalendarActivity.class));
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(MenuActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        currentUser = profileViewModel.getUser().getValue();
        intent.putExtra("userDetails", currentUser);
        Log.d("MA uid from currentUser", currentUser.getUid());
        startActivity(intent);
    }
}






//public class MenuActivity extends AppCompatActivity {
//
//    private TextView userName;
//    private ImageView btnUserProfile;
//    private RelativeLayout rootMenu;
//
//    private DatabaseReference users;
//    private ValueEventListener usersEventListener;
//
//    private User currentUser;
//
//    private String uid;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        initViews();
//        initFirebase();
//        setupButtons();
//    }
//
//    private void initViews() {
//        rootMenu = findViewById(R.id.root_menu);
//        btnUserProfile = findViewById(R.id.profileImage);
//        userName = findViewById(R.id.userName);
//
//    }
//
//    private void initFirebase() {
//        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        Log.d("Menu Activity UID", uid);
//        users = FirebaseDatabase.getInstance()
//                .getReference("Users")
//                .child(uid);
//    }
//
//    private void attachDatabaseReadListener() {
//        if (usersEventListener == null) {
//            usersEventListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    currentUser = dataSnapshot.getValue(User.class);
//                    assert currentUser != null;
//                    currentUser.setUid(uid);
//                    updateUI(currentUser);
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Snackbar.make(rootMenu, "Error loading user data: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
//                }
//            };
//            users.addValueEventListener(usersEventListener);
//        }
//    }
//
//    private void detachDatabaseReadListener() {
//        if (usersEventListener != null) {
//            users.removeEventListener(usersEventListener);
//            usersEventListener = null;
//        }
//    }
//
//    private void updateUI(User user) {
//        userName.setText(user.getName());
//        if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
//            Picasso.get()
//                    .load(user.getProfileUri())
//                    .placeholder(R.drawable.ic_profile)
//                    .resize(48, 48)
//                    .into(btnUserProfile);
//        }
//    }
//
//    private void setupButtons() {
//        btnUserProfile.setOnClickListener(v -> goToActivity(ProfileActivity.class));
//        findViewById(R.id.videoButtonImage).setOnClickListener(v -> goToActivity(PlaylistActivity.class));
//        findViewById(R.id.button3).setOnClickListener(v -> goToActivity(ScannerActivity.class));
//        findViewById(R.id.button2).setOnClickListener(view -> goToActivity(CalendarActivity.class));
//    }
//
//    private void goToActivity(Class<?> activityClass) {
//        Intent intent = new Intent(MenuActivity.this, activityClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("userDetails", currentUser);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        currentUser = getIntent().getParcelableExtra("userDetails");
//        if(currentUser == null) {
//            attachDatabaseReadListener();
//        } else if (currentUser != null) {
//            updateUI(currentUser);
//        } else {
//            Toast.makeText(MenuActivity.this, "User data not available", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        detachDatabaseReadListener();
//    }
//}



