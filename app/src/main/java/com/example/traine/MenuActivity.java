package com.example.traine;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.nio.file.Path;

import Models.User;

public class MenuActivity extends AppCompatActivity {

    private TextView userName;
    private ImageView btnUserProfile;
    private RelativeLayout rootMenu;

    private MenuViewModel menuViewModel;
    private User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initViewModel();
        setupButtons();
    }

    private void initViews() {
        rootMenu = findViewById(R.id.root_menu);
        btnUserProfile = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
    }

    private void initViewModel() {
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        currentUser = getIntent().getParcelableExtra("userDetails");
        if(currentUser != null){
            menuViewModel.setUser(currentUser);
        }else {
            // Если currentUser не существует, вызываем initFirebase
            menuViewModel.initFirebase();
        }
        menuViewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
                updateUI(user);
            }
        });
    }

    private void setupButtons() {
        btnUserProfile.setOnClickListener(v -> goToActivity(ProfileActivity.class));
        findViewById(R.id.videoButtonImage).setOnClickListener(v -> goToActivity(PlaylistActivity.class));
        findViewById(R.id.button3).setOnClickListener(v -> goToActivity(ScannerActivity.class));
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(MenuActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("userDetails", currentUser);
        startActivity(intent);
    }

    private void updateUI(User user) {
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
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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


