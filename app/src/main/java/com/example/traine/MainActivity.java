package com.example.traine;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView buttonSignUp, buttonSignIn;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    RelativeLayout root;
    LayoutInflater inflater; // Перевикористання inflater

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        root = findViewById(R.id.root_element);
        inflater = LayoutInflater.from(this); // Ініціалізація тут

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        buttonSignUp.setOnClickListener(view -> showSignUpWindow());
        buttonSignIn.setOnClickListener(view -> showSignInWindow());
    }

    private void showSignInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Увійти").setMessage("Введіть дані для входу");
        View sign_in_window = inflater.inflate(R.layout.sign_in_form, null);
        dialog.setView(sign_in_window);

        MaterialEditText email = sign_in_window.findViewById(R.id.emailField);
        MaterialEditText pass = sign_in_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Увійти", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Введіть коректну пошту", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (pass.getText().toString().length() < 6) {
                Snackbar.make(root, "Введіть пароль, який має більше 5 символів", Snackbar.LENGTH_LONG).show();
                return;
            }
            // Вхід за поштою та паролем
            mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        startActivity(new Intent(MainActivity.this, MenuActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Snackbar.make(root, "Помилка авторізації!" + e.getMessage(), Snackbar.LENGTH_LONG).show());
        });
        dialog.show();
    }

    private void showSignUpWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Зареєструватися").setMessage("Введіть необхідні дані для реєстрації в поля нижче");
        View register_window = inflater.inflate(R.layout.register_form, null);
        dialog.setView(register_window);

        MaterialEditText email = register_window.findViewById(R.id.emailField);
        MaterialEditText pass = register_window.findViewById(R.id.passField);
        MaterialEditText name = register_window.findViewById(R.id.nameField);
        MaterialEditText phone = register_window.findViewById(R.id.phoneField);

        dialog.setNegativeButton("Скасувати", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Додати користувача", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Введіть коректну пошту", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (pass.getText().toString().length() < 6) {
                Snackbar.make(root, "Введіть пароль, який має більше 5 символів", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(name.getText().toString())) {
                Snackbar.make(root, "Введіть коректне ім'я", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(phone.getText().toString())) {
                Snackbar.make(root, "Введіть коректний номер телефону", Snackbar.LENGTH_LONG).show();
                return;
            }
            // Сама реєстрація користувача в БД
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", email.getText().toString());
                        userMap.put("pass", pass.getText().toString());
                        userMap.put("name", name.getText().toString());
                        userMap.put("phone", phone.getText().toString());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(userMap)
                                .addOnSuccessListener(unused -> Snackbar.make(root, "Новий користувач успішно створений!", Snackbar.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Snackbar.make(root, "Виникли складнощі при створенні користувача!", Snackbar.LENGTH_LONG).show());
                    });
        });
        dialog.show();
    }
}

//    registerUser(email.getText().toString(), pass.getText().toString(), name.getText().toString(), phone.getText().toString());
//    private void registerUser(String email, String password, String name, String phone) {
//        OkHttpClient client = new OkHttpClient();
//        String signUpUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=AIzaSyDg2uWcROtTvPj0LDIP9D0L_AwXXxLGK9w";
//        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}";
//
//        RequestBody signUpBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
//        Request request = new Request.Builder()
//                .url(signUpUrl)
//                .post(signUpBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    final String responseData = response.body().string();
//                    // Парсим ответ, чтобы извлечь нужные данные
//                    String idToken = null; // ID Token пользователя
//                    String localId = null;
//                    try {
//                        idToken = new JSONObject(responseData).getString("idToken");
//                        localId = new JSONObject(responseData).getString("localId");
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    String finalLocalId = localId;
//                    String finalIdToken = idToken;
//                    runOnUiThread(() -> {
//                        saveUserData(finalLocalId, email, name, phone, finalIdToken);
//                        //Snackbar.make(root, "Регистрация успешна: " + responseData, Snackbar.LENGTH_LONG).show();
//                        // Сохраняем дополнительные данные пользователя в Firebase Database
//                    });
//                }
//            }
//        });
//    }
//
//    private void saveUserData(String userId, String email, String name, String phone, String idToken) {
//        OkHttpClient client = new OkHttpClient();
//        String url = "https://traine-11a25.firebaseio.com/Users/" + userId + ".json";
//
//        String json = "{\"email\":\"" + email + "\", \"name\":\"" + name + "\", \"phone\":\"" + phone + "\"}";
//
//        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
//        Request request = new Request.Builder()
//                .url(url)
//                .put(body) // Используем PUT для создания или обновления данных пользователя
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
////                e.printStackTrace();
//                Snackbar.make(root, "Error:" + e, Snackbar.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    runOnUiThread(() -> Snackbar.make(root, "Данные пользователя сохранены в Firebase Database.", Snackbar.LENGTH_LONG).show());
//                }
//            }
//        });
//    }



