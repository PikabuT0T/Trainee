package com.example.traine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;
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
        if (currentUser != null) {
            String uid = auth.getCurrentUser().getUid();
            users = database.getReference("Users").child(uid);
            storageRef = storage.getReference("images").child("prof").child(uid).child("profile_image.jpg");
            attachDatabaseReadListener();
            updateUI(currentUser);
        }
//        } else {
//            clearCurrentUser();
//        }
    }

    private void clearCurrentUser() {
        currentUser = null;
        nameView.setText("");
        emailView.setText("");
        phoneView.setText("");
        imageView2.setImageResource(R.drawable.ic_profile);
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
        if (item.getItemId() == R.id.settings_email_change) {
            showRefactorWindow("email");
        }
        if (item.getItemId() == R.id.settings_name_change) {
            showRefactorWindow("name");
        }
        if (item.getItemId() == R.id.settings_password_change) {
            showRefactorWindow("password");
        }
        if (item.getItemId() == R.id.settings_phone_change) {
            showRefactorWindow("phone");
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRefactorWindow(String type) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Внесіть зміни");
        dialog.setMessage("Введіть зміни в поля нижче");

        LayoutInflater inflater = LayoutInflater.from(this);
        View refactorWindow = null;
        TextInputEditText inputField = null;
        TextInputEditText secondInputField = null;
        TextInputEditText currentPasswordField = null;

        switch (type) {
            case "email":
                refactorWindow = inflater.inflate(R.layout.refactor_email_form, null);
                inputField = refactorWindow.findViewById(R.id.emailField);
                currentPasswordField = refactorWindow.findViewById(R.id.currentPass); // Добавьте поле для текущего пароля
                break;
            case "name":
                refactorWindow = inflater.inflate(R.layout.refactor_name_form, null);
                inputField = refactorWindow.findViewById(R.id.nameField);
                break;
            case "password":
                refactorWindow = inflater.inflate(R.layout.refactor_password_form, null);
                inputField = refactorWindow.findViewById(R.id.newPass);
                secondInputField = refactorWindow.findViewById(R.id.secondPass);
                currentPasswordField = refactorWindow.findViewById(R.id.currentPass); // Добавьте поле для текущего пароля
                break;
            case "phone":
                refactorWindow = inflater.inflate(R.layout.refactor_phone_form, null);
                inputField = refactorWindow.findViewById(R.id.phoneField);
                break;
        }

        dialog.setView(refactorWindow);
        TextInputEditText finalInputField = inputField;
        TextInputEditText finalSecondInputField = secondInputField;
        TextInputEditText finalCurrentPasswordField = currentPasswordField;

        dialog.setPositiveButton("Зберегти", (dialogInterface, i) -> {
            String newValue = finalInputField.getText().toString().trim();
            if (type.equals("password")) {
                String confirmNewValue = finalSecondInputField.getText().toString().trim();
                String currentPassword = finalCurrentPasswordField.getText().toString().trim();
                if (!newValue.equals(confirmNewValue)) {
                    Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateUserInformation(type, newValue, currentPassword);
            } else if (type.equals("email")) {
                String currentPassword = finalCurrentPasswordField.getText().toString().trim();
                updateUserInformation(type, newValue, currentPassword);
            } else {
                updateUserInformation(type, newValue, null);
            }
        });
        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.show();
    }

    private void updateUserInformation(String type, String newValue, String currentPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getApplicationContext(), "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        if (currentPassword != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseAuth", "Повторная аутентификация успешна.");
                        performUpdate(type, newValue, user, userRef);
                    } else {
                        Log.e("FirebaseAuth", "Ошибка повторной аутентификации: " + task.getException());
                        Toast.makeText(getApplicationContext(), "Ошибка повторной аутентификации", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            performUpdate(type, newValue, user, userRef);
        }
    }

    private void performUpdate(String type, String newValue, FirebaseUser user, DatabaseReference userRef) {
        switch (type) {
            case "email":
                user.updateEmail(newValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    users.setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("FirebaseDatabase", "Email успешно записан в базу данных.");
                                                Toast.makeText(getApplicationContext(), "Email обновлен", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e("FirebaseDatabase", "Ошибка при записи email в базу данных: " + task.getException());
                                                Toast.makeText(getApplicationContext(), "Ошибка при обновлении email в БД", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Log.e("FirebaseAuth", "Ошибка при обновлении email: " + task.getException());
                                    Toast.makeText(getApplicationContext(), "Ошибка при обновлении email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case "name":
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newValue)
                        .build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    userRef.child("name").setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("FirebaseDatabase", "Имя успешно записано в базу данных.");
                                                Toast.makeText(getApplicationContext(), "Имя обновлено", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e("FirebaseDatabase", "Ошибка при записи имени в базу данных: " + task.getException());
                                                Toast.makeText(getApplicationContext(), "Ошибка при обновлении имени в БД", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Log.e("FirebaseAuth", "Ошибка при обновлении имени: " + task.getException());
                                    Toast.makeText(getApplicationContext(), "Ошибка при обновлении имени", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case "password":
                user.updatePassword(newValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("FirebaseAuth", "Пароль успешно обновлен.");
                                    Toast.makeText(getApplicationContext(), "Пароль обновлен", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("FirebaseAuth", "Ошибка при обновлении пароля: " + task.getException());
                                    Toast.makeText(getApplicationContext(), "Ошибка при обновлении пароля", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case "phone":
                userRef.child("phone").setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("FirebaseDatabase", "Телефон успешно записан в базу данных.");
                            Toast.makeText(getApplicationContext(), "Телефон обновлен", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("FirebaseDatabase", "Ошибка при записи телефона в базу данных: " + task.getException());
                            Toast.makeText(getApplicationContext(), "Ошибка при обновлении телефона в БД", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
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

    private void checkToken() {
        String authToken = getToken();
        if (authToken != null) {
            deleteToken();
        } else {
            Log.d("Auth Token Failure", "Token was not found");
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadImage(data.getData());
            Toast.makeText(this, "Зображення успішно завантажене!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error: Спробуйте завантажити інше зображення!", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(Uri imageUri) {
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String newUri = uri.toString();
                users.child("profileUri").setValue(newUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.setProfileUri(newUri);
                        updateUI(currentUser);
                    } else {
                        Log.e("ProfileActivity", "Failed to update profile URI");
                    }
                });
            }).addOnFailureListener(e -> Log.e("ProfileActivity", "Failed to get download URL"));
        }).addOnFailureListener(e -> Log.e("ProfileActivity", "Failed to upload image"));
    }

    private void updateUI(User user) {
        if (user != null) {
            nameView.setText(user.getName());
            emailView.setText(user.getEmail());
            phoneView.setText(user.getPhone());
            if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
                Picasso.get()
                        .load(user.getProfileUri())
                        .placeholder(R.drawable.ic_profile)
                        .resize(140, 140)
                        .into(imageView2);
            }
        }
//        } else {
//            clearCurrentUser();
//        }
    }

    private void goToActivity(Class<?> activityClass) {
        Intent intent = new Intent(ProfileActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("userDetails", currentUser);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initCurrentUser();
    }

    private void attachDatabaseReadListener() {
        if (usersEventListener == null) {
            usersEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currentUser = dataSnapshot.getValue(User.class);
                    updateUI(currentUser);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileActivity", "Failed to read user data", error.toException());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
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
}


//public class ProfileActivity extends AppCompatActivity {
//    private ProfileViewModel viewModel;
//    private TextView nameView, emailView, phoneView;
//    private ImageView imageView2;
//    private ImageButton buttonMain, buttonLogout;
//    private User currentUser;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        initViews();
//        initCurrentUser();
//
//        // Получаем ViewModel
//        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
//
//        // Наблюдаем за изменениями в данных пользователя
//        viewModel.getUser().observe(this, this::updateUI);
//
//        // Устанавливаем слушатели
//        setupListeners();
//    }
//
//    private void initCurrentUser() {
//        currentUser = getIntent().getParcelableExtra("userDetails");
//        updateUI(currentUser);
//    }
//
//    private void clearCurrentUser() {
//        currentUser = null;
//        nameView.setText("");
//        emailView.setText("");
//        phoneView.setText("");
//        imageView2.setImageResource(R.drawable.ic_profile);
//    }
//
//    private void initViews() {
//        nameView = findViewById(R.id.viewName);
//        emailView = findViewById(R.id.emailView);
//        phoneView = findViewById(R.id.phoneView);
//        imageView2 = findViewById(R.id.imageView2);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        buttonLogout = findViewById(R.id.button_logout);
//        Toolbar toolbar = findViewById(R.id.toolbar_profile);
//        setSupportActionBar(toolbar);
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_items, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//
//        if (item.getItemId() == R.id.settings_email_change) {
//            showRefactorWindow("email");
//        }
//        if (item.getItemId() == R.id.settings_name_change) {
//            showRefactorWindow("name");
//        }
//        if (item.getItemId() == R.id.settings_password_change) {
//            showRefactorWindow("password");
//        }
//        if (item.getItemId() == R.id.settings_phone_change) {
//            showRefactorWindow("phone");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showRefactorWindow(String type) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactorWindow = null;
//        MaterialEditText inputField = null;
//
//        switch (type) {
//            case "email":
//                refactorWindow = inflater.inflate(R.layout.refactor_email_form, null);
//                inputField = refactorWindow.findViewById(R.id.emailField);
//                break;
//            case "name":
//                refactorWindow = inflater.inflate(R.layout.refactor_name_form, null);
//                inputField = refactorWindow.findViewById(R.id.nameField);
//                break;
//            case "password":
//                refactorWindow = inflater.inflate(R.layout.refactor_password_form, null);
//                inputField = refactorWindow.findViewById(R.id.newPass);
//                MaterialEditText secondInputField = refactorWindow.findViewById(R.id.secondPass);
//                break;
//            case "phone":
//                refactorWindow = inflater.inflate(R.layout.refactor_phone_form, null);
//                inputField = refactorWindow.findViewById(R.id.phoneField);
//                break;
//        }
//
//        dialog.setView(refactorWindow);
//        dialog.setPositiveButton("Зберегти", (dialogInterface, i) -> {
//            // Code to handle saving the new value
//        });
//        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());
//
//        dialog.show();
//    }
//
//    private void setupListeners() {
//        buttonMain.setOnClickListener(view -> goToActivity(MenuActivity.class));
//        imageView2.setOnClickListener(view -> selectImage());
//        buttonLogout.setOnClickListener(view -> {
//            checkToken();
//            clearCurrentUser();
//            goToActivity(MainActivity.class);
//        });
//    }
//
//    private void deleteToken() {
//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("AuthToken");
//        Log.d("Auth Token", "Token was deleted");
//        editor.apply();  // Или editor.commit() для синхронного удаления
//    }
//
//    private String getToken() {
//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", getApplication().MODE_PRIVATE);
//        return sharedPreferences.getString("AuthToken", null);  // Дешифрование токена после получения
//    }
//    private void checkToken(){
//        String authToken = getToken();
//        if(authToken != null){
//            deleteToken();
//        } else {
//            Log.d("Auth Token Failure", "Token was not found");
//        }
//    }
//
//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, viewModel.PICK_IMAGE);
//    }
//
//    // Обработка результатов выбора изображения
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == viewModel.PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            viewModel.uploadImage(data.getData());
//            Toast.makeText(this, "Зображення успішно завантажене!", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "Error: Спробуйте завантажити інше зображення!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // Обновление интерфейса пользователя
//    private void updateUI(User user) {
//        nameView.setText(user.getName());
//        emailView.setText(user.getEmail());
//        phoneView.setText(user.getPhone());
//        if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
//            Picasso.get()
//                    .load(user.getProfileUri())
//                    .placeholder(R.drawable.ic_profile)
//                    .resize(140, 140)
//                    .into(imageView2);
//        }
//    }
//
//    private void updateUserDetails(User updatedUser) {
//        viewModel.updateUser(updatedUser);
//    }
//
//    private void goToActivity(Class<?> activityClass) {
//        Intent intent = new Intent(ProfileActivity.this, activityClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("userDetails", currentUser);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent); // Обов'язково викликати setIntent для оновлення наміру
//        initCurrentUser();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        initCurrentUser();
//    }
//}

//public class ProfileActivity extends AppCompatActivity {
//    private ProfileViewModel viewModel;
//    private TextView nameView, emailView, phoneView;
//    private ImageView imageView2;
//    private ImageButton buttonMain, buttonLogout;
//    private User currentUser;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        initViews();
//        //initCurrentUser();
//
//        // Получаем ViewModel
//        //viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
//
//        // Наблюдаем за изменениями в данных пользователя
//        //viewModel.getUser().observe(this, this::updateUI);
//
//        // Устанавливаем слушатели
//        setupListeners();
//    }
//
//    private void initCurrentUser() {
//        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
//        currentUser = getIntent().getParcelableExtra("userDetails");
//        Log.d("Profile Activity", currentUser.getUid());
//        Log.d("Profile Activity", currentUser.getEmail());
//        Log.d("Profile Activity", currentUser.getName());
//        Log.d("Profile Activity", currentUser.getPhone());
//        Log.d("Profile Activity UID", currentUser.getUid());
//        viewModel.setUser(currentUser);
//        viewModel.getUser().observe(this, new Observer<User>() {
//            @Override
//            public void onChanged(User user) {
//                currentUser = user;
//                updateUI(user);
//            }
//        });
//        //Log.d("Profile Activity UID", currentUser.getUid())
//    }
//
//    private void initViews() {
//        nameView = findViewById(R.id.viewName);
//        emailView = findViewById(R.id.emailView);
//        phoneView = findViewById(R.id.phoneView);
//        imageView2 = findViewById(R.id.imageView2);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        buttonLogout = findViewById(R.id.button_logout);
//        Toolbar toolbar = findViewById(R.id.toolbar_profile);
//        setSupportActionBar(toolbar);
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_items, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//
//        if (item.getItemId() == R.id.settings_email_change) {
//            showRefactorWindow("email");
//        }
//        if (item.getItemId() == R.id.settings_name_change) {
//            showRefactorWindow("name");
//        }
//        if (item.getItemId() == R.id.settings_password_change) {
//            showRefactorWindow("password");
//        }
//        if (item.getItemId() == R.id.settings_phone_change) {
//            showRefactorWindow("phone");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showRefactorWindow(String type) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactorWindow = null;
//        MaterialEditText inputField = null;
//
//        switch (type) {
//            case "email":
//                refactorWindow = inflater.inflate(R.layout.refactor_email_form, null);
//                inputField = refactorWindow.findViewById(R.id.emailField);
//                break;
//            case "name":
//                refactorWindow = inflater.inflate(R.layout.refactor_name_form, null);
//                inputField = refactorWindow.findViewById(R.id.nameField);
//                break;
//            case "password":
//                refactorWindow = inflater.inflate(R.layout.refactor_password_form, null);
//                inputField = refactorWindow.findViewById(R.id.newPass);
//                MaterialEditText secondInputField = refactorWindow.findViewById(R.id.secondPass);
//                break;
//            case "phone":
//                refactorWindow = inflater.inflate(R.layout.refactor_phone_form, null);
//                inputField = refactorWindow.findViewById(R.id.phoneField);
//                break;
//        }
//
//        dialog.setView(refactorWindow);
//        dialog.setPositiveButton("Зберегти", (dialogInterface, i) -> {
//            // Code to handle saving the new value
//        });
//        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());
//
//        dialog.show();
//    }
//
//    private void setupListeners() {
//        buttonMain.setOnClickListener(view -> goToActivity(MenuActivity.class));
//        imageView2.setOnClickListener(view -> selectImage());
//        buttonLogout.setOnClickListener(view -> {
//            checkToken();
////            currentUser = null;
//            Picasso.get().invalidate(currentUser.getProfileUri());
//            goToActivity(MainActivity.class);
//        });
//    }
//
//    private void deleteToken() {
//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("AuthToken");
//        Log.d("Auth Token", "Token was deleted");
//        editor.apply();  // Или editor.commit() для синхронного удаления
//    }
//
//    private String getToken() {
//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", getApplication().MODE_PRIVATE);
//        return sharedPreferences.getString("AuthToken", null);  // Дешифрование токена после получения
//    }
//    private void checkToken(){
//        String authToken = getToken();
//        if(authToken != null){
//            deleteToken();
//        } else {
//            Log.d("Auth Token Failure", "Token was not found");
//        }
//    }
//
//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, viewModel.PICK_IMAGE);
//    }
//
//    // Обработка результатов выбора изображения
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == viewModel.PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            viewModel.uploadImage(data.getData());
//            Toast.makeText(this, "Зображення успішно завантажене!", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "Error: Спробуйте завантажити інше зображення!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // Обновление интерфейса пользователя
//    private void updateUI(User user) {
//        nameView.setText(user.getName());
//        emailView.setText(user.getEmail());
//        phoneView.setText(user.getPhone());
//        if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
//            Picasso.get()
//                    .load(user.getProfileUri())
//                    .placeholder(R.drawable.ic_profile)
//                    .resize(140, 140)
//                    .into(imageView2);
//        }
//    }
//
//    private void goToActivity(Class<?> activityClass) {
//        Intent intent = new Intent(ProfileActivity.this, activityClass);
//        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("userDetails", currentUser);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent); // Обов'язково викликати setIntent для оновлення наміру
//        initCurrentUser();
//    }
//}

//public class ProfileActivity extends AppCompatActivity {
//    private ProfileViewModel viewModel;
//    private TextView nameView, emailView, phoneView;
//    private ImageView imageView2;
//    private ImageButton buttonMain, buttonLogout;
//    private User profileUser;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        initViews();
//        //initViewModel();
//        //initCurrentUser();
//
//        // Получаем ViewModel
//        //initViewModel();
//
//        // Наблюдаем за изменениями в данных пользователя
//        //viewModel.getUser().observe(this, this::updateUI);
//
//        // Устанавливаем слушатели
//        setupListeners();
//    }
//
////    private void initViewModel() {
////        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
////        currentUser = getIntent().getParcelableExtra("userDetails");
////        Log.d("Profile Activity", currentUser.getUid());
////        Log.d("Profile Activity", currentUser.getEmail());
////        Log.d("Profile Activity", currentUser.getName());
////        Log.d("Profile Activity", currentUser.getPhone());
////        Log.d("Profile Activity UID", currentUser.getUid());
////        //viewModel.setUser(currentUser);
////        viewModel.initFirebase();
////        Log.d("User", "Download userdata from FirebaseDatabase");
////        viewModel.getUser().observe(this, new Observer<User>() {
////            @Override
////            public void onChanged(User user) {
////                currentUser = user;
////
////                updateUI(currentUser);
////            }
////        });
////    }
//
//    private void initCurrentUser() {
//        profileUser = getIntent().getParcelableExtra("userDetails");
//        Log.d("Profile Activity", profileUser.getUid());
//        Log.d("Profile Activity", profileUser.getEmail());
//        Log.d("Profile Activity", profileUser.getName());
//        Log.d("Profile Activity", profileUser.getPhone());
//        Log.d("Profile Activity UID", profileUser.getUid());
//        updateUI(profileUser);
//    }
//
//    private void initViews() {
//        nameView = findViewById(R.id.viewName);
//        emailView = findViewById(R.id.emailView);
//        phoneView = findViewById(R.id.phoneView);
//        imageView2 = findViewById(R.id.imageView2);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        buttonLogout = findViewById(R.id.button_logout);
//        Toolbar toolbar = findViewById(R.id.toolbar_profile);
//        setSupportActionBar(toolbar);
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_items, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//
//        if (item.getItemId() == R.id.settings_email_change) {
//            showRefactorWindow("email");
//        }
//        if (item.getItemId() == R.id.settings_name_change) {
//            showRefactorWindow("name");
//        }
//        if (item.getItemId() == R.id.settings_password_change) {
//            showRefactorWindow("password");
//        }
//        if (item.getItemId() == R.id.settings_phone_change) {
//            showRefactorWindow("phone");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showRefactorWindow(String type) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactorWindow = null;
//        MaterialEditText inputField = null;
//
//        switch (type) {
//            case "email":
//                refactorWindow = inflater.inflate(R.layout.refactor_email_form, null);
//                inputField = refactorWindow.findViewById(R.id.emailField);
//                break;
//            case "name":
//                refactorWindow = inflater.inflate(R.layout.refactor_name_form, null);
//                inputField = refactorWindow.findViewById(R.id.nameField);
//                break;
//            case "password":
//                refactorWindow = inflater.inflate(R.layout.refactor_password_form, null);
//                inputField = refactorWindow.findViewById(R.id.newPass);
//                MaterialEditText secondInputField = refactorWindow.findViewById(R.id.secondPass);
//                break;
//            case "phone":
//                refactorWindow = inflater.inflate(R.layout.refactor_phone_form, null);
//                inputField = refactorWindow.findViewById(R.id.phoneField);
//                break;
//        }
//
//        dialog.setView(refactorWindow);
//        dialog.setPositiveButton("Зберегти", (dialogInterface, i) -> {
//            // Code to handle saving the new value
//        });
//        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());
//
//        dialog.show();
//    }
//
//    private void setupListeners() {
//        buttonMain.setOnClickListener(view -> goToActivity(MenuActivity.class));
//        imageView2.setOnClickListener(view -> selectImage());
//        buttonLogout.setOnClickListener(view -> {
//            //checkToken();
////            currentUser = null;
//            goToActivity(MainActivity.class);
//        });
//    }
//
//    private void deleteToken() {
//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("AuthToken");
//        Log.d("Auth Token", "Token was deleted");
//        editor.apply();  // Или editor.commit() для синхронного удаления
//    }
//
//    private String getToken() {
//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AppPrefs", getApplication().MODE_PRIVATE);
//        return sharedPreferences.getString("AuthToken", null);  // Дешифрование токена после получения
//    }
//    private void checkToken(){
//        String authToken = getToken();
//        if(authToken != null){
//            deleteToken();
//        } else {
//            Log.d("Auth Token Failure", "Token was not found");
//        }
//    }
//
//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, viewModel.PICK_IMAGE);
//    }
//
//    // Обработка результатов выбора изображения
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == viewModel.PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            viewModel.uploadImage(data.getData());
//            Toast.makeText(this, "Зображення успішно завантажене!", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "Error: Спробуйте завантажити інше зображення!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // Обновление интерфейса пользователя
//    private void updateUI(User user) {
//        nameView.setText(user.getName());
//        emailView.setText(user.getEmail());
//        phoneView.setText(user.getPhone());
//        if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
//            Picasso.get()
//                    .load(user.getProfileUri())
//                    .placeholder(R.drawable.ic_profile)
//                    .resize(140, 140)
//                    .into(imageView2);
//        }
//    }
//
//    private void goToActivity(Class<?> activityClass) {
//        Intent intent = new Intent(ProfileActivity.this, activityClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("userDetails", profileUser);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        profileUser = null;
//    }
//
////    @Override
////    protected void onStart() {
////        super.onStart();
////        profileUser = null;
////        profileUser = getIntent().getParcelableExtra("userDetails");
////        Log.d("Profile Activity", profileUser.getEmail());
////        Log.d("Profile Activity", profileUser.getName());
////        Log.d("Profile Activity", profileUser.getPhone());
////        Log.d("Profile Activity UID", profileUser.getUid());
////        updateUI(profileUser);
////    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent); // Обов'язково викликати setIntent для оновлення наміру
//        initCurrentUser();
//    }
//}




//public class ProfileActivity extends AppCompatActivity {
//
//    public int PICK_IMAGE = 1;
//    private TextView nameView, emailView, phoneView;
//    private ImageButton buttonMain;
//    private ImageView imageView2;
//    private RelativeLayout root;
//
//    private FirebaseDatabase db;
//    private DatabaseReference users;
//    private FirebaseStorage storage;
//    private StorageReference storageRef;
//    private User currentUser;
//    private Task uploadTask;
//    private String uid;
//    private String customFileName = "profile_image.jpg";
//    private ValueEventListener usersEventListener; // To hold the event listener
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        initViews();
//        setupListeners();
//        currentUser = getIntent().getParcelableExtra("userDetails");
//        initFirebase();
//    }
//
//    private void initViews() {
//        nameView = findViewById(R.id.viewName);
//        emailView = findViewById(R.id.emailView);
//        phoneView = findViewById(R.id.phoneView);
//        imageView2 = findViewById(R.id.imageView2);
//        root = findViewById(R.id.root_profile);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        Toolbar toolbar = findViewById(R.id.toolbar_profile);
//        setSupportActionBar(toolbar);
//    }
//
//    private void initFirebase() {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            uid = firebaseUser.getUid();
//            db = FirebaseDatabase.getInstance();
//            users = db.getReference("Users").child(uid);
//            storage = FirebaseStorage.getInstance();
//            storageRef = storage.getReference("images");
//        }
//    }
//
//    private void updateUI(User user) {
//        nameView.setText(user.getName());
//        emailView.setText(user.getEmail());
//        phoneView.setText(user.getPhone());
//        if (user.getProfileUri() != null && !user.getProfileUri().isEmpty()) {
//            Picasso.get()
//                    .load(user.getProfileUri())
//                    .placeholder(R.drawable.ic_profile)
//                    .resize(140, 140)
//                    .into(imageView2);
//        }
//    }
//
//    private void setupListeners() {
//        buttonMain.setOnClickListener(view -> goToActivity(MenuActivity.class));
//        imageView2.setOnClickListener(view -> selectImage());
//    }
//
//    private void goToActivity(Class<?> activityClass) {
//        Intent intent = new Intent(ProfileActivity.this, activityClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("userDetails", currentUser);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Picasso.get().invalidate(data.getData());
//            uploadImage(data.getData());
//
//        }
//    }
//
//    private void uploadImage(Uri imageUri) {
//        StorageReference userProfileRef = storageRef.child("prof/" + uid + "/" + customFileName);
//        uploadTask = userProfileRef.putFile(imageUri);
//
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            Snackbar.make(root, "Image uploaded successfully", Snackbar.LENGTH_LONG).show();
//            userProfileRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                String newUri = uri.toString();
//                users.child("profileUri").setValue(newUri);
//                currentUser.setProfileUri(newUri);
//                //Picasso.get().invalidate(currentUser.getProfileUri());
//                updateUI(currentUser);
//
//            }).addOnFailureListener(e -> Snackbar.make(root, "Error getting download URL: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
//        }).addOnFailureListener(e -> Snackbar.make(root, "Upload failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_items, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//
//        if (item.getItemId() == R.id.settings_email_change) {
//            showRefactorWindow("email");
//        }
//        if (item.getItemId() == R.id.settings_name_change) {
//            showRefactorWindow("name");
//        }
//        if (item.getItemId() == R.id.settings_password_change) {
//            showRefactorWindow("password");
//        }
//        if (item.getItemId() == R.id.settings_phone_change) {
//            showRefactorWindow("phone");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showRefactorWindow(String type) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactorWindow = null;
//        MaterialEditText inputField = null;
//
//        switch (type) {
//            case "email":
//                refactorWindow = inflater.inflate(R.layout.refactor_email_form, null);
//                inputField = refactorWindow.findViewById(R.id.emailField);
//                break;
//            case "name":
//                refactorWindow = inflater.inflate(R.layout.refactor_name_form, null);
//                inputField = refactorWindow.findViewById(R.id.nameField);
//                break;
//            case "password":
//                refactorWindow = inflater.inflate(R.layout.refactor_password_form, null);
//                inputField = refactorWindow.findViewById(R.id.newPass);
//                MaterialEditText secondInputField = refactorWindow.findViewById(R.id.secondPass);
//                break;
//            case "phone":
//                refactorWindow = inflater.inflate(R.layout.refactor_phone_form, null);
//                inputField = refactorWindow.findViewById(R.id.phoneField);
//                break;
//        }
//
//        dialog.setView(refactorWindow);
//        dialog.setPositiveButton("Зберегти", (dialogInterface, i) -> {
//            // Code to handle saving the new value
//        });
//        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());
//
//        dialog.show();
//    }
//
//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, PICK_IMAGE);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        updateUI(currentUser);
//        //attachDatabaseReadListener(); // Reattach the listener when the activity starts
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        //detachDatabaseReadListener(); // Detach the listener when the activity stops
//    }
//}

//public class ProfileActivity extends AppCompatActivity {
//
//    private static final int PICK_IMAGE = 1;
//    private TextView nameView, emailView, phoneView;
//    private ImageView imageView2;
//    private DatabaseReference users;
//    private String uid;
//    private FirebaseStorage storage;
//    FirebaseDatabase db;
//    private StorageReference storageRef;
//    private RelativeLayout root;
//    private User userFromProfile = new User();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        setupViews();
//        //setupFirebase();
//        //loadImage();
//    }
//
//    private void setupViews() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        findViewById(R.id.buttonToMainActivity).setOnClickListener(v -> navigateToMain());
//        imageView2 = findViewById(R.id.imageView2);
//        imageView2.setOnClickListener(v -> selectImage());
//        nameView = findViewById(R.id.viewName);
//        emailView = findViewById(R.id.emailView);
//        phoneView = findViewById(R.id.phoneView);
//        root = findViewById(R.id.root_profile);
//    }
//
//    private void setupFirebase() {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            uid = firebaseUser.getUid();
//            db = FirebaseDatabase.getInstance("https://traine-11a25-default-rtdb.europe-west1.firebasedatabase.app/");
//            users = db.getReference("Users").child(uid);
//            emailView.setText(firebaseUser.getEmail());
//            listenForUserChanges();
//        }
//        storage = FirebaseStorage.getInstance("gs://traine-11a25.appspot.com");
//        storageRef = storage.getReference("images");
//    }
//
//    private void listenForUserChanges() {
//        users.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                userFromProfile = dataSnapshot.getValue(User.class);
//                updateUI();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Snackbar.make(root, "Error: " + databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void updateUI() {
//        nameView.setText(userFromProfile.getName());
//        emailView.setText(userFromProfile.getEmail());
//        phoneView.setText(userFromProfile.getPhone());
//        Picasso.get().load(userFromProfile.getProfileUri()).placeholder(R.drawable.ic_profile)
//                .resize(140, 140).into(imageView2);
//    }
//
//    private void navigateToMain() {
//        startActivity(new Intent(ProfileActivity.this, MenuActivity.class));
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_items, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.settings_email_change || id == R.id.settings_name_change ||
//                id == R.id.settings_password_change || id == R.id.settings_phone_change) {
//            showRefactorWindow(id);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showRefactorWindow(int type) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//        //View refactor_window = LayoutInflater.from(this).inflate(getLayoutId(type), null);
//        //dialog.setView(refactor_window);
//        dialog.show();
//    }
//
////    private int getLayoutId(int type) {
//////        switch (type) {
//////            case R.id.settings_email_change: return R.layout.refactor_email_form;
//////            case R.id.settings_name_change: return R.layout.refactor_name_form;
//////            case R.id.settings_password_change: return R.layout.refactor_password_form;
//////            case R.id.settings_phone_change: return R.layout.refactor_phone_form;
//////            default: return -1;
//////        }
////    }
//
//    void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, PICK_IMAGE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
//        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
//        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && imageReturnedIntent != null) {
//            Uri selectedImage = imageReturnedIntent.getData();
//            uploadImage(selectedImage);
//        }
//    }
//
//    private void uploadImage(Uri imageUri) {
//        try {
//            StorageReference userProfile = storageRef.child("prof/" + uid + "/profile_image.jpg");
//            imageView2.setImageURI(imageUri);
//            //uploadTask = userProfile.putFile(imageUri);
//            //handleUploadResult(uploadTask);
//        } catch (Exception e) {
//            Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//        }
//    }
//
//    private void handleUploadResult(UploadTask uploadTask) {
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            Snackbar.make(root, "Зображення успішно завантажене", Snackbar.LENGTH_LONG).show();
//            updateProfileUri(taskSnapshot.getMetadata().getReference().getDownloadUrl());
//        }).addOnFailureListener(e -> {
//            Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//        });
//    }
//
//    private void updateProfileUri(Task<Uri> downloadUrlTask) {
//        downloadUrlTask.addOnSuccessListener(uri -> {
//            String uriImage = uri.toString();
//            users.child("urlProfile").setValue(uriImage);
//            userFromProfile.setProfileUri(uriImage);
//        }).addOnFailureListener(e -> {
//            Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//        });
//    }
//}

//public class ProfileActivity extends AppCompatActivity {
//
//    public static final int PICK_IMAGE = 1;
//    TextView nameView, emailView, phoneView;
//    ImageButton buttonMain;
//
//
//    ImageView imageView2;
//
//    private FirebaseDatabase db;
//    private DatabaseReference users;
//    private String uid;
//    private FirebaseStorage storage;
//
//
//    protected String uriString;
//    StorageReference storageRef;
//    public UploadTask uploadTask;
//
//    private RelativeLayout root;
//
//    private String customFileName = "profile_image.jpg";
//    //private String uriImage;
//
//    public User userFromProfile = new User();
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.menu_items, menu);
//        return true;
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//
//
//        if (item.getItemId() == R.id.settings_email_change) {
//            showRefactorEmailWindow();
//        }
//        if (item.getItemId() == R.id.settings_name_change) {
//            showRefactorNameWindow();
//        }
//        if (item.getItemId() == R.id.settings_password_change) {
//            showRefactorPasswordWindow();
//        }
//        if (item.getItemId() == R.id.settings_phone_change) {
//            showRefactorPhoneWindow();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showRefactorEmailWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactor_window = inflater.inflate(R.layout.refactor_email_form, null);
//        dialog.setView(refactor_window);
//
//        MaterialEditText email = refactor_window.findViewById(R.id.emailField);
//        dialog.show();
//    }
//    private void showRefactorNameWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactor_window = inflater.inflate(R.layout.refactor_name_form, null);
//        dialog.setView(refactor_window);
//
//        MaterialEditText name = refactor_window.findViewById(R.id.nameField);
//        dialog.show();
//    }
//    private void showRefactorPasswordWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactor_window = inflater.inflate(R.layout.refactor_password_form, null);
//        dialog.setView(refactor_window);
//
//        MaterialEditText newPassword = refactor_window.findViewById(R.id.newPass);
//        MaterialEditText secondPassword = refactor_window.findViewById(R.id.secondPass);
//        dialog.show();
//    }
//    private void showRefactorPhoneWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Внесіть зміни");
//        dialog.setMessage("Введіть зміни в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View refactor_window = inflater.inflate(R.layout.refactor_phone_form, null);
//        dialog.setView(refactor_window);
//
//        MaterialEditText phone = refactor_window.findViewById(R.id.phoneField);
//        dialog.show();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//        nameView = findViewById(R.id.viewName);
//        emailView = findViewById(R.id.emailView);
//        phoneView = findViewById(R.id.phoneView);
//        imageView2 = findViewById(R.id.imageView2);
//        root = findViewById(R.id.root_profile);
//        buttonMain = findViewById(R.id.buttonToMainActivity);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        setSupportActionBar(toolbar);
//
//        buttonMain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ProfileActivity.this, MenuActivity.class);
//                startActivity(intent);
//            }
//        });
//
//
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (firebaseUser != null) {
//            // Name, email address, and ??profile photo Url??
//            String email = firebaseUser.getEmail();
//
//            emailView.setText(email);
//
//            // Check if user's email is verified
//            boolean emailVerified = firebaseUser.isEmailVerified();
//
//            // The user's ID, unique to the Firebase project. Do NOT use this value to
//            // authenticate with your backend server, if you have one. Use
//            // FirebaseUser.getIdToken() instead.
//            uid = firebaseUser.getUid();
//        }
//
//
//        db = FirebaseDatabase.getInstance("https://traine-11a25-default-rtdb.europe-west1.firebasedatabase.app/");
//        users = db.getReference("Users").child(uid);
//
//        DatabaseReference dbReferences = db.getReference("Users");
//
//        users.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String name = dataSnapshot.child("name").getValue(String.class);
//                String email = dataSnapshot.child("email").getValue(String.class);
//                String phone = dataSnapshot.child("phone").getValue(String.class);
//                String uri = dataSnapshot.child("testProfile").getValue(String.class);
//
//                try{
//                    userFromProfile.setName(name);
//                    userFromProfile.setEmail(email);
//                    userFromProfile.setPhone(phone);
//                    userFromProfile.setUri(uri);
//
//                    Picasso.get()
//                            .load(userFromProfile.getUri())
//                            .placeholder(R.drawable.ic_profile)
//                            .resize(140, 140)
//                            .into(imageView2);
//
//                }catch (Exception e){
//                    Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                }
//
//                nameView.setText(userFromProfile.getName());
//                emailView.setText(userFromProfile.getEmail());
//                phoneView.setText(userFromProfile.getPhone());
//
//                // Обработка полученных данных
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Обработка ошибок
//                //textTest2.setText("Ошибка чтения данных: " + databaseError.getCode());
//            }
//        });
//
//        storage = FirebaseStorage.getInstance("gs://traine-11a25.appspot.com");
//
//        // Create a storage reference from our app
//        storageRef = storage.getReference("images");
//
//        imageView2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectImage();
//            }
//        });
//
//
//    }
//
//
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
//        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
//            if(requestCode == PICK_IMAGE || requestCode == RESULT_OK || imageReturnedIntent != null || imageReturnedIntent.getData() != null) {
//                try{
//
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    uriString = selectedImage.toString(); //storing uri (content path)
//                    StorageReference userProfile = storageRef.child("prof/"+uid+"/"+customFileName);
//                    imageView2.setImageURI(selectedImage);
//                    uploadTask = userProfile.putFile(selectedImage);
//
//                    StorageReference Image = storage.getReference().child("images/prof/"+uid+"/profile_image.jpg");
//
//                    Image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            String uriImage = uri.toString();
//                            userFromProfile.setUri(uriImage);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                        }
//                    });
//
//                }catch (Exception e){
//                    Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                }
//
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Snackbar.make(root, "Зображення успішно завантажене", Snackbar.LENGTH_LONG).show();
//                        users.child("urlProfile").setValue(uriString);
//                        users.child("testProfile").setValue(userFromProfile.getUri());
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                    }
//                });
//            }
//        }
//
//    //select from Gallery
//    void selectImage(){
////        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
////                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////        startActivityForResult(pickPhoto , 1);
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, PICK_IMAGE);
//    }
//}