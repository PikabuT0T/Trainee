package com.example.traine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;

import Models.User;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private TextView nameView, emailView, phoneView;
    private ImageView imageView2;
    private ImageButton buttonMain, buttonLogout;
    private User currentUser;

    private DatabaseReference users;
    private ValueEventListener usersEventListener;
    private StorageReference storageRef;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initFirebase();
        initViews();
        initCurrentUser();
        setupListeners();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void initCurrentUser() {
        currentUser = getIntent().getParcelableExtra("userDetails");
        if (currentUser != null && auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            users = database.getReference("Users").child(uid);
            storageRef = storage.getReference("images").child("prof").child(uid).child("profile_image.jpg");
            attachDatabaseReadListener();
            updateUI(currentUser);
        }
    }

    private void initViews() {
        nameView = findViewById(R.id.viewName);
        emailView = findViewById(R.id.emailView);
        phoneView = findViewById(R.id.phoneView);
        imageView2 = findViewById(R.id.imageView2);
        buttonMain = findViewById(R.id.buttonToMainActivity);
        buttonLogout = findViewById(R.id.button_logout);
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.settings_name_change) {
            showRefactorWindow("name");
            return true;
        } else if (itemId == R.id.settings_password_change) {
            showRefactorWindow("password");
            return true;
        } else if (itemId == R.id.settings_phone_change) {
            showRefactorWindow("phone");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showRefactorWindow(String type) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Update your " + type);
        dialog.setMessage("Please enter the new " + type + " below:");

        LayoutInflater inflater = LayoutInflater.from(this);
        View refactorWindow = null;

        // Load specific layout based on the type of data being updated
        switch (type) {
            case "name":
                refactorWindow = inflater.inflate(R.layout.refactor_name_form, null);
                break;
            case "password":
                refactorWindow = inflater.inflate(R.layout.refactor_password_form, null);
                break;
            case "phone":
                refactorWindow = inflater.inflate(R.layout.refactor_phone_form, null);
                break;
        }

        if (refactorWindow != null) {
            dialog.setView(refactorWindow);
            TextInputEditText inputField = refactorWindow.findViewById(R.id.newValue);
            TextInputEditText currentPasswordField = refactorWindow.findViewById(R.id.currentPass);
            TextInputEditText secondInputField = type.equals("password") ? refactorWindow.findViewById(R.id.confirmNewValue) : null;

            dialog.setPositiveButton("Save", (dialogInterface, i) -> {
                String newValue = inputField.getText().toString().trim();
                String currentPassword = currentPasswordField != null ? currentPasswordField.getText().toString().trim() : null;
                String confirmNewValue = secondInputField != null ? secondInputField.getText().toString().trim() : null;

                if (type.equals("password") && !newValue.equals(confirmNewValue)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateUserInformation(type, newValue, currentPassword);
            });
            dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            dialog.show();
        } else {
            Toast.makeText(this, "Error: Unable to load the form.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserInformation(String type, String newValue, String currentPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getApplicationContext(), "No authenticated user.", Toast.LENGTH_SHORT).show();
            return;
        }

        Task<Void> updateTask;

        switch (type) {
            case "email":
                updateTask = user.updateEmail(newValue);
                break;
            case "password":
                updateTask = user.updatePassword(newValue);
                break;
            default:
                updateTask = users.child(type).setValue(newValue);
                break;
        }

        updateTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), type + " updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to update " + type + ".", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(User user) {
        nameView.setText(user.getName());
        emailView.setText(user.getEmail());
        phoneView.setText(user.getPhone());
        Picasso.get().load(user.getProfileUri()).placeholder(R.drawable.ic_profile).into(imageView2);
    }

    private void attachDatabaseReadListener() {
        if (usersEventListener == null) {
            usersEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        currentUser = user;
                        updateUI(currentUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileActivity", "Failed to read user data", error.toException());
                }
            };
            users.addValueEventListener(usersEventListener);
        }
    }
    private void setupListeners() {
        buttonMain.setOnClickListener(view -> goToActivity(MenuActivity.class));
        imageView2.setOnClickListener(view -> selectImage());
        buttonLogout.setOnClickListener(view -> {
            checkToken();
            //clearCurrentUser();
            clearAllData();
            goToActivity(MainActivity.class);
        });
    }

    private void clearAllData() {
        // Удаление данных из SharedPreferences
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Очистка кеша приложения
        clearCache();
    }

    private void clearCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void checkToken() {
        String authToken = getToken();
        if (authToken != null) {
            deleteToken();
        } else {
            Log.d("Auth Token Failure", "Token was not found");
        }
    }

    private void deleteToken() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("AuthToken");
        Log.d("Auth Token", "Token was deleted");
        editor.apply();
    }

    private String getToken() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("AuthToken", null);
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(ProfileActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("userDetails", currentUser);
        startActivity(intent);
    }
    private void detachDatabaseReadListener() {
        if (usersEventListener != null) {
            users.removeEventListener(usersEventListener);
            usersEventListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
    }
}
