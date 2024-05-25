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

    private DatabaseReference users;
    private ValueEventListener usersEventListener;

    private User currentUser;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        findViewById(R.id.button2).setOnClickListener(view -> {
//            Intent intent = new Intent(MenuActivity.this, CalendarActivity.class);
//            startActivity(intent);
//        });

        initViews();
        initFirebase();
        setupButtons();
    }

    private void initViews() {
        rootMenu = findViewById(R.id.root_menu);
        btnUserProfile = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);

    }

    private void initFirebase() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("Menu Activity UID", uid);
        users = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid);
    }

    private void attachDatabaseReadListener() {
        if (usersEventListener == null) {
            usersEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUser = dataSnapshot.getValue(User.class);
                    assert currentUser != null;
                    currentUser.setUid(uid);
                    updateUI(currentUser);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(rootMenu, "Error loading user data: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            };
            users.addValueEventListener(usersEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (usersEventListener != null) {
            users.removeEventListener(usersEventListener);
            usersEventListener = null;
        }
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

    private void setupButtons() {
        btnUserProfile.setOnClickListener(v -> goToActivity(ProfileActivity.class));
        findViewById(R.id.videoButtonImage).setOnClickListener(v -> goToActivity(PlaylistActivity.class));
        findViewById(R.id.button3).setOnClickListener(v -> goToActivity(ScannerActivity.class));
        findViewById(R.id.button2).setOnClickListener(view -> goToActivity(CalendarActivity.class));
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(MenuActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("userDetails", currentUser);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = getIntent().getParcelableExtra("userDetails");
        if(currentUser == null) {
            attachDatabaseReadListener();
        } else if (currentUser != null) {
            updateUI(currentUser);
        } else {
            Toast.makeText(MenuActivity.this, "User data not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachDatabaseReadListener();
    }
}
//public class MenuActivity extends AppCompatActivity {
//
//    private TextView userName;
//    private ImageView btnUserProfile;
//    private RelativeLayout rootMenu;
//
//    private MenuViewModel menuViewModel;
//    private User currentUser;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        initViews();
//        initViewModel();
//        setupButtons();
//    }
//
//    private void initViews() {
//        rootMenu = findViewById(R.id.root_menu);
//        btnUserProfile = findViewById(R.id.profileImage);
//        userName = findViewById(R.id.userName);
//    }
//
//    private void initViewModel() {
//        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
//            // Если currentUser не существует, вызываем initFirebase
//        menuViewModel.initFirebase();
//
//        menuViewModel.getUser().observe(this, new Observer<User>() {
//            @Override
//            public void onChanged(User user) {
//                currentUser = user;
//                updateUI(user);
//            }
//        });
//    }
//
//    private void updateCurrentUser(){
//        currentUser = getIntent().getParcelableExtra("userDetails");
//        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
//        if(currentUser != null){
//            menuViewModel.setUser(currentUser);
//        }else {
//            // Если currentUser не существует, вызываем initFirebase
//            menuViewModel.initFirebase();
//        }
//        menuViewModel.getUser().observe(this, new Observer<User>() {
//            @Override
//            public void onChanged(User user) {
//                currentUser = user;
//                updateUI(user);
//            }
//        });
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
//    @Override
//    protected void onStart() {
//        super.onStart();
//        updateCurrentUser();
//    }
//}

//public class MenuActivity extends AppCompatActivity {
//
//    private TextView userName;
//    private ImageView btnUserProfile;
//    private RelativeLayout rootMenu;
//    private Button btnToCalendar;
//
//    private MenuViewModel menuViewModel;
//    private User currentUser;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        initViews();
//        initViewModel();
//        setupButtons();
//    }
//
//    private void initViews() {
//        rootMenu = findViewById(R.id.root_menu);
//        btnUserProfile = findViewById(R.id.profileImage);
//        userName = findViewById(R.id.userName);
//        btnToCalendar = findViewById(R.id.button2);
//    }
//
//    private void initViewModel() {
//        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
//        currentUser = getIntent().getParcelableExtra("userDetails");
//        if(currentUser != null){
//            menuViewModel.setUser(currentUser);
//            Log.d("User", "Download userdata from Parcel");
//        }else {
//            // Если currentUser не существует, вызываем initFirebase
//            menuViewModel.initFirebase();
//            Log.d("User", "Download userdata from FirebaseDatabase");
//        }
//        menuViewModel.getUser().observe(this, new Observer<User>() {
//            @Override
//            public void onChanged(User user) {
//                currentUser = user;
//                updateUI(user);
//                Log.d("Menu Activity", currentUser.getUid());
//                Log.d("Menu Activity", currentUser.getEmail());
//                Log.d("Menu Activity", currentUser.getName());
//                Log.d("Menu Activity", currentUser.getPhone());
//                Log.d("Menu Activity UID", currentUser.getUid());
//            }
//        });
//    }
//
//    private void setupButtons() {
//        btnUserProfile.setOnClickListener(v -> goToActivity(ProfileActivity.class));
//        findViewById(R.id.videoButtonImage).setOnClickListener(v -> goToActivity(PlaylistActivity.class));
//        findViewById(R.id.button3).setOnClickListener(v -> goToActivity(ScannerActivity.class));
//        btnToCalendar.setOnClickListener(view -> goToActivity(CalendarActivity.class));
//    }
//
//    private void goToActivity(Class<?> activityClass) {
//        Intent intent = new Intent(MenuActivity.this, activityClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("userDetails", currentUser);
//        startActivity(intent);
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
//}


//public class MenuActivity extends AppCompatActivity {
//
//    private TextView userName;
//    private ImageView btnUserProfile;
//    private RelativeLayout rootMenu;
//
//    private DatabaseReference users;
//    private ValueEventListener usersEventListener;
//    Button buttonToCalendar;
//
//    private User currentUser;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        findViewById(R.id.button2).setOnClickListener(view -> {
//            Intent intent = new Intent(MenuActivity.this, CalendarActivity.class);
//            startActivity(intent);
//        });
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
//        buttonToCalendar = findViewById(R.id.button2);
//    }
//
//    private void initFirebase() {
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        Log.d("Firebase UID", uid);
////        if (currentUser != null) {
////            currentUser.setUid(uid);
////            Log.d("Firebase UID", "Uid successfully added in User");
////        } else {
////            // Обробка помилки або повідомлення про те, що користувач не ініціалізований
////            Log.e("MenuActivity", "currentUser is null");
////        }
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
//        buttonToCalendar.setOnClickListener(view -> goToActivity(CalendarActivity.class));
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


