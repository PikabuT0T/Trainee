package com.example.traine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import Models.User;

public class ProfileActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    TextView nameView, emailView, phoneView;
    ImageButton buttonMain;

    ImageView imageView2;

    private FirebaseDatabase db;
    private DatabaseReference users;
    private String uid;
    private FirebaseStorage storage;


    protected String uriString;
    StorageReference storageRef;
    public UploadTask uploadTask;

    private RelativeLayout root;

    private String customFileName = "profile_image.jpg";
    //private String uriImage;

    public User userFromProfile = new User();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.settings_action_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.settings_menu_change) {
            Toast.makeText(this, "sds", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameView = findViewById(R.id.viewName);
        emailView = findViewById(R.id.emailView);
        phoneView = findViewById(R.id.phoneView);
        imageView2 = findViewById(R.id.imageView2);
        root = findViewById(R.id.root_profile);
        buttonMain = findViewById(R.id.buttonToMainActivity);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });


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
                String uri = dataSnapshot.child("testProfile").getValue(String.class);

                try{
                    userFromProfile.setName(name);
                    userFromProfile.setEmail(email);
                    userFromProfile.setPhone(phone);
                    userFromProfile.setUri(uri);

                    Picasso.get()
                            .load(userFromProfile.getUri())
                            .placeholder(R.drawable.ic_profile)
                            .resize(140, 140)
                            .into(imageView2);

                }catch (Exception e){
                    Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }

                nameView.setText(userFromProfile.getName());
                emailView.setText(userFromProfile.getEmail());
                phoneView.setText(userFromProfile.getPhone());

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
        storageRef = storage.getReference("images");

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
            if(requestCode == PICK_IMAGE || requestCode == RESULT_OK || imageReturnedIntent != null || imageReturnedIntent.getData() != null) {
                try{

                    Uri selectedImage = imageReturnedIntent.getData();
                    uriString = selectedImage.toString(); //storing uri (content path)
                    StorageReference userProfile = storageRef.child("prof/"+uid+"/"+customFileName);
                    imageView2.setImageURI(selectedImage);
                    uploadTask = userProfile.putFile(selectedImage);

                    StorageReference Image = storage.getReference().child("images/prof/"+uid+"/profile_image.jpg");

                    Image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String uriImage = uri.toString();
                            userFromProfile.setUri(uriImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });

                }catch (Exception e){
                    Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(root, "Зображення успішно завантажене", Snackbar.LENGTH_LONG).show();
                        users.child("urlProfile").setValue(uriString);
                        users.child("testProfile").setValue(userFromProfile.getUri());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }

    //select from Gallery
    void selectImage(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }
}