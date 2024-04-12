package com.example.traine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import Models.User;

public class ProfileActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    TextView nameView, emailView, phoneView, textTest2, textTest1;
    Button buttonProfile;

    ImageView imageView2;

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private List<String> listData;

    private String uid;
    private FirebaseStorage storage;

    private Uri imageUri;

    StorageReference storageRef;
    public UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameView = findViewById(R.id.viewName);
        emailView = findViewById(R.id.emailView);
        phoneView = findViewById(R.id.phoneView);
        textTest1 = findViewById(R.id.test1View);
        textTest2 = findViewById(R.id.test2View);
        imageView2 = findViewById(R.id.imageView2);

//        buttonProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //changeProfileImage();
//            }
//        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            // Name, email address, and ??profile photo Url??
            String email = firebaseUser.getEmail();

            emailView.setText(email);

            // Check if user's email is verified
            boolean emailVerified = firebaseUser.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = firebaseUser.getUid();

            textTest2.setText(uid);
        }


        db = FirebaseDatabase.getInstance("https://traine-11a25-default-rtdb.europe-west1.firebasedatabase.app/");
        users = db.getReference("Users").child(uid);

        DatabaseReference dbReferences = db.getReference("Users");

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);

                nameView.setText(name);
                emailView.setText(email);
                phoneView.setText(phone);

                // Обработка полученных данных
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
                //textTest2.setText("Ошибка чтения данных: " + databaseError.getCode());
            }
        });

        storage = FirebaseStorage.getInstance("gs://traine-11a25.appspot.com");

        // Create a storage reference from our app
        storageRef = storage.getReference("images/profiles");

        // Create a child reference
        // imagesRef now points to "images"
        //StorageReference profilesRef = storageRef.child("profiles");

        // Child references can also take paths
        // spaceRef now points to "images/space.jpg
        // imagesRef still points to "images"


//        StorageReference baseProfileRef = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/traine-11a25.appspot.com/o/image%2Fprofiles%2F2919906.png?alt=media&token=78883bd7-99ae-4f4e-a92c-93e2371f1ca3");
//
//        String url = "https://firebasestorage.googleapis.com/v0/b/traine-11a25.appspot.com/o/image%2Fprofiles%2F2919906.png?alt=media&token=78883bd7-99ae-4f4e-a92c-93e2371f1ca3";
//        Picasso.get()
//                .load(url)
//                //.placeholder() // Изображение-заглушка
//                //.error(R.drawable.error_image) // Обработка ошибок
//                .into(imageView2); // Отображение в ImageView


//        storageRef.child("image/profiles/2919906.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Picasso.get()
//                        .load(uri)
//                        //.placeholder() // Изображение-заглушка
//                        //.error(R.drawable.error_image) // Обработка ошибок
//                        .into(imageView2); // Отображение в ImageView
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });

//        buttonProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                uploadData();
//            }
//        });
        // ИЗУЧИТЬ НАМЕРЕНИЯ Android Studio
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(requestCode == PICK_IMAGE || requestCode == RESULT_OK || data != null || data.getData() != null) {
                imageUri = data.getData();

                Picasso.get()
                        .load(imageUri)
                        .into(imageView2);
            }
        }catch (Exception e){
            Toast.makeText(this, "Error"+e, Toast.LENGTH_SHORT).show();
        }
    }

    //НЕПОНЯТНО, ФОР ВОТ
    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));
    }
    private void uploadData() {
        final StorageReference reference = storageRef.child(System.currentTimeMillis()+"."+getFileExt(imageUri));

        uploadTask = reference.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }

                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();

                    //Map<String, >
                }
            }
        });
    }
}