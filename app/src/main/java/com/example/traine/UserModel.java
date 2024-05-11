package com.example.traine;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Models.User;

public class UserModel {

    private DatabaseReference users;
    private ValueEventListener usersEventListener;
    private StorageReference storageRef; // Добавляем ссылку на Firebase Storage

    public interface UserDataListener {
        void onDataLoaded(User user);
        void onDataUpdated(String profileUri);
        void onError(String errorMessage);
    }

    public UserModel() {
        initFirebase();
    }

    public void initFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        users = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("images"); // Инициализация хранилища
    }

    public void attachDatabaseReadListener(UserDataListener listener) {
        if (usersEventListener == null) {
            usersEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User currentUser = dataSnapshot.getValue(User.class);
                    listener.onDataLoaded(currentUser);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onError(error.getMessage());
                }
            };
            users.addValueEventListener(usersEventListener);
        }
    }

    public void detachDatabaseReadListener() {
        if (usersEventListener != null) {
            users.removeEventListener(usersEventListener);
            usersEventListener = null;
        }
    }

    // Метод для загрузки изображений в Firebase Storage
    public void uploadImage(Uri imageUri, UserModel.UserDataListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference userProfileRef = storageRef.child("prof/" + uid + "/profile_image.jpg");
        userProfileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            userProfileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String newUri = uri.toString();
                users.child("profileUri").setValue(newUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Только обновляем URI изображения
                        listener.onDataUpdated(newUri);
                    } else {
                        listener.onError(task.getException().getMessage());
                    }
                });
            }).addOnFailureListener(e -> listener.onError(e.getMessage()));
        }).addOnFailureListener(e -> listener.onError(e.getMessage()));

    }
}
//public class UserModel {
//
//    private DatabaseReference users;
//    private ValueEventListener usersEventListener;
//
//
//    public interface UserDataListener {
//        void onDataLoaded(User user);
//        void onError(String errorMessage);
//    }
//
//    public void initFirebase() {
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        users = FirebaseDatabase.getInstance()
//                .getReference("Users")
//                .child(uid);
//    }
//
//    public void attachDatabaseReadListener(UserDataListener listener) {
//        if (usersEventListener == null) {
//            usersEventListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    User currentUser = dataSnapshot.getValue(User.class);
//                    listener.onDataLoaded(currentUser);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    listener.onError(error.getMessage());
//                }
//            };
//            users.addValueEventListener(usersEventListener);
//        }
//    }
//
//    public void detachDatabaseReadListener() {
//        if (usersEventListener != null) {
//            users.removeEventListener(usersEventListener);
//            usersEventListener = null;
//        }
//    }
//}