//registerUser(email.getText().toString(), pass.getText().toString(), name.getText().toString(), phone.getText().toString());
//public class MainActivity extends AppCompatActivity {
//
//    TextView buttonSignUp, buttonSignIn;
//    public FirebaseAuth mAuth;
//    public FirebaseDatabase db;
//    public FirebaseFirestore database;
//    public DatabaseReference users;
//
//    RelativeLayout root;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.profile_layout);
//
//        buttonSignIn = findViewById(R.id.buttonSignIn);
//        buttonSignUp = findViewById(R.id.buttonSignUp);
//
//        root = findViewById(R.id.root_element);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        db = FirebaseDatabase.getInstance();
//        users = db.getReference("Users");
//
//        database = FirebaseFirestore.getInstance();
////        users1 = database.collection("Users");
//
//        buttonSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showSignUpWindow();
//            }
//        });
//        buttonSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showSignInWindow();
//            }
//        });
//    }
//
//    private void showSignInWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Увійти");
//        dialog.setMessage("Введіть дані для входу");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View sign_in_window = inflater.inflate(R.layout.sign_in_form, null);
//        dialog.setView(sign_in_window);
//
//        MaterialEditText email = sign_in_window.findViewById(R.id.emailField);
//        MaterialEditText pass = sign_in_window.findViewById(R.id.passField);
//
//        dialog.setNegativeButton("Скасувати", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//            }
//        });
//
//        dialog.setPositiveButton("Увійти", new DialogInterface.OnClickListener() {
//            @Override //налаштовуємо помилки для полів реєстрації
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if(TextUtils.isEmpty(email.getText().toString())){
//                    Snackbar.make(root, "Введіть коректну пошту", Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//                if(pass.getText().toString().length() < 6){
//                    Snackbar.make(root, "Введіть пароль, який має більше 5 символів", Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//
//                //вхід за поштою та паролем
//                mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//                                startActivity(new Intent(MainActivity.this, MenuActivity.class));
//                                finish();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Snackbar.make(root, "Помилка авторізації!" + e.getMessage(), Snackbar.LENGTH_LONG).show();
//                            }
//                        });
//            }
//        });
//        dialog.show();
//    }
//
//    private void showSignUpWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Зареєструватися");
//        dialog.setMessage("Введіть необхідні дані для реєстрації в поля нижче");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View register_window = inflater.inflate(R.layout.register_form, null);
//        dialog.setView(register_window);
//
//        MaterialEditText email = register_window.findViewById(R.id.emailField);
//        MaterialEditText pass = register_window.findViewById(R.id.passField);
//        MaterialEditText name = register_window.findViewById(R.id.nameField);
//        MaterialEditText phone = register_window.findViewById(R.id.phoneField);
//
//        dialog.setNegativeButton("Скасувати", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//            }
//        });
//
//        dialog.setPositiveButton("Додати користувача", new DialogInterface.OnClickListener() {
//            @Override //налаштовуємо помилки для полів реєстрації
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if(TextUtils.isEmpty(email.getText().toString())){
//                    Snackbar.make(root, "Введіть коректну пошту", Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//                if(pass.getText().toString().length() < 6){
//                    Snackbar.make(root, "Введіть пароль, який має більше 5 символів", Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//                if(TextUtils.isEmpty(name.getText().toString())){
//                    Snackbar.make(root, "Введіть коректне ім'я", Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//                if(TextUtils.isEmpty(phone.getText().toString())){
//                    Snackbar.make(root, "Введіть коректний номер телефону", Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//                //сама реєстрація користувача в БД
//                mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//                                User user = new User();
//                                user.setEmail(email.getText().toString());
//                                user.setPass(pass.getText().toString());
//                                user.setName(name.getText().toString());
//                                user.setPhone(phone.getText().toString());
//
//                                Map<String, Object> userMap = new HashMap<>();
//                                userMap.put("email", user.getEmail().toString());
//                                userMap.put("pass", user.getPass().toString());
//                                userMap.put("name", user.getName().toString());
//                                userMap.put("phone", user.getPhone().toString());
//
//                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                        .setValue(userMap)
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void unused) {
//                                                Snackbar.make(root, "Новий користувач успішно створений!", Snackbar.LENGTH_LONG).show();
//                                            }
//                                        })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Snackbar.make(root, "Виникли складнощі при створенні користувача!", Snackbar.LENGTH_LONG).show();
//                                            }
//                                        });
//                            }
//                        });
//            }
//        });
//
//        dialog.show();
//    }
//}